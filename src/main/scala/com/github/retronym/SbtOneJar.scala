package com.github.retronym

import sbt._
import Keys._
import java.util.jar.Attributes.Name._
import sbt.Defaults._
import sbt.Package.ManifestAttributes

// TODO Either fail if exportJars == false, or access the other project artifacts via BuildStructure
object SbtOneJar extends AutoPlugin {

  object autoImport {
    val oneJar = TaskKey[File]("one-jar", "Create a single executable JAR using One-JAR™")
    val oneJarRedist = TaskKey[Set[File]]("one-jar-redist", "The redistributable One-JAR™ launcher, unzipped.")

    @deprecated("will be removed. add `enablePlugins(SbtOneJar) in build.sbt`", "0.9")
    def oneJarSettings: Seq[Def.Setting[_]] = SbtOneJar.projectSettings
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override val projectSettings: Seq[Def.Setting[_]] = inTask(oneJar)(Seq(
    artifactPath := artifactPathSetting(artifact).value
  )) ++ Seq(
    publishArtifact in oneJar := publishMavenStyle.value,
    artifact in oneJar := moduleName(Artifact(_, "one-jar")).value,
    packageOptions in oneJar := Seq(ManifestAttributes((MAIN_CLASS, "com.simontuffs.onejar.Boot"))),
    mainClass in oneJar := (mainClass in run in Compile).value,
    packageOptions in oneJar ++= (mainClass in oneJar).map {
      case Some(mainClass) => Seq(ManifestAttributes(("One-Jar-Main-Class", mainClass)))
      case _ => Seq()
    }.value,
    baseDirectory in oneJarRedist := (target.value / "one-jar-redist"),
    oneJarRedist := {
      val base = (baseDirectory in oneJarRedist).value
      val oneJarResourceName = "one-jar-boot-0.97.jar"
      val s = getClass.getClassLoader.getResourceAsStream(oneJarResourceName)
      if (s == null) sys.error("could not load: " + oneJarResourceName)
      def include(path: String) = path match {
        case "META-INF/MANIFEST.MF" => false
        case x => !x.startsWith("src/")
      }
      IO.unzipStream(s, base, include _)
    },
    mappings in oneJar := {
      val artifact = (packageBin in Compile).value
      val classpath = (dependencyClasspath in Runtime).value
      val oneJarRedistBase = (baseDirectory in oneJarRedist).value
      val thisArtifactMapping = (artifact, (file("main") / artifact.name).getPath)
      val deps: Seq[(File, String)] = {
        val allDeps = Attributed.data(classpath).map(f => (f, (file("lib") / f.name).getPath))
        allDeps.filterNot(_._1 == artifact)
      }

      val redist = oneJarRedist.value.toSeq pair Path.relativeTo(oneJarRedistBase)
      Seq(thisArtifactMapping) ++ deps ++ redist
    },
    oneJar := {
      val output = (artifactPath in oneJar).value
      val packageConf = new Package.Configuration((mappings in oneJar).value, output, (packageOptions in oneJar).value)
      Package(packageConf, Compat.cacheStoreFactory(streams.value.cacheDirectory), streams.value.log)
      output
    }
  )
}
