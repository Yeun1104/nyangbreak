package com.example.nb_frontend.backend.debugtools

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nb_frontend.backend.service.MonitorController
import com.example.nb_frontend.backend.service.MonitorStatus
import com.example.nb_frontend.backend.viewmodel.HistoryViewModel
import com.example.nb_frontend.backend.viewmodel.HomeViewModel
import com.example.nb_frontend.backend.viewmodel.PermissionViewModel
import com.example.nb_frontend.backend.viewmodel.SettingsViewModel
import com.example.nb_frontend.backend.viewmodel.TrackedAppsViewModel

// TODO: PoC 검증용 임시 화면 (debug 빌드 전용). 화면 연결 후 삭제 예정
/** PoC 테스트 화면 */
class BackendTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { TestScreen() } } }
    }
}

private const val YOUTUBE = "com.google.android.youtube"

@Composable
private fun TestScreen(
    permissionVm: PermissionViewModel = viewModel(),
    settingsVm: SettingsViewModel = viewModel(),
    appsVm: TrackedAppsViewModel = viewModel(),
    homeVm: HomeViewModel = viewModel(),
    historyVm: HistoryViewModel = viewModel(),
) {
    val context = LocalContext.current
    val perm by permissionVm.uiState.collectAsStateWithLifecycle()
    val s by settingsVm.settings.collectAsStateWithLifecycle()
    val apps by appsVm.uiState.collectAsStateWithLifecycle()
    val home by homeVm.uiState.collectAsStateWithLifecycle()
    val history by historyVm.uiState.collectAsStateWithLifecycle()
    val live by MonitorStatus.live.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissionVm.refresh() }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionVm.refresh() }

    Column(
        Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("냥브레이크 PoC 테스트", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Section("1. 권한")
        PermRow("사용 정보 접근", perm.usageGranted) { context.startActivity(permissionVm.usageAccessIntent()) }
        PermRow("다른 앱 위에 표시", perm.overlayGranted) { context.startActivity(permissionVm.overlayIntent()) }
        PermRow("알림", perm.notificationGranted) { permissionVm.notificationPermission?.let { notifLauncher.launch(it) } }

        Section("2. 빠른 테스트 설정")
        Button(onClick = {
            settingsVm.setDefaultLimitMinutes(1)
            settingsVm.setBreakSeconds(30)
            appsVm.setTracked(YOUTUBE, true)
            appsVm.setLimitMinutes(YOUTUBE, 1)
            toast(context, "유튜브 1분 제한, 휴식 30초")
        }) { Text("유튜브 1분 · 휴식 30초로 설정") }
        Text("휴식 ${s.breakSeconds}초 · 새 앱 기본 제한 ${s.defaultLimitMinutes}분")
        SwitchRow("눌러서 보내기", s.tapToDismissEnabled) { settingsVm.setTapToDismissEnabled(it) }
        Text("감시 중인 앱: " + apps.trackedPackages.joinToString { "${it.substringAfterLast('.')}(${apps.limitMinutes[it]}분)" }
            .ifEmpty { "없음" })
        if (!apps.loading) Text("설치된 앱 ${apps.apps.size}개 불러옴", color = MaterialTheme.colorScheme.outline)

        Section("3. 감시 서비스")
        Text("서비스: ${if (live.serviceRunning) "실행 중" else "꺼짐"} · 일시정지: ${if (home.isPaused) "예" else "아니오"}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (!MonitorController.ensureRunning(context)) toast(context, "권한 1, 2를 먼저 허용해 주세요") }) { Text("감시 시작") }
            OutlinedButton(onClick = { MonitorController.stop(context) }) { Text("감시 중지") }
            OutlinedButton(onClick = {
                val wanted = !home.isPaused
                if (wanted && !homeVm.togglePause()) toast(context, "휴식 중에는 일시정지할 수 없어요")
                else if (!wanted) homeVm.togglePause()
            }) { Text(if (home.isPaused) "재개" else "일시정지") }
        }

        Section("PoC ② 선택한 앱 식별")
        Text("지금 화면의 앱: ${live.foregroundPackage ?: "(화면 꺼짐 또는 권한 없음)"}")

        Section("PoC ① 사용 시간 조회 (홈 화면 값)")
        home.apps.forEach {
            Text("• ${it.name}: ${it.usedMinutes}/${it.limitMinutes}분 · 고양이까지 ${it.remainingMinutes}분" +
                if (it.onBreak) " · 휴식 중 ${it.breakRemainingSeconds}초" else "")
        }
        live.accumulated.forEach { (pkg, ms) -> Text("  ${pkg.substringAfterLast('.')} 누적 ${ms / 1000}초", color = MaterialTheme.colorScheme.outline) }
        Text("오늘 ${home.todayHours}시간 ${home.todayMinutes}분 · 고양이 ${home.catCount}회")

        Section("기본 통계 (기록 화면 값)")
        Text(history.weekly.joinToString("  ") { "${it.day}${if (it.isToday) "*" else ""} %.2fh".format(it.hours) })
        history.apps.forEach { Text("• ${it.name}: ${it.minutes}분") }

        Text("\nPoC ③ 오버레이: 설정 → 감시 시작 → 유튜브를 1분 쓰면 고양이가 나와 30초 뒤 사라져야 정상.\n결과 로그: Android Studio Logcat에서 'MonitorService' 검색")
    }
}

@Composable private fun Section(title: String) {
    HorizontalDivider(Modifier.padding(top = 8.dp)); Text(title, fontWeight = FontWeight.Bold)
}

@Composable private fun PermRow(label: String, granted: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${if (granted) "✅" else "❌"} $label")
        if (!granted) OutlinedButton(onClick = onClick) { Text("허용하기") }
    }
}

@Composable private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label); Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun toast(context: android.content.Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
