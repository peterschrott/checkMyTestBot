package io.bluerootlabs.checkmytestbot.service

import com.bot4s.telegram.Implicits._
import com.bot4s.telegram.methods.ParseMode.HTML
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models._
import io.bluerootlabs.checkmytestbot.CheckMyTestBot.{answerInlineQuery, reply, request}
import io.bluerootlabs.checkmytestbot.Configuration.FeedbackChatId
import io.bluerootlabs.checkmytestbot.respository.AlgoliaRapidTestRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckMyTestService() {

  import CheckMyTestService._

  val repo: AlgoliaRapidTestRepository = new AlgoliaRapidTestRepository()

  def handleCommandInfo()(implicit msg: Message): Future[Unit] = {
    val text = s"$InfoText \n\n $ByText"
    replyTextWithSearchInlineKb(text)
  }

  def handleCommandStart()(implicit msg: Message): Future[Unit] = {
    val text = InfoText
    replyTextWithSearchInlineKb(text)
  }

  def handleCommandSearch()(implicit msg: Message): Future[Unit] = {
    val text = "Finde jetzt deinen Corona Schnelltest: "
    replyTextWithSearchInlineKb(text)
  }

  def handleCommandCoffee()(implicit msg: Message): Future[Unit] = {
    val text = CoffeeText
    val inlineKbMarkup = InlineKeyboardMarkup(Seq(Seq(SearchInlineKbBtn)))
    val eventualReplied = reply(text, replyMarkup = inlineKbMarkup, parseMode = HTML, disableWebPagePreview = true)
    eventualReplied.map(_ => ())
  }

  def handleCommandFeedback()(implicit msg: Message): Future[Unit] = {
    val text = FeedbackText
    val forceRpl = ForceReply()
    val eventualReplied = reply(text, replyMarkup = forceRpl)
    eventualReplied.map(_ => ())
  }

  def handleMessage()(implicit msg: Message): Future[Unit] = {
    val isCommand = msg.entities.exists(_.exists(_.`type` == MessageEntityType.BotCommand))
    if (!isCommand) {
      if (msg.replyToMessage.exists(r => r.text.contains(FeedbackText))) {
        handleMessageFeedback()
      } else {
        Future.successful(())
      }
    } else {
      Future.successful(())
    }
  }

  def handleMessageFeedback()(implicit msg: Message): Future[Unit] = {
    msg.text.map { feedbackText =>
      val text = s"Feedback from CkMyTstBot: '$feedbackText'"
      println(text)
      request(SendMessage(FeedbackChatId, text))
    }
    val text = "Alles klar! Vielen Dank für dein Feedback. 💙"
    replyTextWithSearchInlineKb(text)
  }

  def handleInlineQuery()(implicit action: InlineQuery): Future[Unit] = {
    val eventualResult = {
      if (action.query.isEmpty) Future(Seq.empty)
      else buildResult(action.query)
    }
    for {
      result <- eventualResult
      _ <- answerInlineQuery(result, cacheTime = 1)
    } yield ()
  }

  private def buildResult(query: String): Future[Seq[InlineQueryResult]] = {
    val eventualResults = repo.findRapidTest(query)
    eventualResults.map { results =>
      results.map { result =>
        val inlineKbMarkup = InlineKeyboardMarkup(Seq(Seq(SearchAgainInlineKbBtn)))

        val idNos = List(Option(result.idNo), result.idNo2, result.idNo3).map(_.filter(_.nonEmpty)).flatMap(l => l)

        val idNoStr = if (idNos.isEmpty) "n.a." else idNos.mkString(", ")

        val passedSensitivityCriteriaStr =
          if (result.passedSensitivityCriteria == "y") "✅ Sensitivitätskriterium erfüllen"
          else "❌ Sensitivitätskriterium NICHT erfüllen"

        val omicronDetectionStr =
          if (result.omicronDetection == "y") "Erkennt auch Omikron entsprechend der Bridging Prüfung!"
          else "<b>Keine</b> Omikron Erkennung entsprechend der Bridging Prüfung"

        val targetAntigenStr = {
          if (result.targetAntigen == "N") "Nukleocapsid Protein"
          else if (result.targetAntigen == "S") "Spike Protein"
          else if (result.targetAntigen == "S+N") "Nukleocapsid Protein & Spike Protein"
          else "n.a."
        }

        val inputMessage =
          s"""
             |<u><b>Dein Test:</b></u>
             |
             |<b>${result.testName}</b>
             |von <b>${result.manufacturer}</b>
             |
             |Ref-Nr.: $idNoStr
             |
             |Sensitivität bei Viruslast
             | 👉 hoch: ${result.cqLow} (CQ ≤ 25)
             | 👉 mäßig: ${result.cqMid} (CQ 25 - 30)
             | 👉 gering: ${result.cqHigh} (CQ ≥ 30)
             |
             |Gesamt Sensitivität: ${result.totalSensitivity}
             |
             |<b>$passedSensitivityCriteriaStr</b>
             |
             |🦠 $omicronDetectionStr
             |
             |Zielantigen: $targetAntigenStr
             |
             |<i>
             |Quelle: Paul-Ehrlich-Institut (stand: $LastUpdate)
             |</i>
             |""".stripMargin

        InlineQueryResultArticle(
          result.objectID,
          result.testName,
          description = s"von ${result.manufacturer}",
          inputMessageContent = InputTextMessageContent(inputMessage, parseMode = HTML),
          replyMarkup = inlineKbMarkup,
          thumbWidth = 1000
        )
      }
    }
  }

  private def replyTextWithSearchInlineKb(text: String)(implicit msg: Message): Future[Unit] = {
    val inlineKbMarkup = InlineKeyboardMarkup(Seq(Seq(SearchInlineKbBtn)))
    val eventualReplied = reply(text, replyMarkup = inlineKbMarkup, parseMode = HTML)
    eventualReplied.map(_ => ())
  }
}

