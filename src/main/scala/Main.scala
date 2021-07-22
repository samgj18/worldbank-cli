import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.worldbank.services.APIWorldBankData
import com.worldbank.services.WorldBankData.{createTable, insertWorldBankRow}
import com.worldbank.services.interpreters.{WorldBankInterpreter, WorldBankUser}
import doobie.ExecutionContexts
import doobie.h2.H2Transactor
import doobie.implicits._
import fs2._

object Main extends IOApp {

  // Launcher.execute[Client](args)

  override def run(args: List[String]): IO[ExitCode] = {
    /*    WorldBankInterpreter.make
      .use(_.getCountriesPopulation(1))
      .flatMap(response => IO(println(response)))
      .as(ExitCode.Success)*/

    /*    val s1 = Stream[IO, Int](1, 2, 3)
      .metered(100.millis)
      .evalTap(datapoint => IO(println(datapoint)))

    val s2 = Stream[IO, Int](4, 5, 6)
      .metered(200.millis)
      .evalTap(datapoint => IO(println(datapoint)))*/

//    s1.concurrently(s2)
//      .compile
//      .drain
//      .as(ExitCode.Success)

    /*    Stream(s1, s2).parJoinUnbounded.compile.drain
      .as(ExitCode.Success)*/

    def transactorResource: Resource[IO, H2Transactor[IO]] =
      for {
        ce         <- ExecutionContexts.fixedThreadPool[IO](32)
        transactor <-
          H2Transactor.newH2Transactor[IO](
            url =
              "jdbc:h2:/Users/samuelgomezjimenez/Documents/GitHub/pecunia/pecunia:worldbank",
            user = "sam",
            pass = "",
            connectEC = ce
          )
      } yield transactor

    val gdp: Stream[IO, APIWorldBankData]                  = Stream
      .resource(WorldBankInterpreter.make)
      .zip(Stream.resource(transactorResource))
      .evalTap(resource => createTable.transact(resource._2))
      .flatMap(resource => {
        val (bank, xa) = resource
        val bankUser   = new WorldBankUser(bank)
        bankUser.getCountriesGdp
          .evalMap(datapoint =>
            insertWorldBankRow(datapoint).transact(xa).as(datapoint)
          )
          .evalTap(datapoint => IO(println(datapoint)))
      })

    val ppp: Stream[IO, APIWorldBankData]                  = Stream
      .resource(WorldBankInterpreter.make)
      .zip(Stream.resource(transactorResource))
      .evalTap(resource => createTable.transact(resource._2))
      .flatMap(resource => {
        val (bank, xa) = resource
        val bankUser   = new WorldBankUser(bank)
        bankUser.getCountriesPopulation
          .evalMap(datapoint =>
            insertWorldBankRow(datapoint).transact(xa).as(datapoint)
          )
          .evalTap(datapoint => IO(println(datapoint)))
      })

    Stream(gdp, ppp).parJoinUnbounded.compile.drain
      .as(ExitCode.Success)

    /*    Stream
      .resource(WorldBankInterpreter.make)
      .map(new WorldBankUser(_))
      .flatMap(_.getCountriesPopulation)
      .metered(10.millis)
      .evalTap((datapoint: APIWorldBankData) =>
        IO(insertWorldBankRow(datapoint))
      )
      .compile
      .drain
      .as(ExitCode.Success)*/
  }
}
