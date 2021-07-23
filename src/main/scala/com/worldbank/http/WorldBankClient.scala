package com.worldbank.http

import cats.effect.IO
import com.worldbank.repositories.interpreters.WorldBankData.{
  insertGdpRow,
  insertPppRow
}
import com.worldbank.services.APIWorldBankData
import doobie.{ConnectionIO, Transactor}
import fs2.Stream
import doobie.implicits._

final class WorldBankClient(client: WorldBankApi) {
  def getCountriesPopulation: Stream[IO, APIWorldBankData] = {
    Stream
      .eval(client.getCountriesPopulation(1))
      .flatMap { result =>
        Stream.unfoldEval(result.page) { existingState =>
          client
            .getCountriesPopulation(existingState)
            .map { response =>
              if (response.page <= response.pages)
                Some((response, response.page + 1))
              else None
            }
        }
      }
      .flatMap(response => Stream.emits(response.data))
  }

  def getCountriesGdp: Stream[IO, APIWorldBankData] = {
    Stream
      .eval(client.getCountriesGdp(1))
      .flatMap { result =>
        Stream.unfoldEval(result.page) { existingState =>
          client
            .getCountriesGdp(existingState)
            .map { response =>
              if (response.page <= response.pages)
                Some((response, response.page + 1))
              else None
            }
        }
      }
      .flatMap(response => Stream.emits(response.data))
  }

  def fetchData(
      xa: Transactor[IO],
      dataType: WorldBankIndicator
  ): Stream[IO, APIWorldBankData] = {
    val bankUser = new WorldBankClient(client)

    val data                                         = dataType match {
      case WorldBankIndicator.GDP => bankUser.getCountriesGdp
      case WorldBankIndicator.PPP => bankUser.getCountriesPopulation
    }

    val query: APIWorldBankData => ConnectionIO[Int] = dataType match {
      case WorldBankIndicator.GDP => insertGdpRow
      case WorldBankIndicator.PPP => insertPppRow
    }

    data
      .evalMap(datapoint => query(datapoint).transact(xa).as(datapoint))
  }

}
