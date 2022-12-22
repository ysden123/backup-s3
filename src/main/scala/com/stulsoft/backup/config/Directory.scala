/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup.config

case class Directory(name:String,
                     source:String,
                     destination:String,
                     maxBackupDirectories:Int,
                     directoriesToSkip:Option[Set[String]])
