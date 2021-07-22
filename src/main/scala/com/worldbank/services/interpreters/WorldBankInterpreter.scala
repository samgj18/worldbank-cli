package com.worldbank.services.interpreters

import cats.effect._
import com.worldbank.services._
import com.worldbank.services.interpreters.WorldBankInterpreter._
import fs2._
import io.circe.Error
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe._

class WorldBankUser(client: WorldBankInterpreter) {
  def getCountriesPopulation: Stream[IO, APIWorldBankData] = {
    Stream
      .eval(client.getCountriesPopulation(1))
      .flatMap { result =>
        Stream.unfoldEval(result.page) { existingState =>
          client
            .getCountriesPopulation(existingState)
            .map { response =>
              if (response.page <= response.pages)
                Some((response, response.page + 1))
              else None
            }
        }
      }
      .flatMap(response => Stream.emits(response.data))
  }

  def getCountriesGdp: Stream[IO, APIWorldBankData] = {
    Stream
      .eval(client.getCountriesGdp(1))
      .flatMap { result =>
        Stream.unfoldEval(result.page) { existingState =>
          client
            .getCountriesGdp(existingState)
            .map { response =>
              if (response.page <= response.pages)
                Some((response, response.page + 1))
              else None
            }
        }
      }
      .flatMap(response => Stream.emits(response.data))
  }
}

object WorldBankInterpreter                       {
  def make: Resource[IO, WorldBankInterpreter] = {
    val createBackend: IO[SttpBackend[IO, Any]] =
      AsyncHttpClientCatsBackend[IO]()
    val resource                                = Resource.make[IO, SttpBackend[IO, Any]](acquire =
      createBackend
    )(release = backend => backend.close())
    resource.map(new WorldBankInterpreter(_))
  }

  private[interpreters] class WorldBankInterpreter(
      backend: SttpBackend[IO, Any]
  ) extends WorldBankService {
    private val baseUrl = "https://api.worldbank.org/v2/country/all/indicator"

    def getCountriesPopulation(page: Int): IO[ApiWorldBankDataResponse] = {
      val request: RequestT[Identity, Either[
        ResponseException[String, Error],
        ApiWorldBankDataResponse
      ], Any] =
        basicRequest
          .get(
            uri"${baseUrl}/SP.POP.TOTL?date=2010:2018&format=json&page=${page}"
          )
          .response(asJson[ApiWorldBankDataResponse])

      request
        .send(backend)
        .map(_.body)
        .flatMap(IO.fromEither)
    }

    def getCountriesGdp(page: Int): IO[ApiWorldBankDataResponse]        = {
      val request: RequestT[Identity, Either[
        ResponseException[String, Error],
        ApiWorldBankDataResponse
      ], Any] = basicRequest
        .get(
          uri"${baseUrl}/NY.GDP.MKTP.PP.KD?date=2010:2018&format=json&page=${page}"
        )
        .response(asJson[ApiWorldBankDataResponse])

      request
        .send(backend)
        .map(_.body)
        .flatMap(IO.fromEither)
    }
  }
}
