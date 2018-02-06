name := "sbt-onejar"

crossSbtVersions := Seq("0.13.17", "1.1.1")

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts += ("-Dplugin.version=" + version.value)

organization := "org.scala-sbt.plugins"

version := "0.9-SNAPSHOT"

sbtPlugin := true

scalacOptions in Compile ++= Seq("-deprecation")

publishTo := Some(Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

publishMavenStyle := false
