package com.worldbank.clients

import wvlet.airframe.launcher.{command, option}

class Client(
    @option(
      prefix = "-d,--dataload",
      description = "perform data ingestion from Worldbank"
    )
    data: Option[String],
    @option(
      prefix = "-r,--results",
      description =
        "perform data queries and print to the console the two results"
    )
    result: Option[String],
    @option(prefix = "-h,--help", description = "show help", isHelp = true)
    displayHelp: Boolean
) {
  @command(isDefault = true)
  def default(): Seq[Unit] = {
    for {
      _ <- "Query should go here"
      _ <- "Results should go here"
    } yield println("Data loaded, this are the results")
  }
}
