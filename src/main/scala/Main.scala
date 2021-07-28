import cats.effect._
import cats.implicits._
import com.monovore.decline.Opts
import com.monovore.decline.effect._
import com.worldbank.client.ClientOpts._
import com.worldbank.client._
import com.worldbank.http._
import com.worldbank.repositories.interpreters.WorldBankData._
import com.worldbank.services.CountryRate.getRates
import doobie.ConnectionIO
import doobie.implicits._
import fs2._
import org.h2.store.fs.FileUtils

object Main
    extends CommandIOApp(
      name = "worldbank",
      header = "Autoscheduler command line",
      version = "0.0.1"
    ) {

  sealed trait Program
  object Program {
    case object Ingest    extends Program
    case object Calculate extends Program
    case object Run       extends Program
  }

  def ingestion: Stream[IO, APIWorldBankData] =
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

  def calculatePpp: IO[String] = {
    val getCountriesRates: ConnectionIO[String] = for {
      countries      <- getWorldBankPPPData.compile.toList
      rankedCountries = getRates(countries)
      organizedData   = rankedCountries
                        .sortWith(_.value > _.value)
                        .take(10)

    } yield {
      organizedData
        .map(country => s"${country.country}")
        .mkString(", ")
    }

    transactorResource.use(getCountriesRates.transact[IO])

  }

  def calculateGdp: IO[String] = {
    val getCountriesRates: ConnectionIO[String] = for {

      countriesWithHighestPPPRate <-
        getCountriesOrderedByPPPRate.compile.toList.map(_.toSet)
      countriesWithGdp            <-
        getCountriesOrderedByGDPRate.compile.toList.map(_.toSet)
      top3CountriesByGDPGrowth     =
        countriesWithHighestPPPRate.intersect(countriesWithGdp)
    } yield {
      top3CountriesByGDPGrowth.mkString(", ")
    }

    transactorResource.use(getCountriesRates.transact[IO])
  }

  def main: Opts[IO[ExitCode]] = {

    def ingestionEffectLyft: IO[Unit] =
      IO(
        FileUtils
          .tryDelete(
            s"${new java.io.File(".").getAbsolutePath}/pecunia:worldbank.mv.db"
          )
      ) >>
        ingestion.compile.drain

    def calculationsEffectLyft: IO[Unit] =
      IO.println(
        "This is the list of the top 10 countries with highest PPP rate: "
      ) >>
        calculatePpp.flatMap(IO.println) >>
        IO.println(
          "This is the list of the top 3 countries with highest GDP rate: "
        ) >>
        calculateGdp.flatMap(IO.println)

    resultsOpts.map {
      case Results(ingest, calculate) =>
        val ingestEffect    = if (ingest) ingestionEffectLyft else IO.unit
        val calculateEffect = if (calculate) calculationsEffectLyft else IO.unit

        ingestEffect >> calculateEffect as ExitCode.Success
    }
  }
}
