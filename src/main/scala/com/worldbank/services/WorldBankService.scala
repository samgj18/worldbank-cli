package com.worldbank.services

import com.worldbank.repositories.WorldBankData

final case class CountryRate(
    country: String,
    value: BigDecimal
)

object CountryRate {

  def getRates(
      countries: List[WorldBankData]
  ): List[CountryRate] = {
    countries
      .groupBy(_.country)
      .flatMap {
        case (name, country) =>
          country
            .sortBy(_.date)
            .sliding(2)
            .collect {
              case yearN :: yearNPlus1 :: Nil =>
                (yearNPlus1.value, yearN.value) match {
                  case (Some(yearPlus1), Some(year)) =>
                    Some(
                      CountryRate(
                        country = name,
                        value = yearPlus1 - year
                      )
                    )
                  case _                             => None
                }
            }
            .toList
            .flatten
      }
      .toList
      .groupBy(_.country)
      .map(country => (country._1, country._2.map(_.value).sum))
      .map(country => CountryRate(country._1, country._2))
      .toList
      .sortWith(_.value > _.value)
  }

}
