package com.cinvestav

import cats.data.Kleisli
import cats.effect.{Async, ContextShift, IO}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.result.{InsertManyResult, InsertOneResult}
import cats.effect.implicits._
import cats.implicits._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import com.cinvestav.domain.QRSComplexResult
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object QRSMongoDB {

  def insertMany[F[_]:Async:ContextShift](documents:Seq[QRSComplexResult])(implicit collection: MongoCollection[QRSComplexResult])
  :F[InsertManyResult] =
    Async[F].async {
      cb=>
        collection.insertMany(documents).toFuture().onComplete{
          case Failure(e) =>
            cb(Left(e))
          case Success(value)=> cb(Right(value))
        }
    }


}
