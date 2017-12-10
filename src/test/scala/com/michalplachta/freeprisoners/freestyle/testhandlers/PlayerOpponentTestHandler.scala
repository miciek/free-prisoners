package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.~>
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import com.michalplachta.freeprisoners.states.OpponentState.OpponentStateA
import com.michalplachta.freeprisoners.states.PlayerOpponentState.PlayerOpponentStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

trait PlayerOpponentTestHandler
    extends PlayerTestHandler
    with OpponentTestHandler {
  implicit val playerHandler: Player.Op ~> PlayerOpponentStateA =
    playerTestHandler.andThen(new (PlayerStateA ~> PlayerOpponentStateA) {
      override def apply[A](
          playerStateA: PlayerStateA[A]): PlayerOpponentStateA[A] =
        playerStateA.transformS(
          _.playerState,
          (state, playerState) => state.copy(playerState = playerState))
    })

  implicit val opponentHandler: Opponent.Op ~> PlayerOpponentStateA =
    opponentTestHandler.andThen(new (OpponentStateA ~> PlayerOpponentStateA) {
      override def apply[A](
          opponentStateA: OpponentStateA[A]): PlayerOpponentStateA[A] =
        opponentStateA.transformS(
          _.opponentState,
          (state, opponentState) => state.copy(opponentState = opponentState))
    })
}
