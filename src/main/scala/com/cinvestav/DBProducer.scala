package com.cinvestav
import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import com.cinvestav.domain.{QRSComplexEvent, QRSComplexEventKey}
import fs2.kafka.{ KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings, produce, producerStream}
import fs2.Stream

import scala.concurrent.duration._
import scala.language.postfixOps

class DBProducer[F[_]:ConcurrentEffect:ContextShift:Timer](BOOTSTRAP_SERVERS:String){

  import com.cinvestav.avro.serializers.VulcanSerializer
  val serializer: VulcanSerializer[F] = VulcanSerializer[F]()
  val PRODUCER_SETTINGS: ProducerSettings[F, QRSComplexEventKey, QRSComplexEvent] =
    ProducerSettings[F,QRSComplexEventKey,QRSComplexEvent](
      keySerializer = serializer.keyDeserializer,
      valueSerializer = serializer.valueDeserializer
    )
    .withBootstrapServers(BOOTSTRAP_SERVERS)

  def runStream(): Stream[F, KafkaProducer.Metrics[F, QRSComplexEventKey, QRSComplexEvent]] =
    producerStream[F]
      .using(PRODUCER_SETTINGS)
}

object DBProducer {
  def apply[F[_] : ConcurrentEffect : ContextShift : Timer](BOOTSTRAP_SERVERS:String): DBProducer[F] =
    new DBProducer(BOOTSTRAP_SERVERS)
}
