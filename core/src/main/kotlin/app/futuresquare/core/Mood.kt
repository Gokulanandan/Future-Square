package app.futuresquare.core

/** How the dog mascot reacts when a field ends on a value. */
enum class Reaction { WOW, LAUGH, NONE }

object Mood {
    private val wow = mapOf(
        FieldKind.AMBITION to setOf(
            "doctor", "engineer", "pilot", "astronaut", "scientist", "actor", "cricketer", "collector",
            "ias officer", "ips officer", "ceo", "president", "prime minister", "lawyer", "judge", "army officer",
        ),
        FieldKind.HOUSE to setOf("palace", "villa", "castle", "mansion"),
        FieldKind.VEHICLE to setOf("helicopter", "jeep", "car", "plane", "rocket"),
        FieldKind.PET to setOf("elephant", "parrot"),
        FieldKind.SUPERPOWER to setOf("flying", "time travel"),
        FieldKind.PLACE to setOf("the moon", "japan", "london", "paris"),
    )
    private val laugh = mapOf(
        FieldKind.HOUSE to setOf("jail", "hut"),
        FieldKind.VEHICLE to setOf("cycle"),
    )

    /** [allValues] is everything the field started with, so "last rank" can be worked out. */
    fun of(kind: FieldKind, value: String, allValues: List<String>): Reaction {
        if (kind == FieldKind.RANK) {
            val rank = value.trim().toIntOrNull() ?: return Reaction.NONE
            val worst = allValues.mapNotNull { it.trim().toIntOrNull() }.maxOrNull() ?: return Reaction.NONE
            return when {
                rank == 1 -> Reaction.WOW
                rank == worst && worst >= 3 -> Reaction.LAUGH
                else -> Reaction.NONE
            }
        }
        val v = value.trim().lowercase()
        return when {
            wow[kind]?.contains(v) == true -> Reaction.WOW
            laugh[kind]?.contains(v) == true -> Reaction.LAUGH
            else -> Reaction.NONE
        }
    }
}
