import uk.gov.hmrc.DefaultBuildSettings

import scala.collection.immutable.Seq

val appName = "home-office-immigration-status-frontend"

ThisBuild / scalaVersion := "3.5.1"
ThisBuild / majorVersion := 0

val commonSettings: Seq[String] = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:noAutoTupling",
  "-Wvalue-discard",
  "-Werror",
  "-Wconf:src=routes/.*:s",
  "-Wconf:src=views/.*txt.*:s",
  "-Wconf:msg=Flag.*repeatedly:s",
  "-Wunused:unsafe-warn-patvars"
)

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
    scalacOptions ++= commonSettings
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings(), scalacOptions ++= commonSettings)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
