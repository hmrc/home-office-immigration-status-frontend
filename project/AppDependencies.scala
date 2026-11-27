import sbt.*

object AppDependencies {

  private val featureToggleVersion = "3.1.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"           % "13.16.0",
    "uk.gov.hmrc" %% "domain-play-30"                       % "13.0.0",
    "uk.gov.hmrc" %% "mongo-feature-toggles-client-play-30" % featureToggleVersion,
    "uk.gov.hmrc" %% "crypto-json-play-30"                  % "8.4.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-test-play-30" % featureToggleVersion,
    "uk.gov.hmrc"       %% "domain-test-play-30"                        % "13.0.0",
    "org.scalatestplus" %% "scalacheck-1-18"                            % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
