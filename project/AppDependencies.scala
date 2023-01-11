import sbt._

object AppDependencies {

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % "7.12.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "3.34.0-play-28",
    "uk.gov.hmrc"       %% "play-partials"              % "8.3.0-play-28",
    "uk.gov.hmrc"       %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % "0.74.0",
    "uk.gov.hmrc"       %% "json-encryption"            % "5.1.0-play-28"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"                %% "scalatest"            % "3.2.15",
    "org.mockito"                   % "mockito-core"         % "4.11.0",
    "org.scalatestplus"            %% "scalacheck-1-17"      % "3.2.15.0",
    "org.scalatestplus.play"       %% "scalatestplus-play"   % "5.1.0",
    "com.github.tomakehurst"        % "wiremock-jre8"        % "2.35.0",
    "com.vladsch.flexmark"          % "flexmark-all"         % "0.62.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.1"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
