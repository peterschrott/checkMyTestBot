package io.bluerootlabs.checkmytestbot

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object CheckMyTestBotApp extends App {

  val bot = CheckMyTestBot
  val eol = bot.run()
  Await.result(eol, Duration.Inf)
}
