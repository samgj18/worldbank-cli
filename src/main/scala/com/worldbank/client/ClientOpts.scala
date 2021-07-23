package com.worldbank.client

import cats.effect.{IO, Resource}
import cats.implicits._
import com.monovore.decline.Opts
import doobie.ExecutionContexts
import doobie.h2.H2Transactor

final case class ShowProcesses(all: Boolean)
final case class Results(ingest: Boolean, calculate: Boolean)

object ClientOpts {
  val resultsOpts: Opts[Results]            =
    Opts.subcommand("run", "Ingest data and calculate results!") {
      val ingestFlag = Opts
        .flag(
          "dataload",
          "Ingest data into persistence local database",
          short = "d"
        )
        .orFalse

      val resultFlag = Opts
        .flag(
          "result",
          "Calculates GDP and PPP from the World Bank API",
          short = "r"
        )
        .orFalse

      (ingestFlag, resultFlag).mapN(Results)

    }

  val showCommandsOpts: Opts[ShowProcesses] =
    Opts.subcommand("ps", "Lists all available commands!") {
      Opts
        .flag("all", "Whether to show all running processes.", short = "a")
        .orFalse
        .map(ShowProcesses)
    }

  def transactorResource: Resource[IO, H2Transactor[IO]] =
    for {
      ce         <- ExecutionContexts.fixedThreadPool[IO](32)
      path        = new java.io.File(".").getAbsolutePath
      transactor <- H2Transactor.newH2Transactor[IO](
                      url = s"jdbc:h2:${path}/pecunia:worldbank",
                      user = "autoscheduler",
                      pass = "",
                      connectEC = ce
                    )
    } yield transactor

}
