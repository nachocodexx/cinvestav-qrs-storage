package com.cinvestav
import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.kafka.{CommittableConsumerRecord, ConsumerStream, KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings, produce, producerStream}
import fs2.Stream
import io.circe.Json

import scala.concurrent.duration._
import scala.language.postfixOps

class DBProducer[F[_]:ConcurrentEffect:ContextShift:Timer](BOOTSTRAP_SERVERS:String){

  val PRODUCER_SETTINGS: ProducerSettings[F, Option[String], String] = ProducerSettings[F,Option[String],String]
    .withBootstrapServers(BOOTSTRAP_SERVERS)

  def runStream(): Stream[F, KafkaProducer.Metrics[F, Option[String], String]] =
    producerStream[F]
      .using(PRODUCER_SETTINGS)
}

object DBProducer {
  def apply[F[_] : ConcurrentEffect : ContextShift : Timer](BOOTSTRAP_SERVERS:String): DBProducer[F] =
    new DBProducer(BOOTSTRAP_SERVERS)
}
