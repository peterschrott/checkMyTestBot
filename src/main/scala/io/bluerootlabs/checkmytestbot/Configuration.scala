package io.bluerootlabs.checkmytestbot

import com.typesafe.config.{Config, ConfigFactory}

case object Configuration {

  private val config: Config = ConfigFactory.load(getClass.getClassLoader)

  final val Port: Int = config.getInt("checkMyTestBot.port")
  final val WebhookUrl: String = config.getString("checkMyTestBot.webhookUrl")
  final val TelegramBotToken: String = config.getString("checkMyTestBot.telegramBotToken")

  final val FeedbackChatId: String = config.getString("checkMyTestBot.feedbackChatId")

  final val AlgoliaApplicationId: String = config.getString("checkMyTestBot.algolia.applicationId")
  final val AlgoliaApiKey: String = config.getString("checkMyTestBot.algolia.apiKey")
}
