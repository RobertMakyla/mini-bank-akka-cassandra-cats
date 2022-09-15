package minibank.wiring


import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import minibank.actors.Bank
import minibank.actors.PersistentBankAccount.Command
import minibank.http.BankRouter
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.Http
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

object Boot {

  def startHttpServer(bank: ActorRef[Command])(implicit system: ActorSystem[_]): Unit = {

    implicit val ec: ExecutionContext = system.executionContext

    val routes = new BankRouter(bank).routes

    val httpBindingFuture = Http().newServerAt("localhost", 8080).bind(routes)
    httpBindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        system.log.error(s"Failed to bind HTTP server, because: $ex")
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    trait RootCommand
    case class GetBankActor(replyTo: ActorRef[ActorRef[Command]]) extends RootCommand

    val root: Behavior[RootCommand] = Behaviors.setup { context =>
      val bankActor = context.spawn(Bank(), "bank")
      Behaviors.receiveMessage {
        case GetBankActor(replyTo) =>
          replyTo ! bankActor
          Behaviors.same
      }
    }

    implicit val system: ActorSystem[RootCommand] = ActorSystem(root, "BankSystem")
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val ec: ExecutionContext = system.executionContext

    val bankActorFuture: Future[ActorRef[Command]] = system.ask(replyTo => GetBankActor(replyTo))
    bankActorFuture.foreach(startHttpServer)
  }
}