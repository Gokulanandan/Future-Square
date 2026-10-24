package app.futuresquare.core

/** Turns the surviving values into one "future" sentence. */
object Sentence {
    fun build(who: String, fields: List<Field>, winners: Map<Int, String>): String {
        val parts = fields.mapNotNull { f -> winners[f.id]?.let { phrase(f, it) } }
        if (parts.isEmpty()) return "$who will have a surprising future."
        val joined = if (parts.size == 1) parts[0] else parts.dropLast(1).joinToString(", ") + " and " + parts.last()
        return "$who will $joined."
    }

    fun phrase(field: Field, v: String): String = when (field.kind) {
        FieldKind.AMBITION -> "become ${article(v)} ${v.lowercase()}"
        FieldKind.RANK -> v.toIntOrNull()?.let { "come ${ordinal(it)} in class" } ?: "get rank $v"
        FieldKind.HOUSE -> "live in ${article(v)} ${v.lowercase()}"
        FieldKind.FRIEND -> "stay best friends with $v"
        FieldKind.FOOD -> "eat ${v.lowercase()} every day"
        FieldKind.PET -> "have a pet ${v.lowercase()}"
        FieldKind.VEHICLE -> "ride ${article(v)} ${v.lowercase()}"
        FieldKind.SUPERPOWER -> "have ${v.lowercase()} as a superpower"
        FieldKind.PLACE -> "travel to $v"
        FieldKind.CUSTOM -> "get $v for “${field.name}”"
    }

    fun article(word: String): String = if (word.firstOrNull()?.lowercaseChar() in "aeiou") "an" else "a"

    fun ordinal(n: Int): String {
        val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
            1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th"
        }
        return "$n$suffix"
    }
}
