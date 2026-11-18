package com.example.nb_frontend.backend.block

import androidx.compose.runtime.Composable
import com.example.nb_frontend.backend.data.settings.CatCharacter

/** 휴식 화면에 전달하는 값 */
data class BreakUiState(
    val cat: CatCharacter,
    val remainingMillis: Long,
    /** "눌러서 보내기"가 켜져 있으면 true */
    val canRelease: Boolean,
)

/** 휴식 화면 등록. 프론트가 [register]로 화면을 등록해야 고양이가 뜬다 */
object BreakScreenRegistry {
    var content: (@Composable (state: BreakUiState, release: () -> Unit) -> Unit)? = null
        private set

    fun register(screen: @Composable (state: BreakUiState, release: () -> Unit) -> Unit) {
        content = screen
    }
}
