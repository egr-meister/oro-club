package com.oroclub.game.domain

/** One quiz question with exactly one correct answer index. */
data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

/** A themed set of exactly [QuizContent.QUESTIONS_PER_CATEGORY] questions. */
data class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    /** Drawable-independent index (1..12) of the item art used as the category badge. */
    val badgeItemIndex: Int,
    val questions: List<Question>
)

/**
 * ---------------------------------------------------------------------------
 * PLACEHOLDER CONTENT — REPLACEABLE.
 *
 * These 3 x 10 questions are themed drafts written to ship a complete, playable
 * build. They are ordinary Kotlin data: swap the strings (and `correctIndex`)
 * for the final copy without touching any other file. Question count per
 * category must stay at QUESTIONS_PER_CATEGORY (10) and `correctIndex` must
 * always point inside `options`; both are asserted by the loader below.
 *
 * Oro Club makes no real-money, gambling or prize claims. The questions are
 * general-knowledge trivia dressed in a joker / playing-card / luck theme.
 * ---------------------------------------------------------------------------
 */
object QuizContent {

    const val QUESTIONS_PER_CATEGORY: Int = 10

    private val jokersCourt = QuizCategory(
        id = "jokers_court",
        title = "Joker's Court",
        subtitle = "Jesters, masks and mischief",
        badgeItemIndex = 1,
        questions = listOf(
            Question(
                "What is the Joker card also commonly called in English decks?",
                listOf("The Fool", "The Wild Card", "The Knave", "The Herald"),
                1
            ),
            Question(
                "A court jester's hat with three drooping points and bells is known as a…",
                listOf("Mitre", "Cap and bells", "Coronet", "Tricorne"),
                1
            ),
            Question(
                "In a standard 54-card deck, how many Jokers are included?",
                listOf("One", "Two", "Three", "Four"),
                1
            ),
            Question(
                "Which tarot card is the historical ancestor of the modern Joker?",
                listOf("The Fool", "The Magician", "The Star", "The Wheel"),
                0
            ),
            Question(
                "A performer who juggles, tumbles and jokes at a royal court is a…",
                listOf("Bard", "Jester", "Steward", "Herald"),
                1
            ),
            Question(
                "The Joker was added to card decks in the 1860s for which game?",
                listOf("Whist", "Euchre", "Baccarat", "Piquet"),
                1
            ),
            Question(
                "Masks worn at a masquerade covering only the eyes are called…",
                listOf("Domino masks", "Visors", "Gorgets", "Cowls"),
                0
            ),
            Question(
                "In many games the Joker can substitute for any card. That role is called…",
                listOf("Trump", "Wild", "Void", "Anchor"),
                1
            ),
            Question(
                "Which colour pair is most traditional for a jester's motley costume?",
                listOf("Green and white", "Gold and black", "Grey and brown", "Pink and teal"),
                1
            ),
            Question(
                "The small bells sewn to a jester's hat are there mainly to…",
                listOf("Announce movement", "Ward off insects", "Measure time", "Balance the hat"),
                0
            )
        )
    )

    private val cardTable = QuizCategory(
        id = "card_table",
        title = "The Card Table",
        subtitle = "Suits, ranks and old rules",
        badgeItemIndex = 3,
        questions = listOf(
            Question(
                "How many cards are in a standard deck without Jokers?",
                listOf("48", "50", "52", "54"),
                2
            ),
            Question(
                "Which of these is NOT one of the four French suits?",
                listOf("Hearts", "Clubs", "Shields", "Spades"),
                2
            ),
            Question(
                "How many face cards are in a standard 52-card deck?",
                listOf("Eight", "Twelve", "Sixteen", "Twenty"),
                1
            ),
            Question(
                "In poker, which hand ranks highest?",
                listOf("Full house", "Four of a kind", "Straight flush", "Flush"),
                2
            ),
            Question(
                "The Ace of Spades is traditionally the most decorated card because it carried…",
                listOf("A tax stamp", "The printer's prayer", "A royal seal", "A player's initials"),
                0
            ),
            Question(
                "How many cards of each rank appear in a 52-card deck?",
                listOf("Two", "Three", "Four", "Six"),
                2
            ),
            Question(
                "Which suit is traditionally paired with Spades as a black suit?",
                listOf("Hearts", "Diamonds", "Clubs", "None"),
                2
            ),
            Question(
                "A 'flush' in poker means all five cards share the same…",
                listOf("Rank", "Suit", "Colour", "Value"),
                1
            ),
            Question(
                "Riffling a deck to randomise it is called a…",
                listOf("Cut", "Shuffle", "Deal", "Fold"),
                1
            ),
            Question(
                "In most Western decks, which card sits between the Jack and the King?",
                listOf("Ten", "Queen", "Ace", "Knight"),
                1
            )
        )
    )

    private val ladyLuck = QuizCategory(
        id = "lady_luck",
        title = "Lady Luck",
        subtitle = "Charms, odds and old superstitions",
        badgeItemIndex = 4,
        questions = listOf(
            Question(
                "How many pips are on a standard six-sided die in total?",
                listOf("18", "21", "24", "27"),
                1
            ),
            Question(
                "Opposite faces of a standard die always add up to…",
                listOf("Five", "Six", "Seven", "Eight"),
                2
            ),
            Question(
                "A four-leaf clover is rare because most clovers have how many leaves?",
                listOf("Two", "Three", "Five", "Six"),
                1
            ),
            Question(
                "Hanging a horseshoe with the ends pointing up is said to…",
                listOf("Hold the luck in", "Let the luck out", "Repel visitors", "Mark a stable"),
                0
            ),
            Question(
                "Rolling two standard dice, which total is the most likely?",
                listOf("Six", "Seven", "Eight", "Twelve"),
                1
            ),
            Question(
                "The fear of the number thirteen is called…",
                listOf("Triskaidekaphobia", "Trypophobia", "Tessarophobia", "Hexakosiophobia"),
                0
            ),
            Question(
                "A single coin flip has what chance of landing heads?",
                listOf("One in two", "One in three", "One in four", "One in six"),
                0
            ),
            Question(
                "Which gemstone is traditionally called the stone of good fortune?",
                listOf("Jade", "Onyx", "Opal", "Amber"),
                0
            ),
            Question(
                "In many cultures, which item is kept in a pocket as a luck charm?",
                listOf("A rabbit's foot", "A spoon", "A candle", "A ribbon"),
                0
            ),
            Question(
                "Rolling two sixes with a pair of dice happens on average once every…",
                listOf("12 rolls", "24 rolls", "36 rolls", "48 rolls"),
                2
            )
        )
    )

    /**
     * Every category the app ships, already validated. A category whose data is
     * malformed (wrong question count or an out-of-range correct index) is
     * dropped instead of crashing the screen that renders it.
     */
    val categories: List<QuizCategory> =
        listOf(jokersCourt, cardTable, ladyLuck).filter { category ->
            category.questions.size == QUESTIONS_PER_CATEGORY &&
                category.questions.all { it.options.isNotEmpty() && it.correctIndex in it.options.indices }
        }

    fun categoryById(id: String?): QuizCategory? =
        if (id.isNullOrBlank()) null else categories.firstOrNull { it.id == id }
}
