import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.*
import scoverage.ScoverageKeys

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt IntegrationTest/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle IntegrationTest/scalastyle")

lazy val root = (project in file("."))
  .settings(
    name := "home-office-immigration-status-frontend",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.10",
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always),
    PlayKeys.playDefaultPort := 10210,
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    libraryDependencies ++= AppDependencies(),
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*BuildInfo;.*Routes;.*RoutesPrefix;.*Filters?;MicroserviceAuditConnector;" +
      "Module;GraphiteStartUp;Reverse.*",
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    majorVersion := 0,
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
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value)
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .enablePlugins(PlayScala, SbtDistributablesPlugin)

scalacOptions ++= Seq(
  "-Wconf:cat=unused-imports&site=.*views.html.*:s",
  "-Wconf:src=routes/.*:s",
  "-feature"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests.map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
