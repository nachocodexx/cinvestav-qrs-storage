package com.cinvestav.avro
import fs2.kafka.vulcan
import fs2.kafka.vulcan.{AvroSettings, SchemaRegistryClientSettings}
import cats.effect.{IO, Sync}
import cats.effect.implicits._

object MyAvroSettings {
  def getAvroSettings[F[_]:Sync]: AvroSettings[F] = AvroSettings{
    SchemaRegistryClientSettings[F]("http://ec2-3-90-166-46.compute-1.amazonaws.com:8081")
  }
}
