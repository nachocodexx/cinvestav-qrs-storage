package com.cinvestav

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Async, ExitCode, IO, IOApp}
import fs2.{Pipe, Stream}
import fs2.kafka.commitBatchWithin

import scala.concurrent.duration._
import scala.language.postfixOps
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

object QRSDBMain extends IOApp {
  val DATABASE_NAME: String = "sensors"
  val mongoClient:MongoClient = MongoClient("mongodb://localhost:27018")
  val db:MongoDatabase = mongoClient.getDatabase(DATABASE_NAME)
  val sensorCollection: MongoCollection[Document] = db.getCollection(DATABASE_NAME)

  override def run(args: List[String]): IO[ExitCode] ={


    def program() = {
      QRSKafkaConsumer
        .stream()
        .mapAsync(25){
          committable=>
             QRSMongoDB
              .insertOne[IO](sensorCollection,Document("data"->"FAKEEEE!"))
              .as(committable.offset)
        }
        .through(commitBatchWithin(100,10.seconds))
    }


    program().compile.drain.attempt.map {
      case Left(value) =>
        ExitCode.Error
      case Right(value) =>
        ExitCode.Success
    }

  }

}
