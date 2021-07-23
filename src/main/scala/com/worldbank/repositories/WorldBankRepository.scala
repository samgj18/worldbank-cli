package com.worldbank.repositories

import com.worldbank.http.APIWorldBankData
import doobie.ConnectionIO
import fs2.Stream

final case class WorldBankData(
    country: String,
    value: Option[BigDecimal],
    date: Option[Int]
)

trait WorldBankRepository {
  def createTables: ConnectionIO[Boolean]
  def insertGdpRow(s: APIWorldBankData): ConnectionIO[Int]
  def insertPppRow(s: APIWorldBankData): ConnectionIO[Int]
  def getCountriesOrderedByPPPRate: Stream[ConnectionIO, String]
  def getCountriesOrderedByGDPRate: Stream[ConnectionIO, String]
}
