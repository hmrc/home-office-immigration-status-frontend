import uk.gov.hmrc.DefaultBuildSettings

val appName = "home-office-immigration-status-frontend"

ThisBuild / scalaVersion := "3.5.1"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 10210,
    libraryDependencies ++= AppDependencies()
  )
  .settings(CodeCoverageSettings.settings)
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    scalacOptions ++= List(
      "-Wconf:cat=unused-imports&src=views/.*:s",
      "-Wconf:src=routes/.*:s",
      "-feature"
    )
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
