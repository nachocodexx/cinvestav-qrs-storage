package com.cinvestav.avro.serializers

import cats.effect.Sync
import com.cinvestav.avro.MyAvroSettings
import com.cinvestav.domain.{QRSComplexEvent, QRSComplexEventKey}
import fs2.kafka.{RecordDeserializer, RecordSerializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer}
import vulcan.Codec
import cats.implicits._

class VulcanSerializer[F[_]:Sync]  {
  implicit val qrsComplexResult:Codec[QRSComplexEvent] =
    Codec.record(
      name = "QRSComplexEvent",
      namespace = "com.cinvestav"
    ){ field=>
      (
        field("sensorId",_.sensorId),
        field("measurement",_.measurement),
        field("timestamp",_.timestamp),
        field("filtered_ecg",_.filtered_ecg),
        field("differentiated_ecg",_.differentiated_ecg),
        field("squared_ecg",_.squared_ecg),
        field("integrated_ecg",_.integrated_ecg),
        field("qrs_timestamp",_.qrs_timestamp),
        ).mapN(QRSComplexEvent)
    }

  implicit val qrsComplexKey:Codec[QRSComplexEventKey] =
    Codec.record(
      name = "QRSComplexEventKey",
      namespace = "com.cinvestav"
    ){ field=>
      (
        field("prefix",_.prefix),
        field("sensorId",_.sensorId)
        ).mapN(QRSComplexEventKey)
    }

  implicit val valueDeserializer:RecordSerializer[F,QRSComplexEvent]
  =avroSerializer[QRSComplexEvent].using(MyAvroSettings.getAvroSettings[F])

  implicit val keyDeserializer:RecordSerializer[F,QRSComplexEventKey] =
    avroSerializer[QRSComplexEventKey].using(MyAvroSettings.getAvroSettings[F])


}

object VulcanSerializer {
  def apply[F[_]:Sync]() = new VulcanSerializer[F]
}
