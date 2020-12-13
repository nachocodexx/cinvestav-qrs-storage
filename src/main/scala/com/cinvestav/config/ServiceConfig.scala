package com.cinvestav.config
import pureconfig._
import pureconfig.generic.auto._
case class DatabaseConfig(uri:String,dbName:String)
case class ServiceConfig(
                        sensorId:String,
                        bootstrapServers:String,
                        consumerTopic:String,
                        producerTopic:String,
                        groupId:String,
                        db:DatabaseConfig
                        )
