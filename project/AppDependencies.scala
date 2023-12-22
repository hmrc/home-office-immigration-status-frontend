import sbt.*

object AppDependencies {

  private val hmrcMongoVersion = "1.6.0"
  private val bootstrapVersion = "7.23.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "7.29.0-play-28",
    "uk.gov.hmrc"       %% "domain"                     % "8.3.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "crypto-json-play-28"        % "7.6.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.17",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.30",
    "org.scalatestplus"   %% "scalacheck-1-17"         % "3.2.17.0",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
