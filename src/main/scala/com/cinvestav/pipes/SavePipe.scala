package com.cinvestav.pipes

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Async, ContextShift, IO}
import com.cinvestav.QRSMongoDB
import fs2.{Chunk, Pipe}
import com.cinvestav.domain.SensorMeasurement
import org.mongodb.scala.MongoCollection

object SavePipe {

  def apply[F[_]:Async:ContextShift]()(implicit collection:MongoCollection[SensorMeasurement]):Pipe[F,
    Chunk[SensorMeasurement],
    List[SensorMeasurement]] =
    in=>in
      .map(_.toList)
      .evalMap(x=> QRSMongoDB.insertMany[F](x)>>x.pure[F])


}
