package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma._

object BotOps {
  sealed trait Bot[A]
  final case class CreateBot(name: String, strategy: Strategy)
      extends Bot[Prisoner]
  final case class GetDecision(prisoner: Prisoner, otherPrisoner: Prisoner)
      extends Bot[Decision]

  object Bot {
    class Ops[S[_]](implicit s: Bot :<: S) {
      def createBot(name: String, strategy: Strategy): Free[S, Prisoner] =
        Free.inject(CreateBot(name, strategy))

      def getDecision(prisoner: Prisoner,
                      otherPrisoner: Prisoner): Free[S, Decision] =
        Free.inject(GetDecision(prisoner, otherPrisoner))
    }
  }

  type Strategy = Prisoner => Decision
  object Strategies {
    val alwaysBlame: Strategy = _ => Guilty
    val alwaysSilent: Strategy = _ => Silence
  }
}
