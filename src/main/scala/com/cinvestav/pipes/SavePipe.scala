package com.cinvestav.pipes

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Async, ContextShift, IO}
import com.cinvestav.QRSMongoDB
import fs2.{Chunk, Pipe}
import com.cinvestav.domain.QRSComplexResult
import org.mongodb.scala.MongoCollection

object SavePipe {

  def apply[F[_]:Async:ContextShift]()(implicit collection:MongoCollection[QRSComplexResult]):Pipe[F,
    Chunk[QRSComplexResult],
    List[QRSComplexResult]] =
    in=>in
      .map(_.toList)
      .evalMap(x=> QRSMongoDB.insertMany[F](x)>>x.pure[F])


}
