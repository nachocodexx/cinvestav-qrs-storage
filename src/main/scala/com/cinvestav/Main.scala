package com.cinvestav

import cats.implicits._
import cats.effect.implicits._
import cats.effect.{Async, ExitCode, IO, IOApp}
import fs2.{Chunk, Pipe, Stream}
import fs2.kafka.{ProducerRecord, ProducerRecords, commitBatchWithin, produce}

import scala.concurrent.duration._
import scala.language.postfixOps
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

import com.cinvestav.DBProducer
import com.cinvestav.config.ServiceConfig
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import com.cinvestav.domain.SensorMeasurement

//case class SensorMeasurement(
//                              sensorId:String,
//                              measurement:Double,
//                              timestamp:Long,
//                              filtered_ecg:Double,
//                              differentiated_ecg:Double,
//                              squared_ecg:Double,
//                              integrated_ecg:Double,
//                              qrs_timestamp:Long
//                            )

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] ={
    val config = ConfigSource.default.load[ServiceConfig]
    config match {
      case Left(value) =>
        println(s"Configuration error: ${value.head.description}")
        IO.unit.as(ExitCode.Error)
      case Right(value) =>
        val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[SensorMeasurement]), DEFAULT_CODEC_REGISTRY )
        val mongoClient:MongoClient = MongoClient(value.db.uri)
        val db:MongoDatabase = mongoClient.getDatabase(value.db.dbName).withCodecRegistry(codecRegistry)
        implicit val collection: MongoCollection[SensorMeasurement] = db.getCollection(value.db.dbName)
        implicit val dbProducer: DBProducer[IO] = DBProducer[IO](value.bootstrapServers)
        val consumer = DBConsumer[IO](value.bootstrapServers,value.consumerTopic,value.group)
        consumer.run().compile.drain.as(ExitCode.Success)
    }
//    program2().compile.drain.attempt.map {
//      case Left(value) =>
//        ExitCode.Error
//      case Right(value) =>
//        ExitCode.Success
//    }

  }

}
