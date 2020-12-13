package com.cinvestav.avro.deserializers

import cats.effect.Sync
import cats.implicits._
import com.cinvestav.avro.MyAvroSettings
import com.cinvestav.domain.{QRSComplexKey, QRSComplexResult}
import fs2.kafka.RecordDeserializer
import fs2.kafka.vulcan.avroDeserializer
import vulcan.Codec

class VulcanDeserializer[F[_]:Sync]{
  implicit val qrsComplexResult:Codec[QRSComplexResult] =
    Codec.record(
      name = "QRSComplexResult",
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
      ).mapN(QRSComplexResult)
    }

  implicit val qrsComplexKey:Codec[QRSComplexKey] =
    Codec.record(
      name = "QRSComplexKey",
      namespace = "com.cinvestav"
    ){ field=>
      (
        field("prefix",_.prefix),
        field("sensorId",_.sensorId)
      ).mapN(QRSComplexKey)
    }

  implicit val valueDeserializer:RecordDeserializer[F,QRSComplexResult]
  =avroDeserializer[QRSComplexResult].using(MyAvroSettings.getAvroSettings[F])

  implicit val keyDeserializer:RecordDeserializer[F,QRSComplexKey] =
    avroDeserializer[QRSComplexKey].using(MyAvroSettings.getAvroSettings[F])


}


object VulcanDeserializer{
  def apply[F[_]:Sync]()=new VulcanDeserializer[F]
}

