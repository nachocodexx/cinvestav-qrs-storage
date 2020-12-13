package com.cinvestav
import cats.implicits._
import cats.effect.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import scala.concurrent.duration._
import scala.language.postfixOps
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import com.cinvestav.config.ServiceConfig
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import com.cinvestav.domain.QRSComplexResult


object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] ={
    val config = ConfigSource.default.load[ServiceConfig]
    config match {
      case Left(value) =>
        println(s"Configuration error: ${value.head.description}")
        IO.unit.as(ExitCode.Error)
      case Right(value) =>
        val mongoClient = MongoClient(value.db.uri)
        val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[QRSComplexResult]), DEFAULT_CODEC_REGISTRY)
        val db: MongoDatabase = mongoClient.getDatabase(value.db.dbName).withCodecRegistry(codecRegistry)
        implicit val collection: MongoCollection[QRSComplexResult] = db.getCollection("qrs_results")
        implicit val dbProducer: DBProducer[IO] = DBProducer[IO](value.bootstrapServers)
        implicit val config: ServiceConfig = value
        val consumer = DBConsumer[IO](value.bootstrapServers, value.consumerTopic, value.groupId)
        consumer.run().compile.drain.as(ExitCode.Success)
    }
  }

}
