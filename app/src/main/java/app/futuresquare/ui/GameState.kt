package app.futuresquare.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.futuresquare.core.Field
import app.futuresquare.core.FutureSquare
import app.futuresquare.core.Prediction
import app.futuresquare.core.Predictor

enum class Screen { CREATOR, DESK, FIELDS_CHOICE, PICK_FIELDS, VALUES, BEGIN, STRIKING, RESULT, WHO }

const val YOU = "You"

class GameState(private val store: AvatarStore) {
    var screen by mutableStateOf(if (store.hasPlayer()) Screen.DESK else Screen.CREATOR)
    var player by mutableStateOf(store.player())
        private set

    /** Whose future is being predicted, and the student drawn for them. */
    var who by mutableStateOf(YOU)
        private set
    var subject by mutableStateOf(store.player())
        private set

    /** Null while making the player's own student, otherwise the friend's name. */
    var creatingFor by mutableStateOf<String?>(null)

    var squareDrawn by mutableStateOf(false)
    var fields by mutableStateOf(FutureSquare.classicFields())
    var lines by mutableIntStateOf(0)
    var prediction by mutableStateOf<Prediction?>(null)
        private set
    var marks by mutableStateOf(BoardMarks())
    var skip by mutableStateOf(false)

    fun avatarBeingMade(): Avatar = creatingFor?.let { store.friend(it) ?: Avatar(girl = true, skin = 2, hairColor = 1, uniform = 2, bag = 1) } ?: player

    fun editStudent() {
        creatingFor = if (who == YOU) null else who
        screen = Screen.CREATOR
    }

    fun makeFriendStudent(name: String) {
        creatingFor = name.trim()
        screen = Screen.CREATOR
    }

    fun saveStudent(a: Avatar) {
        val name = creatingFor
        creatingFor = null
        if (name == null) {
            player = a
            store.savePlayer(a)
            if (who == YOU) subject = a
            screen = if (squareDrawn) Screen.FIELDS_CHOICE else Screen.DESK
        } else {
            store.saveFriend(name, a)
            if (who == name) {
                subject = a
                screen = Screen.FIELDS_CHOICE
            } else {
                newRound(name)
            }
        }
    }

    fun newRound(forWho: String) {
        who = forWho
        subject = if (forWho == YOU) player else store.friend(forWho) ?: avatarFor(forWho)
        fields = FutureSquare.classicFields()
        lines = 0
        prediction = null
        marks = BoardMarks()
        screen = Screen.FIELDS_CHOICE
    }

    fun updateField(id: Int, change: (Field) -> Field) {
        fields = fields.map { if (it.id == id) change(it) else it }
    }

    fun beginStrikes() {
        prediction = Predictor.predict(fields, lines.coerceAtLeast(1))
        marks = BoardMarks()
        skip = false
        screen = Screen.STRIKING
    }

    /** A friend without a saved student still gets a consistent look, picked from their name. */
    private fun avatarFor(name: String): Avatar {
        val h = name.lowercase().hashCode().let { if (it < 0) -it else it }
        return Avatar(
            girl = h % 2 == 0,
            skin = h / 2 % AvatarParts.skins.size,
            hairStyle = h / 7 % 3,
            hairColor = h / 11 % 3,
            glasses = h % 5 == 0,
            uniform = h / 13 % AvatarParts.uniforms.size,
            bag = h / 17 % AvatarParts.bags.size,
        )
    }
}
