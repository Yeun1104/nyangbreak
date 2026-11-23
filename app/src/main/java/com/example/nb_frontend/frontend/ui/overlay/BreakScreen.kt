package com.example.nb_frontend.frontend.ui.overlay

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nb_frontend.R
import com.example.nb_frontend.backend.block.BreakUiState
import com.example.nb_frontend.backend.data.settings.CatCharacter
import com.example.nb_frontend.frontend.ui.theme.Jua
import com.example.nb_frontend.frontend.ui.theme.NbOrange50
import com.example.nb_frontend.frontend.ui.theme.NbWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val HOLD_TO_RELEASE_MS = 3_000L
private const val DEFAULT_OUTRO_MS = 10_000L
private const val DEFAULT_LOOP_MS = 10_000L

private enum class CatPhase { INTRO, LOOP, OUTRO }

private data class CatClips(
    @RawRes val intro: Int,
    @RawRes val loop: Int,
    @RawRes val outro: Int,
)

private fun clipsFor(cat: CatCharacter): CatClips = when (cat) {
    CatCharacter.TABBY -> CatClips(
        R.raw.cat_tabby_intro, R.raw.cat_tabby_loop, R.raw.cat_tabby_outro
    )
    CatCharacter.GRAY_WHITE -> CatClips(
        R.raw.cat_orange_intro, R.raw.cat_orange_loop, R.raw.cat_orange_outro
    )
    CatCharacter.TUXEDO -> CatClips(
        R.raw.cat_tuxedo_intro, R.raw.cat_tuxedo_loop, R.raw.cat_tuxedo_outro
    )
}

@Composable
fun BreakScreen(
    state: BreakUiState,
    onRelease: () -> Unit
) {
    val supportsAnimation = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    val context = LocalContext.current
    val clips = remember(state.cat) { clipsFor(state.cat) }

    var phase by remember { mutableStateOf(CatPhase.INTRO) }
    var outroFinished by remember { mutableStateOf(false) }
    var releaseAfterOutro by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var outroMillis by remember(clips) { mutableLongStateOf(DEFAULT_OUTRO_MS) }
    var loopMillis by remember(clips) { mutableLongStateOf(DEFAULT_LOOP_MS) }
    var loopRound by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val latestOnRelease by rememberUpdatedState(onRelease)
    val latestRemaining by rememberUpdatedState(state.remainingMillis)

    LaunchedEffect(clips) {
        val (outro, loop) = withContext(Dispatchers.IO) {
            webpDurationMillis(context, clips.outro) to webpDurationMillis(context, clips.loop)
        }
        outroMillis = outro ?: DEFAULT_OUTRO_MS
        loopMillis = loop ?: DEFAULT_LOOP_MS
    }

    fun shouldStartOutro(): Boolean =
        latestRemaining > 0 && latestRemaining - outroMillis < loopMillis / 2

    LaunchedEffect(state.remainingMillis, outroMillis) {
        if (phase == CatPhase.INTRO && state.remainingMillis in 1..outroMillis) {
            phase = CatPhase.OUTRO
        }
    }

    fun onHoldComplete() {
        when {
            !supportsAnimation || outroFinished -> latestOnRelease()
            phase != CatPhase.OUTRO -> {
                releaseAfterOutro = true
                phase = CatPhase.OUTRO
            }
            else -> releaseAfterOutro = true
        }
    }

    val holdModifier = if (state.canRelease) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                val job: Job = scope.launch {
                    val start = System.currentTimeMillis()
                    while (true) {
                        val p = ((System.currentTimeMillis() - start).toFloat() / HOLD_TO_RELEASE_MS)
                            .coerceAtMost(1f)
                        holdProgress = p
                        if (p >= 1f) {
                            onHoldComplete()
                            break
                        }
                        delay(16)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
                if (holdProgress < 1f) holdProgress = 0f
            }
        }
    } else {
        Modifier
    }

    val totalSeconds = ((state.remainingMillis + 999) / 1000).toInt().coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NbWhite.copy(alpha = 0.9f))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                fontFamily = Jua,
                fontSize = 28.sp,
                color = NbOrange50
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .then(holdModifier),
            contentAlignment = Alignment.Center
        ) {
            if (supportsAnimation) {
                CatAnimation(
                    resId = when (phase) {
                        CatPhase.INTRO -> clips.intro
                        CatPhase.LOOP -> clips.loop
                        CatPhase.OUTRO -> clips.outro
                    },
                    playKey = loopRound,
                    onCompletion = {
                        when (phase) {
                            CatPhase.INTRO -> {
                                phase = if (shouldStartOutro()) CatPhase.OUTRO else CatPhase.LOOP
                            }
                            CatPhase.LOOP -> {
                                if (shouldStartOutro()) phase = CatPhase.OUTRO else loopRound++
                            }
                            CatPhase.OUTRO -> {
                                outroFinished = true
                                if (releaseAfterOutro) latestOnRelease()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = state.cat.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun CatAnimation(
    @RawRes resId: Int,
    playKey: Int,
    onCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestOnCompletion by rememberUpdatedState(onCompletion)
    var drawable by remember { mutableStateOf<AnimatedImageDrawable?>(null) }

    LaunchedEffect(resId) {
        val decoded = withContext(Dispatchers.IO) {
            ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.resources, resId))
        } as? AnimatedImageDrawable ?: return@LaunchedEffect

        decoded.repeatCount = 0
        decoded.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(d: Drawable?) {
                latestOnCompletion()
            }
        })
        decoded.start()
        drawable = decoded
    }

    LaunchedEffect(playKey) {
        if (playKey > 0) drawable?.start()
    }

    DisposableEffect(drawable) {
        val current = drawable
        onDispose {
            current?.clearAnimationCallbacks()
            current?.stop()
        }
    }

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        },
        update = { it.setImageDrawable(drawable) },
        modifier = modifier
    )
}

private fun webpDurationMillis(context: Context, @RawRes resId: Int): Long? = try {
    val bytes = context.resources.openRawResource(resId).use { it.readBytes() }
    var pos = 12
    var total = 0L
    while (pos + 8 <= bytes.size) {
        val fourcc = String(bytes, pos, 4, Charsets.US_ASCII)
        val size = le32(bytes, pos + 4)
        if (fourcc == "ANMF" && pos + 8 + 15 <= bytes.size) {
            total += le24(bytes, pos + 8 + 12)
        }
        pos += 8 + size + (size and 1)
    }
    total.takeIf { it > 0 }
} catch (e: Exception) {
    null
}

private fun le24(b: ByteArray, i: Int): Int =
    (b[i].toInt() and 0xFF) or
            ((b[i + 1].toInt() and 0xFF) shl 8) or
            ((b[i + 2].toInt() and 0xFF) shl 16)

private fun le32(b: ByteArray, i: Int): Int =
    le24(b, i) or ((b[i + 3].toInt() and 0xFF) shl 24)
