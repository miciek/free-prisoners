package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.EitherK
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerBotTestInterpreter.PlayerBot
import com.michalplachta.freeprisoners.states.BotHandler.BotStateA
import com.michalplachta.freeprisoners.states.PlayerBotState.PlayerBotStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

class PlayerBotTestInterpreter extends (PlayerBot ~> PlayerBotStateA) {
  implicit val playerInterpreter: Player ~> PlayerBotStateA =
    (new PlayerTestInterpreter).andThen(new (PlayerStateA ~> PlayerBotStateA) {
      override def apply[A](playerStateA: PlayerStateA[A]): PlayerBotStateA[A] =
        playerStateA.transformS(
          _.playerState,
          (state, playerState) => state.copy(playerState = playerState))
    })

  implicit val botInterpreter: Bot ~> PlayerBotStateA =
    (new BotTestInterpreter).andThen(new (BotStateA ~> PlayerBotStateA) {
      override def apply[A](botStateA: BotStateA[A]): PlayerBotStateA[A] =
        botStateA.transformS(
          _.botState,
          (state, botState) => state.copy(botState = botState))
    })

  override def apply[A](playerBot: PlayerBot[A]) =
    (playerInterpreter or botInterpreter).apply(playerBot)
}

object PlayerBotTestInterpreter {
  type PlayerBot[A] = EitherK[Player, Bot, A]
}
