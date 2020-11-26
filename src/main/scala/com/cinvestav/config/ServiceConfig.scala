package com.cinvestav.config
import pureconfig._
import pureconfig.generic.auto._
case class DatabaseConfig(uri:String,dbName:String)
case class ServiceConfig(
                        bootstrapServers:String,
                        consumerTopic:String,
                        producerTopic:String,
                        group:String,
                        db:DatabaseConfig
                        )
