package com.example.nb_frontend.backend.block

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.nb_frontend.backend.data.settings.CatCharacter

/** 다른 앱 위에 휴식 화면 창을 띄우고 내린다. 화면 내용은 [BreakScreenRegistry]에 등록된 것을 쓴다 */
class BlockOverlay(context: Context) {
    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var view: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var shownFor: String? = null
    private var remaining by mutableLongStateOf(0L)

    val showingPackage: String? get() = shownFor

    /** @return 오버레이 권한이 없으면 false */
    fun show(packageName: String, cat: CatCharacter, canRelease: Boolean, release: () -> Unit): Boolean {
        if (shownFor == packageName) return true
        hide()

        val lifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        val composeView = ComposeView(appContext).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                BreakScreenRegistry.content(BreakUiState(cat, remaining, canRelease), release)
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.CENTER }

        return try {
            windowManager.addView(composeView, params)
            view = composeView
            owner = lifecycleOwner
            shownFor = packageName
            true
        } catch (e: RuntimeException) {
            lifecycleOwner.stop()
            false
        }
    }

    fun updateCountdown(remainingMillis: Long) {
        remaining = remainingMillis
    }

    fun hide() {
        val v = view ?: return
        try {
            windowManager.removeView(v)
        } catch (_: IllegalArgumentException) {
        }
        owner?.stop()
        view = null
        owner = null
        shownFor = null
    }

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val registry = LifecycleRegistry(this)
        private val savedState = SavedStateRegistryController.create(this)
        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

        fun start() {
            savedState.performRestore(null)
            registry.currentState = Lifecycle.State.RESUMED
        }

        fun stop() {
            registry.currentState = Lifecycle.State.DESTROYED
        }
    }
}
