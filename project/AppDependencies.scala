import sbt._

object AppDependencies {

  val silencerVersion = "1.7.1"

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"       % "5.16.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"               % "1.26.0-play-28",
    "uk.gov.hmrc"       %% "play-partials"                    % "8.2.0-play-28",
    "uk.gov.hmrc"       %% "agent-kenshoo-monitoring"         % "4.8.0-play-28",
    "uk.gov.hmrc"       %% "domain"                           % "6.2.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"               % "0.58.0",
    "uk.gov.hmrc"       %% "json-encryption"                  % "4.10.0-play-28",
     compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"               %% "scalatest"                % "3.2.9",
    "org.mockito"                 %  "mockito-core"             % "3.5.15",
    "org.scalatestplus"           %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.scalacheck"              %% "scalacheck"               % "1.14.3",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "com.github.tomakehurst"      %  "wiremock"                 % "2.27.2",
    "com.vladsch.flexmark"        % "flexmark-all"              % "0.35.10",
    "org.mockito"                 % "mockito-all"               % "1.10.19",
    "org.pegdown"                 %  "pegdown"                  % "1.6.0",
    "com.ironcorelabs"            %% "cats-scalatest"           % "3.1.1",
  ).map(_ % "test, it")


  private val jettyVersion = "9.2.24.v20180105"

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

  def apply(): Seq[ModuleID] = compile ++ test
}
