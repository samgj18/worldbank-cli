package com.worldbank.http

import cats.effect._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Error, HCursor}
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe._

final case class ApiWorldBankDataResponse(
    page: Int,
    pages: Int,
    data: List[APIWorldBankData]
)

object ApiWorldBankDataResponse {
  implicit val apiWorldBankDataResponseDecoder
      : Decoder[ApiWorldBankDataResponse] = { (cursor: HCursor) =>
    val pageInfoCursor = cursor.downN(0)
    val dataCursor     = cursor.downN(1)
    for {
      page  <- pageInfoCursor.downField("page").as[Int]
      pages <- pageInfoCursor.downField("pages").as[Int]
      data  <- dataCursor.as[List[APIWorldBankData]]
    } yield ApiWorldBankDataResponse(page, pages, data)
  }
}

final case class APIWorldBankData(
    indicator: Indicator,
    country: Country,
    countryiso3code: String,
    value: Option[BigDecimal],
    date: String,
    unit: Option[String],
    obs_status: Option[String],
    decimal: Int
)

object APIWorldBankData         {
  implicit val apiWorldBankDataDecoder: Decoder[APIWorldBankData] =
    deriveDecoder[APIWorldBankData]
}

final case class Country(
    id: String,
    value: String
)

object Country                  {
  implicit val countryDecoder: Decoder[Country] =
    deriveDecoder[Country]
}

final case class Indicator(
    id: String,
    value: String
)

object Indicator                {
  implicit val indicatorDecoder: Decoder[Indicator] =
    deriveDecoder[Indicator]
}

trait WorldBankHttp             {
  def getCountriesPopulation(page: Int): IO[ApiWorldBankDataResponse]
  def getCountriesGdp(page: Int): IO[ApiWorldBankDataResponse]
}

final class WorldBankApi(backend: SttpBackend[IO, Any]) extends WorldBankHttp {
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

object WorldBankApi {
  def make: Resource[IO, WorldBankApi] = {
    val createBackend: IO[SttpBackend[IO, Any]] =
      AsyncHttpClientCatsBackend[IO]()
    val resource                                = Resource.make[IO, SttpBackend[IO, Any]](acquire =
      createBackend
    )(release = backend => backend.close())
    resource.map(new WorldBankApi(_))
  }
}
