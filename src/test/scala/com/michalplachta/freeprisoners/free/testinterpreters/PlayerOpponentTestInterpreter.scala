package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.EitherK
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerOpponentTestInterpreter.PlayerOpponent
import com.michalplachta.freeprisoners.states.OpponentState.OpponentStateA
import com.michalplachta.freeprisoners.states.PlayerOpponentState.PlayerOpponentStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

class PlayerOpponentTestInterpreter
    extends (PlayerOpponent ~> PlayerOpponentStateA) {
  implicit val playerInterpreter: Player ~> PlayerOpponentStateA =
    (new PlayerTestInterpreter)
      .andThen(new (PlayerStateA ~> PlayerOpponentStateA) {
        override def apply[A](
            playerStateA: PlayerStateA[A]): PlayerOpponentStateA[A] =
          playerStateA.transformS(
            _.playerState,
            (state, playerState) => state.copy(playerState = playerState))
      })

  implicit val opponentInterpreter: Opponent ~> PlayerOpponentStateA =
    (new OpponentTestInterpreter)
      .andThen(new (OpponentStateA ~> PlayerOpponentStateA) {
        override def apply[A](
            opponentStateA: OpponentStateA[A]): PlayerOpponentStateA[A] =
          opponentStateA.transformS(
            _.opponentState,
            (state, opponentState) => state.copy(opponentState = opponentState))
      })

  override def apply[A](playerOpponent: PlayerOpponent[A]) =
    (playerInterpreter or opponentInterpreter).apply(playerOpponent)
}

object PlayerOpponentTestInterpreter {
  type PlayerOpponent[A] = EitherK[Player, Opponent, A]
}
