package com.michalplachta.freeprisoners.apps

object MultiplayerGame {
  def main(args: Array[String]): Unit = {
    MultiplayerGameFree.main(args)
    MultiplayerGameFreestyle.main(args)
  }
}

object MultiplayerGameFree extends App {
  import cats.data.EitherK
  import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps.DecisionRegistry
  import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking
  import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
  import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
  import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
  import com.michalplachta.freeprisoners.free.interpreters._
  import com.michalplachta.freeprisoners.free.programs.UnknownOpponent

  val matchmakingInterpreter = new MatchmakingServerInterpreter
  val decisionServerInterpreter = new DecisionServerInterpreter

  type Multiplayer[A] = EitherK[Player, Opponent, A]
  type LowLevelMultiplayer0[A] = EitherK[DecisionRegistry, Timing, A]
  type LowLevelMultiplayer1[A] = EitherK[Matchmaking, LowLevelMultiplayer0, A]
  type LowLevelMultiplayer[A] = EitherK[Player, LowLevelMultiplayer1, A]

  implicit val playerOps = new Player.Ops[LowLevelMultiplayer]
  implicit val matchmakingOps = new Matchmaking.Ops[LowLevelMultiplayer]
  implicit val decisionRegistryOps =
    new DecisionRegistry.Ops[LowLevelMultiplayer]
  implicit val timingOps = new Timing.Ops[LowLevelMultiplayer]

  val interpreter = new PlayerLocalInterpreter[LowLevelMultiplayer] or new RemoteOpponentInterpreter
  val lowLevelInterpreter = PlayerConsoleInterpreter or (matchmakingInterpreter or (decisionServerInterpreter or TimingInterpreter))

  UnknownOpponent
    .program(
      new Player.Ops[Multiplayer],
      new Opponent.Ops[Multiplayer]
    )
    .foldMap(interpreter)
    .foldMap(lowLevelInterpreter)
    .unsafeRunSync()

  matchmakingInterpreter.terminate()
  decisionServerInterpreter.terminate()
}

/*_*/
object MultiplayerGameFreestyle extends App {
  import cats.effect.IO
  import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
  import com.michalplachta.freeprisoners.freestyle.algebras.{
    DecisionRegistry,
    Matchmaking,
    Timing
  }
  import com.michalplachta.freeprisoners.freestyle.handlers.{
    DecisionServerHandler,
    MatchmakingServerHandler,
    PlayerConsoleHandler,
    TimingHandler
  }
  import com.michalplachta.freeprisoners.freestyle.handlers.{
    PlayerLocalHandler,
    RemoteOpponentHandler
  }
  import com.michalplachta.freeprisoners.freestyle.programs.UnknownOpponent

  import freestyle._
  import freestyle.implicits._

  @module trait Multiplayer {
    val player: Player
    val opponent: Opponent
  }

  @module trait LowLevelMultiplayer {
    val player: Player
    val matchmaking: Matchmaking
    val game: DecisionRegistry
    val timing: Timing
  }

  implicit val playerLocalHandler =
    new PlayerLocalHandler[LowLevelMultiplayer.Op]
  implicit val remoteOpponentHandler =
    new RemoteOpponentHandler[LowLevelMultiplayer.Op]

  implicit val playerHandler = new PlayerConsoleHandler
  implicit val matchmakingHandler = new MatchmakingServerHandler
  implicit val decisionServerHandler = new DecisionServerHandler
  implicit val timingHandler = new TimingHandler
  UnknownOpponent
    .program[Multiplayer.Op]
    .interpret[FreeS[LowLevelMultiplayer.Op, ?]]
    .interpret[IO]
    .unsafeRunSync()

  matchmakingHandler.terminate()
  decisionServerHandler.terminate()
}
