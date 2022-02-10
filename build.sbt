import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*BuildInfo;.*Routes;.*RoutesPrefix;.*Filters?;MicroserviceAuditConnector;" +
      "Module;GraphiteStartUp;Reverse.*",
    ScoverageKeys.coverageMinimum := 90.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val root = (project in file("."))
  .settings(
    name := "home-office-immigration-status-frontend",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 10210,
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    resolvers := Seq(
      Resolver.jcenterRepo,
      Resolver.typesafeRepo("releases"),
      "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
    ),
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides ++= AppDependencies.jettyOverrides,
    publishingSettings,
    scoverageSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    majorVersion := 0,
    Concat.groups := Seq(
      "javascripts/immigrationstatus-app.js" ->
        group(Seq(
          "javascripts/jquery-3.6.0.min.js",
          "javascripts/libraries/location-autocomplete.min.js",
          "javascripts/autocomplete.js",
          "javascripts/ga-events.js"
        ))
    ),
    pipelineStages in Assets := Seq(concat)
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    scalafmtOnCompile in IntegrationTest := true
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .enablePlugins(PlayScala, SbtDistributablesPlugin)

inConfig(IntegrationTest)(scalafmtCoreSettings)

scalacOptions ++= Seq(
  "-P:silencer:globalFilters=Unused import",
  "-feature"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
  tests.map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
