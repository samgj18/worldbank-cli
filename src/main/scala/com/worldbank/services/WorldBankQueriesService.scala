package com.worldbank.services

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import fs2.Stream

trait WorldBankQueriesService {
  def createTable: ConnectionIO[Boolean]
  def insertWorldBankRow(s: APIWorldBankData): ConnectionIO[Int]
  def getWorldBankRows: Stream[ConnectionIO, WorldBankData]
}

final case class WorldBankData(
    country: String,
    value: Option[BigDecimal],
    date: String
)

object WorldBankData extends WorldBankQueriesService {

  def createTable: ConnectionIO[Boolean] =
    sql"CREATE TABLE IF NOT EXISTS worldbank(indicator VARCHAR(100), country VARCHAR(100), countryiso3code VARCHAR(100), value DECIMAL(20, 2) DEFAULT NULL, date VARCHAR(300), unit VARCHAR(100) DEFAULT NULL, obs_status VARCHAR(100) DEFAULT NULL, decimal INT)".update.run
      .map(_ > 0)

  def insertWorldBankRow(s: APIWorldBankData): ConnectionIO[Int] =
    sql"INSERT INTO worldbank(indicator, country, countryiso3code, `value`, `date`, unit, obs_status, `decimal`) VALUES(${s.indicator.value}, ${s.country.value}, ${s.countryiso3code}, ${s.value}, ${s.date}, ${s.unit}, ${s.obs_status}, ${s.decimal})".update.run

  def getWorldBankRows: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT country, `value`, `date` FROM worldbank"
      .query[WorldBankData]
      .stream

}
