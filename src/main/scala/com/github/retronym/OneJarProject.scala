package com.github.retronym

import sbt._
import java.util.jar.Attributes.Name._
import java.lang.String

trait OneJarProject extends DefaultProject{

  override def classpathFilter = super.classpathFilter -- "*-sources.jar" -- "*-javadoc.jar"

  def onejarJarName : String       = name + "-onejar-" + this.version + ".jar"
  def onejarOutputPath : Path      = outputPath / onejarJarName
  def onejarTemporaryPath : Path   = outputPath / "onejar"
  def onejarClasspath : PathFinder = runClasspath
  def onejarExtraJars : PathFinder = mainDependencies.scalaJars
  val oneJarResourceName: String   = "one-jar-boot-0.97.jar"

  def onejarPackageOptions: Seq[PackageOption] = List(ManifestAttributes(
        (MAIN_CLASS, "com.simontuffs.onejar.Boot")
      ))

  lazy val onejar = onejarTask(onejarTemporaryPath,
    onejarClasspath,
    onejarExtraJars
  ) dependsOn (`package`) describedAs ("Builds an optimized, single-file deployable JAR.")

  def onejarTask(tempDir: Path, classpath: PathFinder, extraJars: PathFinder) =
    packageTask(Path.lazyPathFinder(onejarPaths(tempDir, classpath, extraJars)), onejarOutputPath, onejarPackageOptions)

  def onejarPaths(tempDir: Path, classpath: PathFinder, extraJars: PathFinder) = {
    import xsbt.FileUtilities._

    val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)

    // Unpack One-Jar itself, which is a classpath resource of this Plugin
    val oneJarResourceStream = {
      val s = getClass.getClassLoader.getResourceAsStream(oneJarResourceName)
      if (s == null) error("could not load: " + oneJarResourceName)
      s
    }
    val notManifest: xsbt.NameFilter = -(new xsbt.ExactFilter("META-INF/MANIFEST.MF"))
    unzip(oneJarResourceStream, tempDir.asFile, notManifest)

    // Copy the JAR for this module to "main"
    FileUtilities.copy(List(jarPath), tempDir / "main", log)

    // Copy all dependencies to "lib"
    FileUtilities.copy(libs ++ extraJars.get, tempDir / "lib", log)

    // Return the paths that will be added to the -onejar.jar
    val base = (Path.lazyPathFinder(tempDir :: Nil /*:: directories*/) ##)
    descendents(base, "*").get
  }
}