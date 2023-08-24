import sbt.*

object AppDependencies {

  private val bootstrapVersion = "7.21.0"

  private val compile: Seq[ModuleID] =
    Seq(
      play.sbt.PlayImport.ws,
      "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapVersion,
      "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "7.17.0-play-28",
      "uk.gov.hmrc"       %% "play-partials"              % "8.4.0-play-28",
      "uk.gov.hmrc"       %% "domain"                     % "8.3.0-play-28",
      "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % "1.3.0",
      "uk.gov.hmrc"       %% "json-encryption"            % "5.1.0-play-28"
    )

  private val test: Seq[ModuleID] =
    Seq(
      "uk.gov.hmrc"                  %% "bootstrap-test-play-28"  % bootstrapVersion,
      "org.scalatest"                %% "scalatest"               % "3.2.16",
      "org.mockito"                  %% "mockito-scala-scalatest" % "1.17.14",
      "org.scalatestplus"            %% "scalacheck-1-17"         % "3.2.16.0",
      "com.github.tomakehurst"        % "wiremock-jre8"           % "2.35.0",
      "com.vladsch.flexmark"          % "flexmark-all"            % "0.64.8",
      "com.fasterxml.jackson.module" %% "jackson-module-scala"    % "2.15.2"
    ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
