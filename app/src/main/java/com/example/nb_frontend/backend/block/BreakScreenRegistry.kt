package com.example.nb_frontend.backend.block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.backend.data.settings.CatCharacter

/** 휴식 화면에 전달하는 값 */
data class BreakUiState(
    val cat: CatCharacter,
    val remainingMillis: Long,
    /** "눌러서 보내기"가 켜져 있으면 true */
    val canRelease: Boolean,
)

/** 휴식 화면 등록. 프론트가 [register]로 화면을 등록하고, 등록 전에는 임시 화면을 쓴다 */
object BreakScreenRegistry {
    var content: @Composable (state: BreakUiState, release: () -> Unit) -> Unit = { state, release ->
        TemporaryBreakScreen(state, release)
    }
        private set

    fun register(screen: @Composable (state: BreakUiState, release: () -> Unit) -> Unit) {
        content = screen
    }
}

// TODO: 임시 화면. 프론트 휴식 화면이 등록되면 삭제 예정
@Composable
private fun TemporaryBreakScreen(state: BreakUiState, release: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val sec = (state.remainingMillis + 999) / 1000
            Text("%02d:%02d".format(sec / 60, sec % 60), color = Color.White, fontSize = 48.sp)
            if (state.canRelease) TextButton(onClick = release) { Text("해제", color = Color.White) }
        }
    }
}
