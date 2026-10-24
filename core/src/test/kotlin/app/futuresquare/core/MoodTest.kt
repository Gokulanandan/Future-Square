package app.futuresquare.core

import kotlin.test.Test
import kotlin.test.assertEquals

class MoodTest {
    private val ranks = listOf("1", "2", "3")

    @Test
    fun topRankIsWowAndLastRankIsFunny() {
        assertEquals(Reaction.WOW, Mood.of(FieldKind.RANK, "1", ranks))
        assertEquals(Reaction.NONE, Mood.of(FieldKind.RANK, "2", ranks))
        assertEquals(Reaction.LAUGH, Mood.of(FieldKind.RANK, "3", ranks))
    }

    @Test
    fun lastOfOnlyTwoRanksIsNotMockedHarshly() {
        assertEquals(Reaction.NONE, Mood.of(FieldKind.RANK, "2", listOf("1", "2")))
    }

    @Test
    fun housesAndAmbitions() {
        assertEquals(Reaction.LAUGH, Mood.of(FieldKind.HOUSE, "Jail", emptyList()))
        assertEquals(Reaction.WOW, Mood.of(FieldKind.HOUSE, " Palace ", emptyList()))
        assertEquals(Reaction.WOW, Mood.of(FieldKind.AMBITION, "Pilot", emptyList()))
        assertEquals(Reaction.NONE, Mood.of(FieldKind.FRIEND, "Jail", emptyList()))
    }
}
