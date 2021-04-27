import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimum := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val compileDeps = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "4.2.0",
  "uk.gov.hmrc" %% "govuk-template" % "5.65.0-play-27",
  "uk.gov.hmrc" %% "play-ui" % "9.1.0-play-27",
  "uk.gov.hmrc" %% "auth-client" % "5.2.0-play-27",
  "uk.gov.hmrc" %% "play-partials" % "8.0.0-play-27",
  "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.4.0",
  "uk.gov.hmrc" %% "play-fsm" % "0.83.0-play-27",
  "uk.gov.hmrc" %% "domain" % "5.11.0-play-27",
  "uk.gov.hmrc" %% "mongo-caching" % "6.16.0-play-27",
  "uk.gov.hmrc" %% "json-encryption"  % "4.10.0-play-27"
)

def testDeps(scope: String) = Seq(
  "org.scalatest" %% "scalatest" % "3.0.9" % scope,
  "org.mockito" % "mockito-core" % "3.5.15" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope,
  "com.github.tomakehurst" % "wiremock" % "2.27.2" % scope,
  "org.pegdown" % "pegdown" % "1.6.0"
)

val jettyVersion = "9.2.24.v20180105"

val jettyOverrides = Seq(
  "org.eclipse.jetty" % "jetty-server" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-security" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-xml" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-client" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-http" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-io" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty" % "jetty-util" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % IntegrationTest
)

lazy val root = (project in file("."))
  .settings(
    name := "home-office-settled-status-frontend",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 9386,
    resolvers := Seq(
      Resolver.jcenterRepo,
      Resolver.typesafeRepo("releases"),
      "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    dependencyOverrides ++= jettyOverrides,
    publishingSettings,
    scoverageSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    majorVersion := 0
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
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)

inConfig(IntegrationTest)(scalafmtCoreSettings)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
