package com.worldbank.repositories.interpreters
import cats.implicits._
import com.worldbank.http.APIWorldBankData
import com.worldbank.repositories.algebras.{WorldBankData, WorldBankRepository}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import fs2.Stream

object WorldBankData extends WorldBankRepository {

  def createTables: ConnectionIO[Boolean] =
    sql"CREATE TABLE IF NOT EXISTS worldbankgpd(indicator VARCHAR(100), country VARCHAR(100), countryiso3code VARCHAR(100), value DECIMAL(20, 2) DEFAULT NULL, date VARCHAR(300), unit VARCHAR(100) DEFAULT NULL, obs_status VARCHAR(100) DEFAULT NULL, decimal INT)".update.run >>
      sql"CREATE TABLE IF NOT EXISTS worldbankppp(indicator VARCHAR(100), country VARCHAR(100), countryiso3code VARCHAR(100), value DECIMAL(20, 2) DEFAULT NULL, date VARCHAR(300), unit VARCHAR(100) DEFAULT NULL, obs_status VARCHAR(100) DEFAULT NULL, decimal INT)".update.run
        .map(_ > 0)

  def insertGdpRow(s: APIWorldBankData): ConnectionIO[Int] =
    sql"INSERT INTO worldbankgpd(INDICATOR, COUNTRY, COUNTRYISO3CODE, `VALUE`, `DATE`, UNIT, OBS_STATUS, `DECIMAL`) VALUES(${s.indicator.value}, ${s.country.value}, ${s.countryiso3code}, ${s.value}, ${s.date}, ${s.unit}, ${s.obs_status}, ${s.decimal})".update.run

  def insertPppRow(s: APIWorldBankData): ConnectionIO[Int] =
    sql"INSERT INTO worldbankppp(INDICATOR, COUNTRY, COUNTRYISO3CODE, `VALUE`, `DATE`, UNIT, OBS_STATUS, `DECIMAL`) VALUES(${s.indicator.value}, ${s.country.value}, ${s.countryiso3code}, ${s.value}, ${s.date}, ${s.unit}, ${s.obs_status}, ${s.decimal})".update.run

  def getCountriesOrderedByPPPRate: Stream[ConnectionIO, String] =
    sql"SELECT COUNTRY, SUM(diff), NULL as DATE FROM ((SELECT COUNTRY, `DATE`, `VALUE`, `VALUE` - LAG(VALUE, 1) OVER (PARTITION BY COUNTRY ORDER BY `DATE`) AS diff FROM worldbankppp WHERE COUNTRYISO3CODE != '' AND COUNTRYISO3CODE NOT IN('TSS', 'SSA', 'PRE', 'TMN', 'LAC', 'IBD', 'IBT', 'MNA', 'LDC', 'IDB', 'MEA', 'TLA', 'LCN', 'IDX', 'IDA', 'LTE', 'EAR', 'AFE', 'ARB', 'EAS', 'EMU', 'ECS', 'ECA', 'TEC', 'EUU', 'FCS', 'HPC', 'WLD', 'LMY', 'MIC', 'EAP', 'TEA', 'OED', 'PST', 'TSA', 'SAS', 'SSF'))) AS t WHERE diff IS NOT NULL GROUP BY COUNTRY ORDER BY SUM(diff) DESC LIMIT 10;"
      .query[WorldBankData]
      .stream
      .map(_.country)

  def getCountriesOrderedByGDPRate: Stream[ConnectionIO, String] =
    sql"SELECT COUNTRY, SUM(diff), NULL as DATE FROM ((SELECT COUNTRY, `DATE`, `VALUE`, `VALUE` - LAG(VALUE, 1) OVER (PARTITION BY COUNTRY ORDER BY `DATE`) AS diff FROM worldbankgpd WHERE COUNTRYISO3CODE != '' AND COUNTRYISO3CODE NOT IN('TSS', 'SSA', 'PRE', 'TMN', 'LAC', 'IBD', 'IBT', 'MNA', 'LDC', 'IDB', 'MEA', 'TLA', 'LCN', 'IDX', 'IDA', 'LTE', 'EAR', 'AFE', 'ARB', 'EAS', 'EMU', 'ECS', 'ECA', 'TEC', 'EUU', 'FCS', 'HPC', 'WLD', 'LMY', 'MIC', 'EAP', 'TEA', 'OED', 'PST', 'TSA', 'SAS', 'SSF'))) AS t WHERE diff IS NOT NULL GROUP BY COUNTRY ORDER BY SUM(diff) DESC LIMIT 3;"
      .query[WorldBankData]
      .stream
      .map(_.country)

  def getWorldBankPPPData: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT COUNTRY, `VALUE`, `DATE` FROM WORLDBANKPPP WHERE COUNTRYISO3CODE != '' AND COUNTRYISO3CODE NOT IN('TSS', 'SSA', 'PRE', 'TMN', 'LAC', 'IBD', 'IBT', 'MNA', 'LDC', 'IDB', 'MEA', 'TLA', 'LCN', 'IDX', 'IDA', 'LTE', 'EAR', 'AFE', 'ARB', 'EAS', 'EMU', 'ECS', 'ECA', 'TEC', 'EUU', 'FCS', 'HPC', 'WLD', 'LMY', 'MIC', 'EAP', 'TEA', 'OED', 'PST', 'TSA', 'SAS', 'SSF');"
      .query[WorldBankData]
      .stream

  def getWorldBankGdpData: Stream[ConnectionIO, WorldBankData] =
    sql"SELECT COUNTRY, `VALUE`, `DATE` FROM WORLDBANKGPD WHERE COUNTRYISO3CODE != '' AND COUNTRYISO3CODE NOT IN('TSS', 'SSA', 'PRE', 'TMN', 'LAC', 'IBD', 'IBT', 'MNA', 'LDC', 'IDB', 'MEA', 'TLA', 'LCN', 'IDX', 'IDA', 'LTE', 'EAR', 'AFE', 'ARB', 'EAS', 'EMU', 'ECS', 'ECA', 'TEC', 'EUU', 'FCS', 'HPC', 'WLD', 'LMY', 'MIC', 'EAP', 'TEA', 'OED', 'PST', 'TSA', 'SAS', 'SSF');"
      .query[WorldBankData]
      .stream

}
