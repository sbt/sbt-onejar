import sbt._
import Keys._

object build extends Build {
  val sbtXjc = Project(
    id = "sbt-onejar",
    base = file("."),
    settings = Defaults.defaultSettings ++ ScriptedPlugin.scriptedSettings ++ Seq[Project.Setting[_]](
      organization := "com.github.retronym",
      version := "0.8-SNAPSHOT",
      sbtPlugin := true,
      scalacOptions in Compile ++= Seq("-deprecation"),
      publishTo := Some(Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)),
      publishMavenStyle := false
    )
  )
}