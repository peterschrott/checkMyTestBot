package io.bluerootlabs.checkmytestbot

import com.bot4s.telegram.api._
import com.bot4s.telegram.api.declarative.{Commands, InlineQueries}
import com.bot4s.telegram.clients.AkkaHttpClient
import io.bluerootlabs.checkmytestbot.service.CheckMyTestService

import scala.concurrent.Future

object CheckMyTestBot extends AkkaTelegramBot
  with Webhook
  with Commands[Future]
  with InlineQueries[Future] {

  override val port: Int = Configuration.Port

  override val webhookUrl: String = Configuration.WebhookUrl
  override val client: AkkaHttpClient = new AkkaHttpClient(Configuration.TelegramBotToken)

  val service: CheckMyTestService = new CheckMyTestService()

  onCommand("start") { implicit msg => service.handleCommandStart() }

  onCommand("suche") { implicit msg => service.handleCommandSearch() }

  onCommand("info") { implicit msg => service.handleCommandInfo() }

  onCommand("coffee") { implicit msg => service.handleCommandCoffee() }

  onCommand("feedback") { implicit msg => service.handleCommandFeedback() }

  onMessage { implicit msg => service.handleMessage() }

  onInlineQuery { implicit action => service.handleInlineQuery() }
}
