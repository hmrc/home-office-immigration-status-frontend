import sbt._

object AppDependencies {

  val silencerVersion = "1.7.9"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % "7.3.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "3.24.0-play-28",
    "uk.gov.hmrc"       %% "play-partials"              % "8.3.0-play-28",
    "uk.gov.hmrc"       %% "agent-kenshoo-monitoring"   % "4.8.0-play-28",
    "uk.gov.hmrc"       %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % "0.72.0",
    "uk.gov.hmrc"       %% "json-encryption"            % "5.1.0-play-28",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"                %% "scalatest"            % "3.2.13",
    "org.mockito"                   % "mockito-core"         % "4.8.0",
    "org.scalatestplus"            %% "scalacheck-1-16"      % "3.2.13.0",
    "org.scalatestplus.play"       %% "scalatestplus-play"   % "5.1.0",
    "com.github.tomakehurst"        % "wiremock-jre8"        % "2.33.2",
    "com.vladsch.flexmark"          % "flexmark-all"         % "0.62.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
