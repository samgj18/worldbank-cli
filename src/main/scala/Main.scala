import cats.effect.{ExitCode, IO}
import cats.implicits._
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import com.worldbank.client.ClientOpts._
import com.worldbank.client.{Results, ShowProcesses}
import com.worldbank.http.{WorldBankApi, WorldBankIndicator, WorldBankClient}
import com.worldbank.repositories.interpreters.WorldBankData._
import com.worldbank.services.APIWorldBankData
import doobie.implicits._
import fs2._
import org.h2.store.fs.FileUtils

object Main
    extends CommandIOApp(
      name = "worldbank",
      header = "Autoscheduler command line",
      version = "0.0.1"
    ) {

  def program: Stream[IO, APIWorldBankData] = {
    Stream
      .resource(transactorResource)
      .zip(Stream.resource(WorldBankApi.make))
      .flatMap {
        case (transactor, interpreter) =>
          val client = new WorldBankClient(interpreter)
          Stream
            .eval(createTables.transact(transactor))
            .drain ++
            Stream(
              client.fetchData(transactor, WorldBankIndicator.PPP),
              client.fetchData(transactor, WorldBankIndicator.GDP)
            ).parJoinUnbounded
      }
  }

  def main: Opts[IO[ExitCode]]              =
    (showCommandsOpts orElse resultsOpts).map {
      case ShowProcesses(_)           => IO.println("Starting...").as(ExitCode.Success)
      case Results(ingest, calculate) =>
        val ingestEffect =
          if (ingest)
            IO(
              FileUtils
                .tryDelete(
                  s"${new java.io.File(".").getAbsolutePath}/pecunia:worldbank.mv.db"
                )
            ) *>
              program.compile.drain
          else IO.unit

        val calculateEffect =
          if (calculate)
            IO.println("TODO")
          else IO.unit

        (ingestEffect >> calculateEffect).as(ExitCode.Success)
    }
}
