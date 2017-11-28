package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.~>
import com.michalplachta.freeprisoners.freestyle.algebras.{Bot, Player}
import com.michalplachta.freeprisoners.states.BotHandler.BotStateA
import com.michalplachta.freeprisoners.states.PlayerBotState.PlayerBotStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

trait PlayerBotTestHandler extends PlayerTestHandler with BotTestHandler {
  implicit val playerHandler: Player.Op ~> PlayerBotStateA =
    playerTestHandler.andThen(new (PlayerStateA ~> PlayerBotStateA) {
      override def apply[A](playerStateA: PlayerStateA[A]): PlayerBotStateA[A] =
        playerStateA.transformS(
          _.playerState,
          (state, playerState) => state.copy(playerState = playerState))
    })

  implicit val botHandler: Bot.Op ~> PlayerBotStateA =
    botTestHandler.andThen(new (BotStateA ~> PlayerBotStateA) {
      override def apply[A](botStateA: BotStateA[A]): PlayerBotStateA[A] =
        botStateA.transformS(
          _.botState,
          (state, botState) => state.copy(botState = botState))
    })
}
