package io.bluerootlabs.checkmytestbot.model

import algolia.responses.{HighlightResult, Hit, RankingInfo, SnippetResult}

case class RapidTestDto(objectID: String,
                        atNo: String,
                        idNo: String,
                        idNo2: Option[String],
                        idNo3: Option[String],
                        manufacturer: String,
                        testName: String,
                        cqLow: String,
                        cqMid: String,
                        cqHigh: String,
                        totalSensitivity: String,
                        passedSensitivityCriteria: String,
                        _highlightResult: Option[Map[String, HighlightResult]],
                        _snippetResult: Option[Map[String, SnippetResult]],
                        _rankingInfo: Option[RankingInfo],
                        _distinctSeqID: Option[Integer]) extends Hit {
}
