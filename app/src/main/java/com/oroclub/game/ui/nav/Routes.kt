package com.oroclub.game.ui.nav

import android.net.Uri

/**
 * All navigation destinations. Argument builders encode their inputs so a value
 * can never break the route pattern.
 */
object Routes {

    const val MAIN = "main"
    const val MENU = "menu"
    const val QUIZ_CATEGORIES = "quiz_categories"
    const val LEVELS = "levels"
    const val RESULTS = "results"
    const val SETTINGS = "settings"
    const val RULES = "rules"

    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_SCORE = "score"
    const val ARG_LEVEL = "level"
    const val ARG_MATCHED = "matched"
    const val ARG_TOTAL = "total"
    const val ARG_WON = "won"

    const val QUIZ = "quiz/{$ARG_CATEGORY_ID}"
    const val QUIZ_RESULTS = "quiz_results/{$ARG_CATEGORY_ID}/{$ARG_SCORE}"
    const val PAIRS = "pairs/{$ARG_LEVEL}"
    const val PAIRS_RESULTS = "pairs_results/{$ARG_LEVEL}/{$ARG_MATCHED}/{$ARG_TOTAL}/{$ARG_WON}"

    fun quiz(categoryId: String): String = "quiz/${Uri.encode(categoryId)}"

    fun quizResults(categoryId: String, score: Int): String =
        "quiz_results/${Uri.encode(categoryId)}/$score"

    fun pairs(level: Int): String = "pairs/$level"

    fun pairsResults(level: Int, matched: Int, total: Int, won: Boolean): String =
        "pairs_results/$level/$matched/$total/${if (won) 1 else 0}"
}
