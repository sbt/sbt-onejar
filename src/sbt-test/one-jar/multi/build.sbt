def standardSettings = Seq(
  exportJars := true
)

lazy val alpha = Project("alpha", file("alpha"))
  .settings(standardSettings)

lazy val beta = Project("beta", file("beta"))
  .dependsOn(alpha)
  .enablePlugins(SbtOneJar)
  .settings(
    standardSettings
  )
