/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup

import com.typesafe.scalalogging.StrictLogging
import os.{Path, PathChunk}
import os.PathChunk.StringPathChunk

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.{Failure, Success, Try}

/*
import math.Fractional.Implicits.infixFractionalOps
import math.Integral.Implicits.infixIntegralOps
*/

case class VersionService(destinationDirectory: String, maxBackupDirectories: Int) extends StrictLogging:
  def buildOutputDirectoryName(): Path =
    if findNumberOfExistingBackups() >= maxBackupDirectories then
      findOldestBackupDirectory()
        .foreach(p =>
          try
            logger.info("Going to delete {}", p)
            os.remove.all(p)
          catch
            case exception: Exception => logger.error(exception.getMessage, exception)
        )

    Path(s"$destinationDirectory/${buildOutputBackupDirectoryName()}")

  private def findNumberOfExistingBackups(): Int =
    try
      os.list(Path(destinationDirectory)).count(p => os.isDir(p))
    catch
      case _ => 0

  private def findOldestBackupDirectory(): Option[Path] =
    Try(os
      .list(Path(destinationDirectory))
      .filter(p => os.isDir(p))
      .reduceLeft((p1, p2) => if os.mtime(p1) < os.mtime(p2) then p1 else p2)) match
      case Success(min) => Some(min)
      case Failure(_) => None

  private def buildOutputBackupDirectoryName(): String =
    val calendar = Calendar.getInstance()
    val formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
    formatter.format(calendar.getTimeInMillis)