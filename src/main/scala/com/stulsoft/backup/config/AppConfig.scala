/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup.config

import com.typesafe.scalalogging.StrictLogging
import org.json4s.{DefaultFormats, JArray, JValue}
import org.json4s.jackson.JsonMethods.*
import org.json4s.jvalue2extractable

import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import scala.io.Source
import scala.util.{Failure, Success, Using}

case class AppConfig(directories: Set[Directory])

object AppConfig extends StrictLogging:
  def build(): AppConfig =
    implicit val formats: DefaultFormats = DefaultFormats
    try {
      val jsonObject: JValue =
        if getClass.getResource("AppConfig.class").toString.startsWith("file:") then
          logger.info("Getting configuration from the resource")
          parse(Source.fromResource("configuration.json").getLines().mkString)
        else
          val configPath = s"${System.getenv("APPDATA")}\\backup-s3\\configuration.json"
          logger.info("Getting configuration from {}", configPath)
          Using(Source.fromFile(configPath)) {
            source => parse(source.getLines().mkString)
          } match {
            case Success(jValue) => jValue
            case Failure(exception) => exception match {
              case _@(_: NoSuchFileException | _: FileNotFoundException) =>
                logger.error(s"Cannot find $configPath")
                JArray(Nil)
              case x =>
                x.printStackTrace()
                JArray(Nil)
            }
          }
      val directories = jsonObject.children.map(directory => directory.extract[Directory]).toSet
      AppConfig(directories)
    } catch {
      case e: Exception =>
        logger.error(s"sError: ${e.getMessage}", e)
        AppConfig(Set())
    }
