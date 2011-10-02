package com.github.retronym

import sbt._
import Keys._
import java.util.jar.Attributes.Name._
import sbt.Defaults._
import sbt.Package.ManifestAttributes

// TODO Either fail if exportJars == false, or access the other project artifacts via BuildStructure
object SbtOneJar extends Plugin {
  val oneJar = TaskKey[File]("one-jar", "Create a single executable JAR using One-JAR™")
  val oneJarRedist = TaskKey[Set[File]]("one-jar-redist", "The redistributable One-JAR™ launcher, unzipped.")

  val oneJarSettings: Seq[Project.Setting[_]] = inTask(oneJar)(Seq(
    artifactPath <<= artifactPathSetting(artifact),
    cacheDirectory <<= cacheDirectory / oneJar.key.label
  )) ++ Seq(
    publishArtifact in oneJar <<= publishMavenStyle,
    artifact in oneJar <<= moduleName(Artifact(_, "one-jar")),
    packageOptions in oneJar := Seq(ManifestAttributes((MAIN_CLASS, "com.simontuffs.onejar.Boot"))),
    mainClass in oneJar <<= mainClass in run in Compile,
    packageOptions in oneJar <++= (mainClass in oneJar).map {
      case Some(mainClass) => Seq(ManifestAttributes(("One-Jar-Main-Class", mainClass)))
      case _ => Seq()
    },
    baseDirectory in oneJarRedist <<= (target)(_ / "one-jar-redist"),
    oneJarRedist <<= (baseDirectory in oneJarRedist).map { (base) =>
      val oneJarResourceName = "one-jar-boot-0.97.jar"
      val s = getClass.getClassLoader.getResourceAsStream(oneJarResourceName)
      if (s == null) sys.error("could not load: " + oneJarResourceName)
      def include(path: String) = path match {
        case "META-INF/MANIFEST.MF" => false
        case x => !x.startsWith("src/")
      }
      IO.unzipStream(s, base, include _)
    },
    mappings in oneJar <<= (packageBin in Compile, dependencyClasspath in Runtime,
            oneJarRedist, baseDirectory in oneJarRedist).map {
      (artifact, classpath, oneJarRedist, oneJarRedistBase) =>
        val thisArtifactMapping = (artifact, (file("main") / artifact.name).getPath)
        val deps: Seq[(File, String)] = {
          val allDeps = Build.data(classpath).map(f => (f, (file("lib") / f.name).getPath))
          allDeps.filterNot(_._1 == artifact)
        }

        val redist = oneJarRedist.toSeq x relativeTo(oneJarRedistBase)
        Seq(thisArtifactMapping) ++ deps ++ redist
    },
    oneJar <<= (mappings in oneJar, artifactPath in oneJar, packageOptions in oneJar, cacheDirectory in oneJar, streams) map {
      (mappings, output, packOpts, cacheDir, s) =>
        val packageConf = new Package.Configuration(mappings, output, packOpts)
        Package(packageConf, cacheDir, s.log)
        output
    }
  )
}
