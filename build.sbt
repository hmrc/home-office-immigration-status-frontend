import uk.gov.hmrc.DefaultBuildSettings.*

val appName = "home-office-immigration-status-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.11",
    PlayKeys.playDefaultPort := 10210
  )
  .settings(
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    // To resolve dependency clash between flexmark v0.64.4+ and play-language to run accessibility tests, remove when versions align
    dependencyOverrides += "com.ibm.icu" % "icu4j" % "69.1",
    libraryDependencies ++= AppDependencies()
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    )
  )
  .settings(
    coverageExcludedPackages := "<empty>;.*Routes.*",
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
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
  .configs(IntegrationTest)
  .settings(integrationTestSettings())
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused-imports&src=views/.*:s",
      "-Wconf:src=routes/.*:s",
      "-feature"
    )
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt IntegrationTest/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle IntegrationTest/scalastyle")
