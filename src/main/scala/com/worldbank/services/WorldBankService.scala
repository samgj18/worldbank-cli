package com.worldbank.services

import cats.effect.IO
import io.circe._
import io.circe.generic.semiauto._

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

trait WorldBankService          {
  def getCountriesPopulation(page: Int): IO[ApiWorldBankDataResponse]
  def getCountriesGdp(page: Int): IO[ApiWorldBankDataResponse]
}
