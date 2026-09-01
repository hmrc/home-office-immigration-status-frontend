import sbt.*

object AppDependencies {

  private val hmrcMongoVersion  = "2.13.0"
  private val bootstrapVersion  = "10.8.0"
  private val domainPlayVersion = "13.0.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % "13.11.0",
    "uk.gov.hmrc"       %% "domain-play-30"             % domainPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "crypto-json-play-30"        % "8.4.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "domain-test-play-30"     % domainPlayVersion,
    "org.scalatestplus" %% "scalacheck-1-18"         % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
