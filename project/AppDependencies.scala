import sbt._

object AppDependencies {

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-27"       % "5.3.0",
    "uk.gov.hmrc"     %% "play-frontend-hmrc"               % "0.58.0-play-27",
    "uk.gov.hmrc"     %% "play-partials"                    % "8.0.0-play-27",
    "uk.gov.hmrc"     %% "agent-kenshoo-monitoring"         % "4.4.0",
    "uk.gov.hmrc"     %% "play-fsm"                         % "0.83.0-play-27",
    "uk.gov.hmrc"     %% "domain"                           % "5.11.0-play-27",
    "uk.gov.hmrc"     %% "mongo-caching"                    % "6.16.0-play-27",
    "uk.gov.hmrc"     %% "json-encryption"                  % "4.10.0-play-27"

  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"               %% "scalatest"            % "3.0.9",
    "org.mockito"                 %  "mockito-core"         % "3.5.15",
    "org.scalatestplus.play"      %% "scalatestplus-play"   % "4.0.3",
    "com.github.tomakehurst"      %  "wiremock"             % "2.27.2",
    "org.pegdown"                 %  "pegdown"              % "1.6.0"
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
