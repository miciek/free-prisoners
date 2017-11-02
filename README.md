# Free Prisoners

An application built on top of [Free Monad in Cats](http://typelevel.org/cats/datatypes/freemonad.html). It shows how **Free** can be used to create pure DSLs, side-effecting interpreters and how to use those things together.

The example implements a way to play [Prisoner's Dilemma](https://en.wikipedia.org/wiki/Prisoner's_dilemma) in several modes:

- **[Hot Seat Game](src/main/scala/com/michalplachta/cats/free/HotSeatGame.scala)** - two players play using one computer and console input/output,
- **[Single Player Game](src/main/scala/com/michalplachta/cats/free/SinglePlayerGame.scala)** - one player plays against a bot,
- **[Local Multiplayer Game](src/main/scala/com/michalplachta/cats/free/LocalMultiplayerGame.scala)** - one player plays against bot on local server.
- **Multiplayer Game** - one player plays against another player or bot (remotely). *Work In Progress*

Multiplayer version has two separate interpreters. `LocalServerInterpreter` uses local [Akka](http://akka.io/) `ActorSystem` as transport layer. `RemoteServerInterpreter` uses [Akka Remoting](http://doc.akka.io/docs/akka/2.4/scala/remoting.html).

## Running local games
Use `sbt run` and choose `HotSeatGame`, `SinglePlayerGame` or `LocalMultiplayerGame`.

## Running Multiplayer Game with the Remote Server
*Work In Progress*

To run [Multiplayer Game](src/main/scala/com/michalplachta/cats/free/MultiplayerGame.scala) you will need three separate `sbt` sessions:

1. `sbt "runMain com.michalplachta.cats.free.MultiplayerServer"` - to run the server on default server port (see [server.conf](src/main/resources/server.conf))
1. `sbt "runMain com.michalplachta.cats.free.MultiplayerGame"` - to run the first player session on default client port (see [client.conf](src/main/resources/client.conf))
1. `CLIENT_TCP_PORT=2554 sbt "runMain com.michalplachta.cats.free.MultiplayerGame"` - to run the second player session on port `2554`.

