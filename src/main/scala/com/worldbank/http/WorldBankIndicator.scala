package com.worldbank.http

sealed trait WorldBankIndicator
object WorldBankIndicator {
  case object GDP extends WorldBankIndicator
  case object PPP extends WorldBankIndicator
}