object CheckMyTestService {

  final val LastUpdate: String = "01.04.2022"

  final val FeedbackText: String =
    """
      |Hast du Anregungen oder Feedback? Dann schick sie mir jetzt einfach als Antwort.
      |""".stripMargin.trim

  final val CoffeeText: String =
    """
      |Bei den Servern muss ich ab und an eine Münze nachwerfen 💰 - bei mir einen Kaffee ☕️
      |
      |👉 https://ko-fi.com/peterschrott
      |
      |Deine Spende hilft natürlich!
      |
      |Vielen Dank 🤙
      |
      |""".stripMargin.trim

  final val InfoText: String =
    s"""
      |Dieser Bot zeigt dir Informationen zur Qualität von Corona Schnelltests – auch Selbsttests oder Antigentests genannt – und von Tests aus Testzentren an. 💡
      |
      |Drücke dazu einfach auf "🔎 Test suchen" und gib entweder den <b>Namen</b>, den <b>Hersteller</b> oder die <b>Referenznummer [REF]</b> deines Schnelltests ein. Wähle danach den entsprechenden Test aus den Suchergebnissen.
      |
      |Die angezeigten Informationen stammen aus der vergleichenden Evaluierung der Sensitivität von SARS-CoV-2-Antigenschnelltests des Paul-Ehrlich-Institutes.
      | 👉 https://www.pei.de/DE/newsroom/dossier/coronavirus/testsysteme.html
      |
      | Aktualisiert am $LastUpdate
      | """.stripMargin.trim

  final val ByText: String =
    """
      |made with 💙 in Berlin by Peter
      |
      |
      |<i>
      |Credits:
      |Covid test icons created by Freepik - Flaticon
      |</i>
      |""".stripMargin.trim

  final val SearchInlineKbBtn = InlineKeyboardButton("🔎 Test suchen", switchInlineQueryCurrentChat = "")

  final val SearchAgainInlineKbBtn = InlineKeyboardButton("🔎 Weiter suchen", switchInlineQueryCurrentChat = "")
}
