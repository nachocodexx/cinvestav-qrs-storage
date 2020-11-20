
lazy val ReactiveMongoDB = "org.mongodb" % "mongodb-driver-reactivestreams" %"4.0.5"
lazy val fs2Version = "2.4.4"
lazy val Fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.1.0"
lazy val Logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val MongoScalaDriver = "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.5"

lazy val Fs2= Seq(
 "co.fs2" %% "fs2-core" ,
// optional I/O library
 "co.fs2" %% "fs2-io" ,
// optional reactive streams interop
 "co.fs2" %% "fs2-reactive-streams"
).map(_ % fs2Version)
lazy val Log4Cat = "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"

lazy val QRSDBRoot = (project in file("."))
  .settings(
    name := "cinvestav-qrs-db",
    version := "0.1",
    scalaVersion := "2.12.10",
    organization := "com.cinvestav",
    libraryDependencies ++=Seq(
      MongoScalaDriver,
      Logback,
      Fs2Kafka,
      Log4Cat
    )  ++ Fs2
  )
