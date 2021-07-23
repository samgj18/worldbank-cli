package com.worldbank.http

import cats.effect._
import com.worldbank.services._
import io.circe.Error
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe._

final class WorldBankApi(
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

object WorldBankApi          {
  def make: Resource[IO, WorldBankApi] = {
    val createBackend: IO[SttpBackend[IO, Any]] =
      AsyncHttpClientCatsBackend[IO]()
    val resource                                = Resource.make[IO, SttpBackend[IO, Any]](acquire =
      createBackend
    )(release = backend => backend.close())
    resource.map(new WorldBankApi(_))
  }
}
