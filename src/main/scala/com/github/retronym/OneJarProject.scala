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
  ) dependsOn (`package`) describedAs ("Builds a single-file, executable JAR using One-JAR")

  def onejarTask(tempDir: Path, classpath: PathFinder, extraJars: PathFinder) =
    packageTask(Path.lazyPathFinder(onejarPaths(tempDir, classpath, extraJars)), onejarOutputPath, onejarPackageOptions)

  def onejarPaths(tempDir: Path, classpath: PathFinder, extraJars: PathFinder) = {
    import xsbt.FileUtilities._
    FileUtilities.clean(tempDir, log)

    val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)

    // Unpack One-Jar itself, which is a classpath resource of this Plugin
    val oneJarResourceStream = {
      val s = this.getClass.getClassLoader.getResourceAsStream(oneJarResourceName)
      if (s == null) error("could not load: " + oneJarResourceName)
      s
    }
    val notManifest: xsbt.NameFilter = -(new xsbt.ExactFilter("META-INF/MANIFEST.MF"))
    unzip(oneJarResourceStream, tempDir.asFile, notManifest)

    val tempMainPath = tempDir / "main"
    val tempLibPath = tempDir / "lib"
    Seq(tempLibPath, tempMainPath).foreach(_.asFile.mkdirs)

    // Copy all dependencies to "lib"
    val otherProjectJars = topologicalSort.flatMap {
      case x: BasicPackagePaths => List(x.jarPath)
      case _ => Nil
    }
    val libPaths: List[Path] = libs ++ extraJars.get ++ otherProjectJars

    getOrThrow(FileUtilities.copyFlat(List(jarPath), tempMainPath, log))
    getOrThrow(FileUtilities.copyFlat(libPaths, tempLibPath, log))

    // Return the paths that will be added to the -onejar.jar
    descendents(tempDir ##, "*").get
  }

  def getOrThrow[X](result: Either[String, X]): X =
    result match {
      case Left(s) => error(s)
      case Right(x) => x
    }
}