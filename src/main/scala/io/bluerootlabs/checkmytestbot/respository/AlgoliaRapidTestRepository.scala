package io.bluerootlabs.checkmytestbot.respository

import algolia.AlgoliaClient
import algolia.AlgoliaDsl._
import algolia.objects.Query
import io.bluerootlabs.checkmytestbot.Configuration
import io.bluerootlabs.checkmytestbot.model.RapidTestDto

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlgoliaRapidTestRepository() {

  private val client = new AlgoliaClient(Configuration.AlgoliaApplicationId, Configuration.AlgoliaApiKey)

  def findRapidTest(query: String): Future[Seq[RapidTestDto]] = {
    val eventualSearchResult = client.execute {
      search into "pei_rapid_test_comparison" query Query(
        query = Some(query),
        hitsPerPage = Some(10)
      )
    }
    eventualSearchResult.map(searchResult => searchResult.as[RapidTestDto])
  }
}
