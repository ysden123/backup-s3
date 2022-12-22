/*
 * Copyright (c) 2022. StulSoft
 */

package com.stulsoft.backup

import com.typesafe.scalalogging.StrictLogging

import java.net.URL
import java.util.jar.Manifest
import scala.annotation.tailrec

case class ManifestInfo(groupId:String, artifactId:String) extends StrictLogging:
  private val jar = s"""(.*)$groupId.$artifactId-(.*).jar(.*)""".r

  @tailrec
  private def defineUrl(resources: java.util.Enumeration[URL]): Option[URL] =
    if !resources.hasMoreElements then
      None
    else
      val url = resources.nextElement()
      val urlOption: Option[URL] = url.getPath match
        case jar(_,_,_) =>
          Option(url)
        case _ => None

      if urlOption.isDefined then
        urlOption
        else
        defineUrl(resources)

  private def resourceWithManifest(): Option[URL] =
    val resources = classOf[ManifestInfo]
      .getClassLoader
      .getResources("META-INF/MANIFEST.MF")

    defineUrl(resources)

  private def manifestAttrValue(manifest: Manifest, key: String): String =
    manifest.getMainAttributes.getValue(key) match
      case v:String => v
      case null => ""

  def showManifest(): Unit =
    try
      resourceWithManifest().foreach(url =>
        val is = url.openStream()
        val manifest = new Manifest(is)
        val title = manifestAttrValue(manifest, "Implementation-Title")
        val version = manifestAttrValue(manifest, "Implementation-Version")
        val date = manifestAttrValue(manifest, "Build-Date")
        val text = s"$title, $version, $date"
        println(text)
        logger.info(text)
      )
    catch
      case exception: Exception => exception.printStackTrace()
