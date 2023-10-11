/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup

import com.typesafe.scalalogging.StrictLogging
import os.Path

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.sys.process._
import scala.util.{Failure, Success, Try}

case class VersionService(destinationDirectory: String, maxBackupDirectories: Int) extends StrictLogging:
  def buildOutputDirectoryName(): Path =
    if findNumberOfExistingBackups() >= maxBackupDirectories then
      findOldestBackupDirectories()
        .foreach(seq =>
          seq.foreach(p =>
            try
              val msg = s"Going to delete $p"
              println(msg)
              logger.info(msg)
              val command = Seq("cmd", "/c", "rmdir",
                s""""${p.toString}"""",
                "/s", "/q")
              logger.info("Running {}", command.mkString(" "))
              val result = command.!!
              logger.info("Result: {}", result)
            catch
              case exception: Exception => logger.error(exception.getMessage, exception)
          )
        )

    Path(s"$destinationDirectory/${buildOutputBackupDirectoryName()}")

  private def findNumberOfExistingBackups(): Int =
    try
      os.list(Path(destinationDirectory)).count(p => os.isDir(p))
    catch
      case _ => 0

  private def findOldestBackupDirectories(): Option[Seq[Path]] =
    val dirs = os.list(Path(destinationDirectory))
    Try(dirs.filter(p => os.isDir(p)).sortBy(p => os.mtime(p))) match
      case Success(sorted) => Some(sorted.take(sorted.size - (maxBackupDirectories - 1)))
      case Failure(_) => None

  private def buildOutputBackupDirectoryName(): String =
    val calendar = Calendar.getInstance()
    val formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
    formatter.format(calendar.getTimeInMillis)