package app.futuresquare.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.futuresquare.core.Field
import app.futuresquare.core.FutureSquare
import app.futuresquare.core.Prediction
import app.futuresquare.core.Predictor
import app.futuresquare.core.Reaction

enum class Screen { HEROES, CREATOR, DESK, FIELDS_CHOICE, PICK_FIELDS, VALUES, BEGIN, STRIKING, RESULT, WHO }

const val YOU = "You"
const val DEFAULT_HERO = "zappy"

class GameState(private val store: AvatarStore) {
    var screen by mutableStateOf(if (store.hero(null) != null) Screen.DESK else Screen.HEROES)
    var player by mutableStateOf(store.player())
        private set
    var playerLook by mutableStateOf(Look(store.hero(null) ?: DEFAULT_HERO, store.player()))
        private set

    /** Whose future is being predicted, and the hero or student who plays it. */
    var who by mutableStateOf(YOU)
        private set
    var subjectLook by mutableStateOf(playerLook)
        private set

    /** Null while making the player's own student, otherwise the friend's name. */
    var creatingFor by mutableStateOf<String?>(null)

    /** Null while picking the player's hero, otherwise the friend's name. */
    var pickingFor by mutableStateOf<String?>(null)
        private set

    var squareDrawn by mutableStateOf(false)
    var fields by mutableStateOf(FutureSquare.classicFields())
    var lines by mutableIntStateOf(0)
    var prediction by mutableStateOf<Prediction?>(null)
        private set
    var marks by mutableStateOf(BoardMarks())
    var skip by mutableStateOf(false)

    var dog by mutableStateOf<DogMoment?>(null)
        private set
    private var dogCount = 0

    var soundOn by mutableStateOf(store.soundOn().also { Sfx.enabled = it })
        private set

    fun toggleSound() {
        soundOn = !soundOn
        Sfx.enabled = soundOn
        store.setSoundOn(soundOn)
    }

    fun showDog(reaction: Reaction, caption: String) {
        if (reaction == Reaction.NONE) return
        dog = DogMoment(++dogCount, reaction, caption)
    }

    fun avatarBeingMade(): Avatar = creatingFor?.let { store.friend(it) ?: Avatar(girl = true, skin = 2, hairColor = 1, uniform = 2, bag = 1) } ?: player

    /** The look shown on the hero picker's own-student card. */
    fun studentFor(name: String?): Avatar = name?.let { store.friend(it) ?: avatarFor(it) } ?: player

    fun currentHeroFor(name: String?): String? = store.hero(name)

    fun editHero() {
        pickingFor = if (who == YOU) null else who
        screen = Screen.HEROES
    }

    fun pickFriendHero(name: String) {
        pickingFor = name.trim()
        screen = Screen.HEROES
    }

    /** Picking the student card opens the creator; any hero is saved straight away. */
    fun chooseHero(id: String) {
        val name = pickingFor
        if (id == Heroes.STUDENT) {
            creatingFor = name
            screen = Screen.CREATOR
            return
        }
        lookChosen(name, id)
    }

    fun skipHeroes() {
        val name = pickingFor
        lookChosen(name, store.hero(name) ?: name?.let { Heroes.forName(it).id } ?: DEFAULT_HERO)
    }

    /** Back from the picker, only when there is somewhere to go back to. */
    fun leaveHeroes() {
        if (pickingFor == null && store.hero(null) == null) return
        pickingFor = null
        screen = if (squareDrawn) Screen.FIELDS_CHOICE else Screen.DESK
    }

    /** Back from the student creator to the hero picker it was opened from. */
    fun leaveCreator() {
        pickingFor = creatingFor
        creatingFor = null
        screen = Screen.HEROES
    }

    private fun lookChosen(name: String?, id: String) {
        pickingFor = null
        store.saveHero(name, id)
        if (name == null) {
            playerLook = Look(id, player)
            if (who == YOU) subjectLook = playerLook
            screen = if (squareDrawn) Screen.FIELDS_CHOICE else Screen.DESK
        } else if (who == name) {
            subjectLook = lookFor(name)
            screen = Screen.FIELDS_CHOICE
        } else {
            newRound(name)
        }
    }

    fun saveStudent(a: Avatar) {
        val name = creatingFor
        creatingFor = null
        if (name == null) {
            player = a
            store.savePlayer(a)
        } else {
            store.saveFriend(name, a)
        }
        lookChosen(name, Heroes.STUDENT)
    }

    fun newRound(forWho: String) {
        who = forWho
        subjectLook = if (forWho == YOU) playerLook else lookFor(forWho)
        fields = FutureSquare.classicFields()
        lines = 0
        prediction = null
        marks = BoardMarks()
        dog = null
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

    /** A friend keeps the hero picked for them; otherwise their name picks one. */
    private fun lookFor(name: String): Look =
        Look(store.hero(name) ?: Heroes.forName(name).id, studentFor(name))

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
