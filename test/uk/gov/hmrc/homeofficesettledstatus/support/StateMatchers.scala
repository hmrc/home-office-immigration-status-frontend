package uk.gov.hmrc.homeofficesettledstatus.support

import org.scalatest.matchers.{MatchResult, Matcher}

trait StateMatchers[S] {

  def thenGo(state: S): Matcher[(S, List[S])] =
    new Matcher[(S, List[S])] {
      override def apply(result: (S, List[S])): MatchResult = result match {
        case (thisState, _) if state != thisState =>
          MatchResult(false, s"State $state has been expected but got state $thisState", s"")
        case (thisState, _) if state == thisState =>
          MatchResult(true, "", s"")
      }
    }

  def thenMatch(statePF: PartialFunction[S, Unit]): Matcher[(S, List[S])] =
    new Matcher[(S, List[S])] {
      override def apply(result: (S, List[S])): MatchResult = result match {
        case (thisState, _) if !statePF.isDefinedAt(thisState) =>
          MatchResult(false, s"Matching state has been expected but got state $thisState", s"")
        case _ => MatchResult(true, "", s"")
      }
    }

}
