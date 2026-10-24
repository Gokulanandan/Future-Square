package app.futuresquare.core

/** The four sides of the square, in the order the pencil counts them. */
enum class Side {
    BOTTOM, LEFT, TOP, RIGHT;

    /** Bottom is counted right to left and the left side bottom to top. */
    val countsBackwards: Boolean get() = this == BOTTOM || this == LEFT
}

enum class FieldKind(
    val title: String,
    val suggestions: List<String>,
    /** Where a classic field sits; null for fields the player adds. */
    val classicSide: Side? = null,
) {
    AMBITION("Ambitions", listOf("Doctor", "Engineer", "Pilot", "Teacher", "Astronaut", "Cricketer", "Actor", "Scientist", "Chef"), Side.BOTTOM),
    RANK("Rank", listOf("1", "2", "3", "4", "5"), Side.LEFT),
    HOUSE("Kind of house", listOf("Palace", "Villa", "Flat", "Hut", "Jail", "Treehouse", "Igloo"), Side.TOP),
    FRIEND("Best friend", emptyList(), Side.RIGHT),
    FOOD("Food", listOf("Biryani", "Dosa", "Pizza", "Noodles", "Ice cream", "Parotta")),
    PET("Pet", listOf("Dog", "Cat", "Parrot", "Rabbit", "Elephant", "Fish")),
    VEHICLE("Vehicle", listOf("Cycle", "Bike", "Car", "Jeep", "Helicopter", "Auto")),
    SUPERPOWER("Superpower", listOf("Flying", "Invisibility", "Super speed", "Time travel")),
    PLACE("Dream place", listOf("London", "Ooty", "Japan", "the Moon", "Paris")),
    CUSTOM("My own field", listOf("Option A", "Option B", "Option C"));

    val isClassic: Boolean get() = classicSide != null
}

data class Field(
    val id: Int,
    val kind: FieldKind,
    val name: String,
    val side: Side,
    val values: List<String>,
)

/** One value's spot on the ring the pencil walks around. */
data class Slot(val fieldId: Int, val valueIndex: Int)

object FutureSquare {
    const val MAX_FIELDS = 8
    const val MAX_PER_SIDE = 2
    const val MIN_VALUES = 2

    val countingOrder = listOf(Side.BOTTOM, Side.LEFT, Side.TOP, Side.RIGHT)
    private val addOrder = listOf(Side.TOP, Side.RIGHT, Side.LEFT, Side.BOTTOM)

    fun classicFields(): List<Field> =
        FieldKind.entries.filter { it.isClassic }.mapIndexed { i, kind ->
            Field(i + 1, kind, kind.title, kind.classicSide!!, defaultValues(kind))
        }

    fun defaultValues(kind: FieldKind): List<String> = when (kind) {
        FieldKind.FRIEND -> emptyList() // only the player knows their friends
        FieldKind.HOUSE -> kind.suggestions.take(5)
        FieldKind.CUSTOM -> kind.suggestions
        else -> kind.suggestions.take(3)
    }

    /** The side a new field goes on: the emptiest side, or null when the square is full. */
    fun sideForNewField(fields: List<Field>): Side? {
        if (fields.size >= MAX_FIELDS) return null
        return addOrder
            .filter { side -> fields.count { it.side == side } < MAX_PER_SIDE }
            .minByOrNull { side -> fields.count { it.side == side } }
    }

    fun canAdd(fields: List<Field>, kind: FieldKind): Boolean =
        sideForNewField(fields) != null && (kind == FieldKind.CUSTOM || fields.none { it.kind == kind })

    fun addField(fields: List<Field>, kind: FieldKind): List<Field> {
        if (!canAdd(fields, kind)) return fields
        val side = sideForNewField(fields)!!
        val id = (fields.maxOfOrNull { it.id } ?: 0) + 1
        var name = kind.title
        var n = 1
        while (fields.any { it.name == name }) name = "${kind.title} ${++n}"
        // A classic field added back goes to its own side when there is room.
        val home = kind.classicSide?.takeIf { s -> fields.count { it.side == s } < MAX_PER_SIDE } ?: side
        return fields + Field(id, kind, name, home, defaultValues(kind))
    }

    fun removeField(fields: List<Field>, id: Int): List<Field> =
        if (fields.size <= 2) fields else fields.filterNot { it.id == id }

    /** Every value in counting order: each side in turn, inner field first, then outer. */
    fun ring(fields: List<Field>): List<Slot> = countingOrder.flatMap { side ->
        fields.filter { it.side == side }.flatMap { field ->
            val indices = field.values.indices.toList()
            (if (side.countsBackwards) indices.reversed() else indices).map { Slot(field.id, it) }
        }
    }

    fun fieldsMissingValues(fields: List<Field>): List<Field> = fields.filter { it.values.size < MIN_VALUES }
}
