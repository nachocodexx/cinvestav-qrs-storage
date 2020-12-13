
//lazy val Fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.1.0"
lazy val Logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val MongoScalaDriver = "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.5"
lazy val CatsEffect= "org.typelevel" %% "cats-core" % "2.1.1"

lazy val fs2Version = "2.4.4"
lazy val Fs2= "co.fs2" %% "fs2-core" % fs2Version
lazy val PureConfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"
//lazy val Vulcan = "com.github.fd4s" %% "vulcan" % "1.2.0"

lazy val fs2KafkaVersion = "1.1.0"
lazy val Fs2Kafka = Seq(
  "com.github.fd4s" %% "fs2-kafka",
  "com.github.fd4s" %% "fs2-kafka-vulcan",
).map(_%fs2KafkaVersion)

lazy val QRSDBRoot = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "cinvestav-qrs-db",
    version := "0.1",
    scalaVersion := "2.13.3",
    organization := "com.cinvestav",
    resolvers += "confluent" at "https://packages.confluent.io/maven/",
    libraryDependencies ++=Seq(
      MongoScalaDriver,
      Logback,
      PureConfig,
      Fs2,
      CatsEffect
    )++ Fs2Kafka
  )
lazy val dockerPorts = sys.env.getOrElse("PORT",9092).asInstanceOf[Int]
dockerExposedPorts := Seq(dockerPorts,9092)
dockerRepository := Some("nachocode")
version in Docker := "latest"
