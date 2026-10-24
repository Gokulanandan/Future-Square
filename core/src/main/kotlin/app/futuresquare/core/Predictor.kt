package app.futuresquare.core

/** One round: the ring positions the pencil counted, ending on the one it strikes. */
data class StrikeStep(val path: List<Int>) {
    val struck: Int get() = path.last()
}

data class Prediction(
    val ring: List<Slot>,
    val steps: List<StrikeStep>,
    /** Field id to the value left standing. */
    val winners: Map<Int, String>,
)

object Predictor {
    /**
     * Plays the whole game for a line count [lines]. Counting starts at the first slot of the
     * ring, skips struck values and closed fields (one value left), and carries on from the
     * value after each strike until every field is closed.
     */
    fun predict(fields: List<Field>, lines: Int): Prediction {
        require(lines >= 1) { "Need at least one line" }
        val ring = FutureSquare.ring(fields)
        val alive = BooleanArray(ring.size) { true }
        val left = fields.associate { it.id to it.values.size }.toMutableMap()
        fun open(i: Int) = alive[i] && left.getValue(ring[i].fieldId) > 1

        val steps = mutableListOf<StrikeStep>()
        var pos = 0
        while (ring.indices.any(::open)) {
            val path = mutableListOf<Int>()
            var i = pos
            while (path.size < lines) {
                if (open(i)) path += i
                if (path.size < lines) i = (i + 1) % ring.size
            }
            val struck = path.last()
            alive[struck] = false
            left[ring[struck].fieldId] = left.getValue(ring[struck].fieldId) - 1
            steps += StrikeStep(path)
            pos = (struck + 1) % ring.size
        }

        val winners = fields.mapNotNull { field ->
            ring.indices.firstOrNull { alive[it] && ring[it].fieldId == field.id }
                ?.let { field.id to field.values[ring[it].valueIndex] }
        }.toMap()
        return Prediction(ring, steps, winners)
    }
}
