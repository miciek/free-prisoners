# Free Prisoners

An application built on top of [Free Monad in Cats](http://typelevel.org/cats/datatypes/freemonad.html). It shows how **Free** can be used to create pure DSLs, side-effecting interpreters and how to use those things together.

The example implements a way to play [Prisoner's Dilemma](https://en.wikipedia.org/wiki/Prisoner's_dilemma) in several modes:

- **[Hot Seat Game](src/main/scala/com/michalplachta/freeprisoners/free/programs/HotSeatGame.scala)** - two players play using one computer and console input/output,
- **[Single Player Game](src/main/scala/com/michalplachta/freeprisoners/free/programs/SinglePlayerGame.scala)** - one player plays against a bot,
- **[Multiplayer Game](src/main/scala/com/michalplachta/freeprisoners/free/programs/Multiplayer.scala)** - one player plays against another player (or bot) on remote server.

## Running local games
1. `sbt "runMain com.michalplachta.freeprisoners.apps.HotSeatGame"` - to run Hot Seat Game.
1. `sbt "runMain com.michalplachta.freeprisoners.apps.SinglePlayerGame"` - to run Single Player Game.

## Running Multiplayer Game with the Remote Server
`RemoteServerInterpreter` uses [Akka Remoting (codename Artery)](https://doc.akka.io/docs/akka/2.5.6/scala/remoting-artery.html).

To run [Multiplayer Game](src/main/scala/com/michalplachta/freeprisoners/free/programs/MultiplayerGame.scala) you will need three separate `sbt` sessions:

1. `BIND_PORT=2552 sbt "runMain com.michalplachta.freeprisoners.apps.MultiplayerServer"` - to run the server on specified server port,
1. `sbt "runMain com.michalplachta.freeprisoners.apps.MultiplayerGame"` - to run the first player session,
1. `sbt "runMain com.michalplachta.freeprisoners.apps.MultiplayerGame"` - to run the second player session.

See [application.conf](src/main/resources/application.conf) for more details about the configuration of all 3 actor systems.
