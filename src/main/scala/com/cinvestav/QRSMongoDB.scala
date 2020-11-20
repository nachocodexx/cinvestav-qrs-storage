package com.cinvestav

import cats.data.Kleisli
import cats.effect.{Async, ContextShift, IO}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.result.InsertOneResult
import cats.effect.implicits._
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object QRSMongoDB {

  def insertOne[F[_]:Async:ContextShift](collection: MongoCollection[Document],x:Document):F[InsertOneResult]=
    Async[F].async{
      cb=>
        collection.insertOne(x).toFuture().onComplete {
          case Failure(exception) =>cb(Left(exception))
          case Success(value) => cb(Right(value))
        }
    }


}
