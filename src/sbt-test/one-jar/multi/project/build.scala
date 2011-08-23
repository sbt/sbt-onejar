import com.github.retronym.SbtOneJar
import sbt._
import Keys._

object build extends Build {
  def standardSettings = Seq(
    exportJars := true
  ) ++ Defaults.defaultSettings

  lazy val multi  = Project("multi", file("."), aggregate = Seq(alpha, beta))
  lazy val alpha = Project("alpha", file("alpha"), settings = standardSettings)
  lazy val beta = Project("beta",
    file("beta"),
    dependencies = Seq(alpha),
    settings = standardSettings ++ SbtOneJar.oneJarSettings
  )
}