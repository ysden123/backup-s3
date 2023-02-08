/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup

import com.stulsoft.backup.config.Directory
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import os.Path
import os.PathChunk.StringPathChunk

case class CopyDirectories(directories: Set[Directory]) extends StrictLogging:
  private val outputInterval = 100

  def makeCopy(): Unit =
    directories.foreach(directory =>
      val versionService = VersionService(directory.destination, directory.maxBackupDirectories)
      val odn = versionService.buildOutputDirectoryName()
      logger.debug("odn={}", odn)
      logger.info("Copying {}", directory.name)
      println(s"Copying ${directory.name}")
      logger.info("   source      : {}", directory.source)
      println(s"   source      : ${directory.source}")
      logger.info("   destination : {}", directory.destination)
      println(s"   destination : ${directory.destination}")
      val start = System.currentTimeMillis()
      val directoriesToSkip = directory.directoriesToSkip match
        case Some(set) => buildDirectoriesToSkip(Path(directory.destination), set)
        case _ => Set[Path]()

      var fileCount = 0
      var outputCount = 0

      val filesForCopying = os.walk(Path(directory.source))
        .filter(p => os.isFile(p) && !directoriesToSkip.exists(skipPath => p.startsWith(skipPath)))
      val totalSize = filesForCopying.size
      filesForCopying.foreach(p =>
        val destinationPath = p.segments
          .toList
          .tail
          .map(s => StringPathChunk(s))
          .foldLeft(odn) { (acc, b) => acc / b }
        logger.whenDebugEnabled(logger.debug("Copying {} to {}", p, destinationPath))
        try
          os.copy(p, destinationPath, createFolders = true, replaceExisting = true)
          fileCount += 1
          outputCount += 1
          if outputCount >= outputInterval then
            val duration = DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss,SSS")
            printf("\rCopied %d files (%d%%) in %s", fileCount, fileCount * 100 / totalSize, duration)
            outputCount = 0
        catch
          case exception: Exception => logger.error(exception.getMessage, exception)
      )

      val duration = DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss,SSS")
      val msg1 = s"${directory.source} copied in $duration"
      val msg2 = s"Total handled $fileCount files."
      println()
      println(msg1)
      println(msg2)
      println()
      logger.info(msg1)
      logger.info(msg2)
    )

  private def buildDirectoriesToSkip(wd: Path, set: Set[String]): Set[Path] =
    set.map(sd => wd / StringPathChunk(sd))