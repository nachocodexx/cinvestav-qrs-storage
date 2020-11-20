package com.cinvestav

import cats.effect.{ContextShift, IO, Timer}
import fs2.kafka.{AutoOffsetReset, CommittableConsumerRecord, ConsumerSettings, consumerStream}
import fs2.Stream

object QRSKafkaConsumer {
  val TOPIC:String = "storage"
  val consumerSettings: ConsumerSettings[IO, Option[String], String] =
    ConsumerSettings[IO, Option[String], String]
      .withAutoOffsetReset(AutoOffsetReset.Latest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

  def stream()(implicit cs:ContextShift[IO],timer:Timer[IO]): Stream[IO, CommittableConsumerRecord[IO, Option[String], String]] =
    consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo(TOPIC))
      .flatMap(_.stream)


}
