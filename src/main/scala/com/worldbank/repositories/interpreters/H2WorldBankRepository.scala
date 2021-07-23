package com.worldbank.repositories.interpreters
import cats.implicits._
import com.worldbank.repositories.{WorldBankData, WorldBankRepository}
import com.worldbank.services.APIWorldBankData
import doobie.implicits.toSqlInterpolator
import fs2.Stream
import doobie.ConnectionIO

object WorldBankData extends WorldBankRepository {

  def createTables: ConnectionIO[Boolean] =
    sql"CREATE TABLE IF NOT EXISTS worldbankgpd(indicator VARCHAR(100), country VARCHAR(100), countryiso3code VARCHAR(100), value DECIMAL(20, 2) DEFAULT NULL, date VARCHAR(300), unit VARCHAR(100) DEFAULT NULL, obs_status VARCHAR(100) DEFAULT NULL, decimal INT)".update.run >>
      sql"CREATE TABLE IF NOT EXISTS worldbankppp(indicator VARCHAR(100), country VARCHAR(100), countryiso3code VARCHAR(100), value DECIMAL(20, 2) DEFAULT NULL, date VARCHAR(300), unit VARCHAR(100) DEFAULT NULL, obs_status VARCHAR(100) DEFAULT NULL, decimal INT)".update.run
        .map(_ > 0)

  def insertGdpRow(s: APIWorldBankData): ConnectionIO[Int] =
    sql"INSERT INTO worldbankgpd(INDICATOR, COUNTRY, COUNTRYISO3CODE, `VALUE`, `DATE`, UNIT, OBS_STATUS, `DECIMAL`) VALUES(${s.indicator.value}, ${s.country.value}, ${s.countryiso3code}, ${s.value}, ${s.date}, ${s.unit}, ${s.obs_status}, ${s.decimal})".update.run

  def insertPppRow(s: APIWorldBankData): ConnectionIO[Int] =
    sql"INSERT INTO worldbankppp(INDICATOR, COUNTRY, COUNTRYISO3CODE, `VALUE`, `DATE`, UNIT, OBS_STATUS, `DECIMAL`) VALUES(${s.indicator.value}, ${s.country.value}, ${s.countryiso3code}, ${s.value}, ${s.date}, ${s.unit}, ${s.obs_status}, ${s.decimal})".update.run

  def getCountriesOrderedByPPPRate: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT country, SUM(diff) FROM ((SELECT COUNTRY, `DATE`, `VALUE`, `VALUE` - LAG(VALUE, 1) OVER (PARTITION BY country ORDER BY `DATE`) AS diff FROM worldbankppp)) AS t WHERE diff IS NOT NULL GROUP BY COUNTRY ORDER BY SUM(diff) DESC;"
      .query[WorldBankData]
      .stream

  def getCountriesOrderedByGDPRate: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT COUNTRY, SUM(diff) FROM ((SELECT COUNTRY, `DATE`, `VALUE`, `VALUE` - LAG(VALUE, 1) OVER (PARTITION BY COUNTRY ORDER BY `DATE`) AS diff FROM worldbankgpd)) AS t WHERE diff IS NOT NULL GROUP BY COUNTRY ORDER BY SUM(diff) DESC;"
      .query[WorldBankData]
      .stream

  def getWorldBankPPPData: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT COUNTRY, `VALUE`, `DATE` FROM WORLDBANKPPP;"
      .query[WorldBankData]
      .stream
}
