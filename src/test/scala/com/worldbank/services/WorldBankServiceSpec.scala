package com.worldbank.services

import com.worldbank.repositories.algebras.WorldBankData
import com.worldbank.services.CountryRate.getRates
import munit._

class WorldBankServiceSpec extends FunSuite {
  test("getRates") {
    val countries = List(
      WorldBankData("America", Some(-1), Some(2010)),
      WorldBankData("America", Some(2), Some(2011)),
      WorldBankData("America", Some(3), Some(2012)),
      WorldBankData("Colombia", Some(2), Some(2010)),
      WorldBankData("Colombia", Some(3), Some(2012)),
      WorldBankData("Canada", Some(1), Some(2010)),
      WorldBankData("Canada", Some(-3), Some(2011)),
      WorldBankData("Canada", Some(4), Some(2012)),
      WorldBankData("New Zealand", Some(2), Some(2010)),
      WorldBankData("Qatar", Some(3), Some(2010)),
      WorldBankData("Rwanda", Some(2), Some(2010)),
      WorldBankData("Rwanda", Some(4), Some(2011)),
      WorldBankData("Venezuela", Some(2), Some(2010)),
      WorldBankData("Zimbawe", Some(-2), Some(2010))
    )

    val rates    = List(
      CountryRate(country = "America", value = 4),
      CountryRate(country = "Canada", value = 3),
      CountryRate(country = "Rwanda", value = 2),
      CountryRate(country = "Colombia", value = 1)
    )
    val obtained = getRates(countries)
    val expected = rates

    assertEquals(obtained, expected)
  }
}
