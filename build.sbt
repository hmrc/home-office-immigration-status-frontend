import uk.gov.hmrc.DefaultBuildSettings

val appName = "home-office-immigration-status-frontend"

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
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
    Concat.groups := Seq(
      "javascripts/immigrationstatus-app.js" ->
        group(
          Seq(
            "javascripts/jquery-3.6.0.min.js",
            "javascripts/libraries/location-autocomplete.min.js",
            "javascripts/autocomplete.js",
            "javascripts/ga-events.js"
          )
        )
    ),
    Assets / pipelineStages := Seq(concat)
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused-imports&src=views/.*:s",
      "-Wconf:src=routes/.*:s",
      "-feature"
    )
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt A11y/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle A11y/scalastyle")
