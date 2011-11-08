import sbt._
import Keys._

object build extends Build {
  val sbtXjc = Project(
    id = "sbt-onejar",
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq[Project.Setting[_]](
      organization := "com.github.retronym",
      version := "0.7-SNAPSHOT",
      sbtPlugin := true,
      scalacOptions in Compile ++= Seq("-deprecation"),
      publishTo <<= (version) { (v: String) =>
        val repoSuffix = if (v.contains("-SNAPSHOT")) "snapshots" else "releases"
        val resolver = Resolver.file("gh-pages", new File("/Users/jason/code/retronym.github.com/repo", repoSuffix))
        Some(resolver)
      }
    )
  )
}