package com.cinvestav
import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.cinvestav.config.ServiceConfig
import com.cinvestav.domain.{QRSComplexEvent, QRSComplexEventKey, QRSComplexKey}
import com.cinvestav.pipes.SavePipe
import fs2.Chunk
import fs2.Stream
import fs2.kafka.ProducerResult
import org.mongodb.scala.MongoCollection

import java.util.Calendar
//import com.cinvestav.Main.dbProducer
import fs2.kafka.{AutoOffsetReset,  ConsumerSettings, ProducerRecord, ProducerRecords, consumerStream, produce}
import scala.concurrent.duration._
import scala.language.postfixOps
import com.cinvestav.domain.QRSComplexResult


class DBConsumer[F[_]:ConcurrentEffect:ContextShift:Timer](BOOTSTRAP_SERVERS:String, TOPIC:String, GROUP_ID:String){

  import com.cinvestav.avro.deserializers.VulcanDeserializer
  val deserializer: VulcanDeserializer[F] = VulcanDeserializer[F]()

  val consumerSettings: ConsumerSettings[F, QRSComplexKey, QRSComplexResult] =
    ConsumerSettings[F, QRSComplexKey, QRSComplexResult](
      keyDeserializer = deserializer.keyDeserializer,
      valueDeserializer = deserializer.valueDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Latest)
      .withBootstrapServers(BOOTSTRAP_SERVERS)
      .withGroupId(GROUP_ID)


  def run()(implicit collection: MongoCollection[QRSComplexResult], dbProducer: DBProducer[F],config:ServiceConfig)= {
    dbProducer.runStream().flatMap{producer=>
    consumerStream[F]
      .using(consumerSettings)
      .evalTap(_.subscribeTo(TOPIC))
      .flatMap(_.stream)
      .map(_.record.value)
      .groupWithin(100,1 seconds)
      .through(SavePipe[F])
      .flatMap(x=>Stream.emits(x))
      .evalMap{ x=>
        val key  = QRSComplexEventKey("sid",x.sensorId)
        val value = QRSComplexEvent(x.sensorId,x.measurement,x.timestamp,x.filtered_ecg,x.differentiated_ecg,x
          .squared_ecg,x.integrated_ecg,x.qrs_timestamp)
        val record = ProducerRecord(config.producerTopic,key,value)
        ProducerRecords.one(record).pure[F]
      }
      .evalMap(producer.produce)
      .debug(x=>s"${Calendar.getInstance().getTime} [INFO] - Event sent to Websocket server")
  }
  }
}

object DBConsumer {
  def apply[F[_] : ConcurrentEffect : ContextShift : Timer](BOOTSTRAP_SERVERS: String, TOPIC: String,
                                                            GROUP: String): DBConsumer[F] =
    new DBConsumer(BOOTSTRAP_SERVERS, TOPIC, GROUP)

}
