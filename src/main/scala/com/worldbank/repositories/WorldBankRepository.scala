package com.worldbank.repositories

import com.worldbank.services.APIWorldBankData
import doobie.ConnectionIO
import fs2.Stream

final case class WorldBankData(
    country: String,
    value: Option[BigDecimal],
    date: Option[String]
)

trait WorldBankRepository {
  def createTables: ConnectionIO[Boolean]
  def insertGdpRow(s: APIWorldBankData): ConnectionIO[Int]
  def insertPppRow(s: APIWorldBankData): ConnectionIO[Int]
  def getCountriesOrderedByPPPRate: Stream[ConnectionIO, WorldBankData]
  def getCountriesOrderedByGDPRate: Stream[ConnectionIO, WorldBankData]
}
