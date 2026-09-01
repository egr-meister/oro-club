package com.oroclub.game

import com.oroclub.game.domain.QuizContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizContentTest {

    @Test
    fun `three categories ship and all of them survive validation`() {
        assertEquals(3, QuizContent.categories.size)
    }

    @Test
    fun `every category has ten questions with a valid correct index`() {
        QuizContent.categories.forEach { category ->
            assertEquals(
                "${category.id} question count",
                QuizContent.QUESTIONS_PER_CATEGORY,
                category.questions.size
            )
            category.questions.forEach { question ->
                assertTrue("${category.id}: empty question", question.text.isNotBlank())
                assertTrue("${category.id}: too few options", question.options.size >= 2)
                assertTrue(
                    "${category.id}: correctIndex out of range",
                    question.correctIndex in question.options.indices
                )
                assertEquals(
                    "${category.id}: duplicate options",
                    question.options.size,
                    question.options.distinct().size
                )
            }
        }
    }

    @Test
    fun `category ids are unique and resolvable`() {
        val ids = QuizContent.categories.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        ids.forEach { id -> assertNotNull(QuizContent.categoryById(id)) }
    }

    @Test
    fun `unknown null and blank category ids resolve to null`() {
        assertNull(QuizContent.categoryById(null))
        assertNull(QuizContent.categoryById(""))
        assertNull(QuizContent.categoryById("   "))
        assertNull(QuizContent.categoryById("no_such_deck"))
    }

    @Test
    fun `badge item indices point at shipped item art`() {
        QuizContent.categories.forEach { category ->
            assertTrue(category.badgeItemIndex in 1..12)
        }
    }
}
