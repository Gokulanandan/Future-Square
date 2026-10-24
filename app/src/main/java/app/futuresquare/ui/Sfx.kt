package app.futuresquare.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Small sound effects, synthesised in code so the app ships no audio files.
 * Each sound is rendered once into 16-bit mono PCM and played with a static AudioTrack.
 */
object Sfx {
    var enabled = true

    private const val RATE = 22050
    private val handler = Handler(Looper.getMainLooper())

    private val scratch by lazy { render(0.13) { t, _ -> noise() * env(t, 0.005, 0.13) * 0.22 } }
    private val tick by lazy { render(0.03) { t, _ -> sine(1900.0, t) * env(t, 0.001, 0.03) * 0.18 } }
    private val line by lazy { render(0.05) { t, _ -> (sine(320.0, t) + noise() * 0.4) * env(t, 0.002, 0.05) * 0.2 } }
    private val chime by lazy { notes(listOf(1318.5 to 0.0, 1760.0 to 0.09), 0.5, 0.3) }
    private val fanfare by lazy { notes(listOf(523.3 to 0.0, 659.3 to 0.11, 784.0 to 0.22, 1046.5 to 0.33), 0.9, 0.28) }
    private val wow by lazy {
        render(0.7) { t, _ ->
            val f = 380.0 + 900.0 * (t / 0.45).coerceAtMost(1.0)
            (sine(f, t) + 0.3 * sine(2 * f, t)) * env(t, 0.02, 0.7) * 0.3 + sparkle(t)
        }
    }
    private val laugh by lazy {
        render(0.9) { t, _ ->
            // Three "ha"s, each a short falling buzzy note.
            val beat = 0.28
            val k = (t / beat).toInt()
            val local = t - k * beat
            if (k > 2 || local > 0.2) 0.0 else {
                val f = 520.0 - k * 50 - local * 400
                val buzz = sine(f, t) + 0.5 * sine(2 * f, t) + 0.25 * sine(3 * f, t)
                buzz * env(local, 0.01, 0.2) * 0.26
            }
        }
    }

    fun scratch() = play(scratch)
    fun tick() = play(tick)
    fun line() = play(line)
    fun chime() = play(chime)
    fun fanfare() = play(fanfare)
    fun wow() = play(wow)
    fun laugh() = play(laugh)

    private fun play(pcm: ShortArray) {
        if (!enabled) return
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            handler.postDelayed({ track.release() }, pcm.size * 1000L / RATE + 200)
        } catch (e: Exception) {
            // Sound is a nicety; never let it break the game.
        }
    }

    private fun render(seconds: Double, sample: (t: Double, i: Int) -> Double): ShortArray {
        val n = (seconds * RATE).toInt()
        return ShortArray(n) { i ->
            val v = sample(i.toDouble() / RATE, i).coerceIn(-1.0, 1.0)
            (v * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /** Bell-like notes starting at the given offsets. */
    private fun notes(list: List<Pair<Double, Double>>, seconds: Double, volume: Double) = render(seconds) { t, _ ->
        list.sumOf { (f, start) ->
            val local = t - start
            if (local < 0) 0.0 else (sine(f, local) + 0.35 * sine(2 * f, local) + 0.15 * sine(3 * f, local)) * exp(-local * 7)
        } * volume
    }

    private fun sine(f: Double, t: Double) = sin(2 * PI * f * t)
    private fun noise() = Random.nextDouble(-1.0, 1.0)
    private fun env(t: Double, attack: Double, length: Double): Double = when {
        t < attack -> t / attack
        t > length -> 0.0
        else -> exp(-4 * (t - attack) / length)
    }
    private fun sparkle(t: Double): Double = if (t < 0.45) 0.0 else sine(2637.0 + 400 * sin(t * 60), t) * exp(-(t - 0.45) * 10) * 0.12
}
