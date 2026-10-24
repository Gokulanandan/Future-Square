package app.futuresquare.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PredictorTest {
    // The notebook sketch: ambitions D, E, P; rank 1-3; houses P, V, F, H, J; friends A, B, C.
    private val sketch = listOf(
        Field(1, FieldKind.AMBITION, "Ambitions", Side.BOTTOM, listOf("D", "E", "P")),
        Field(2, FieldKind.RANK, "Rank", Side.LEFT, listOf("1", "2", "3")),
        Field(3, FieldKind.HOUSE, "Kind of house", Side.TOP, listOf("P", "V", "F", "H", "J")),
        Field(4, FieldKind.FRIEND, "Best friend", Side.RIGHT, listOf("A", "B", "C")),
    )

    @Test
    fun fiveLinesStrikesRankTwoFirst() {
        val p = Predictor.predict(sketch, 5)
        val first = p.ring[p.steps.first().struck]
        assertEquals(Slot(2, 1), first) // Rank "2"
    }

    @Test
    fun secondStrikeContinuesAfterTheFirst() {
        val p = Predictor.predict(sketch, 5)
        // After Rank 2: count 1, P, V, F, H -> strike H.
        assertEquals(Slot(3, 3), p.ring[p.steps[1].struck])
    }

    @Test
    fun everyFieldEndsWithOneValue() {
        for (n in 1..30) {
            val p = Predictor.predict(sketch, n)
            assertEquals(sketch.map { it.id }.toSet(), p.winners.keys, "n=$n")
            assertEquals(sketch.sumOf { it.values.size } - sketch.size, p.steps.size, "n=$n")
        }
    }

    @Test
    fun closedFieldsAreNeverCounted() {
        val p = Predictor.predict(sketch, 7)
        val left = sketch.associate { it.id to it.values.size }.toMutableMap()
        for (step in p.steps) {
            for (i in step.path) assertTrue(left.getValue(p.ring[i].fieldId) > 1)
            left[p.ring[step.struck].fieldId] = left.getValue(p.ring[step.struck].fieldId) - 1
        }
    }

    @Test
    fun ringCountsBottomRightToLeftThenUpTheLeft() {
        val names = FutureSquare.ring(sketch).take(6).map { s -> sketch.first { it.id == s.fieldId }.values[s.valueIndex] }
        assertEquals(listOf("P", "E", "D", "3", "2", "1"), names)
    }

    @Test
    fun addedFieldsFillSidesUpToEight() {
        var fields = FutureSquare.classicFields()
        val extras = listOf(FieldKind.FOOD, FieldKind.PET, FieldKind.VEHICLE, FieldKind.SUPERPOWER, FieldKind.PLACE)
        extras.forEach { fields = FutureSquare.addField(fields, it) }
        assertEquals(8, fields.size)
        Side.entries.forEach { side -> assertEquals(2, fields.count { it.side == side }) }
        assertEquals(Side.TOP, fields[4].side)
    }

    @Test
    fun sentenceJoinsAllWinners() {
        val s = Sentence.build("You", sketch, mapOf(1 to "Engineer", 2 to "2", 3 to "Villa", 4 to "Sudar"))
        assertEquals("You will become an engineer, come 2nd in class, live in a villa and stay best friends with Sudar.", s)
    }
}
