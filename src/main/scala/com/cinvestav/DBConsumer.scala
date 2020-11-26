package com.cinvestav

import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.kafka.ProducerResult
import org.mongodb.scala.MongoCollection
//import com.cinvestav.Main.dbProducer
import fs2.kafka.{AutoOffsetReset, CommittableConsumerRecord, ConsumerSettings, ProducerRecord, ProducerRecords, consumerStream, produce}
import fs2.Stream
import io.circe.parser.decode
import scala.concurrent.duration._
import scala.language.postfixOps
import com.cinvestav.domain.SensorMeasurement
import com.cinvestav.pipes.SavePipe
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

class DBConsumer[F[_]:ConcurrentEffect:ContextShift:Timer](BOOTSTRAP_SERVERS:String,TOPIC:String,GROUP:String){
  val consumerSettings: ConsumerSettings[F, Option[String], String] =
    ConsumerSettings[F, Option[String], String]
      .withAutoOffsetReset(AutoOffsetReset.Latest)
      .withBootstrapServers(BOOTSTRAP_SERVERS)
      .withGroupId(GROUP)


  def run()(implicit collection: MongoCollection[SensorMeasurement],dbProducer: DBProducer[F]): Stream[F,
    ProducerResult[Option[String], String, Unit]] ={
    consumerStream[F]
      .using(consumerSettings)
      .evalTap(_.subscribeTo(TOPIC))
      .flatMap(_.stream)
      .map(_.record.value)
      .map(x=>decode[SensorMeasurement](x))
      .filter(_.isRight)
      .map(x=>x.getOrElse( SensorMeasurement("",0,0,0,0,0,0,0)))
      .groupWithin(100,1 seconds)
      .through(SavePipe[F]())
      .flatMap(x=>Stream.emits(x))
      .map(x=>ProducerRecord("s1",None,x.asJson.toString))
      .map(x=>ProducerRecords.one(x))
      .through(produce(dbProducer.PRODUCER_SETTINGS))
      .evalTap(x=>println(s"SAVED ${x}").pure[F])
  }
}

object DBConsumer {
//  val TOPIC:String = "db"

  def apply[F[_] : ConcurrentEffect : ContextShift : Timer](BOOTSTRAP_SERVERS: String, TOPIC: String, GROUP: String): DBConsumer[F] =
    new DBConsumer(BOOTSTRAP_SERVERS, TOPIC, GROUP)


//  val consumerSettings: ConsumerSettings[IO, Option[String], String] =
//    ConsumerSettings[IO, Option[String], String]
//      .withAutoOffsetReset(AutoOffsetReset.Latest)
//      .withBootstrapServers("localhost:9092")
//      .withGroupId("group_1")
//  def stream()(implicit cs:ContextShift[IO],timer:Timer[IO],collection:MongoCollection[SensorMeasurement],
//               dbProducer: DBProducer[IO]): Stream[IO, ProducerResult[Option[String], String, Unit]]
//  =
//    consumerStream[IO]
//      .using(consumerSettings)
//      .evalTap(_.subscribeTo(TOPIC))
//      .flatMap(_.stream)
//      .map(_.record.value)
//      .map(x=>decode[SensorMeasurement](x))
//      .filter(_.isRight)
//      .map(x=>x.getOrElse( SensorMeasurement("",0,0,0,0,0,0,0)))
//      .groupWithin(100,1 seconds)
//      .through(SavePipe[IO]())
//      .flatMap(x=>Stream.emits(x))
//      .map(x=>ProducerRecord("s1",None,x.asJson.toString))
//      .map(x=>ProducerRecords.one(x))
//      .through(produce(dbProducer.PRODUCER_SETTINGS))
//      .evalTap(x=>println(s"SAVED ${x}").pure[IO])

}
