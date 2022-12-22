/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup

import com.stulsoft.backup.config.AppConfig
import com.typesafe.scalalogging.{Logger, StrictLogging}

object Main extends StrictLogging:
  def main(args: Array[String]): Unit =
    ManifestInfo("com.stulsoft", "backup-s3").showManifest()
    logger.info("Start backup-s3")
    val appConfig = AppConfig.build()
    logger.info("Processing following configuration:")
    logger.info(appConfig.toString)
    CopyDirectories(appConfig.directories).makeCopy()
