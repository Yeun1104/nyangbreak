package com.example.nb_frontend.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.ui.components.TabBar
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown10
import com.example.nb_frontend.ui.theme.NbBrown5
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown90
import com.example.nb_frontend.ui.theme.NbOrange10
import com.example.nb_frontend.ui.theme.NbOrange40
import com.example.nb_frontend.ui.theme.NbWhite
import com.example.nb_frontend.ui.theme.Roboto

data class DayUsage(
    val day: String,
    val hours: Float,
    val isToday: Boolean = false
)

data class AppUsage(
    val name: String,
    val minutes: Int
)

private val dummyWeekly = listOf(
    DayUsage("금", 4.0f),
    DayUsage("토", 6.0f),
    DayUsage("일", 4.5f),
    DayUsage("월", 2.0f),
    DayUsage("화", 6.5f),
    DayUsage("수", 4.0f),
    DayUsage("목", 5.0f, isToday = true)
)

private val dummyApps = listOf(
    AppUsage("인스타그램", 125),
    AppUsage("유튜브", 90),
    AppUsage("카카오톡", 42)
)

@Composable
fun HistoryScreen(
    todayMinutes: Int = 277,
    blockCount: Int = 4,
    weekly: List<DayUsage> = dummyWeekly,
    apps: List<AppUsage> = dummyApps
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NbBrown5)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "기록",
            fontFamily = Jua,
            fontSize = 32.sp,
            color = NbBrown90
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "오늘 사용한 시간",
                value = formatMinutes(todayMinutes),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "오늘 차단 횟수",
                value = "${blockCount}회",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HistoryCard {
            SectionTitle("요일별 사용 시간")
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = NbBrown10)
            Spacer(modifier = Modifier.height(48.dp))
            WeeklyBarChart(data = weekly)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        HistoryCard {
            SectionTitle("앱별 사용 시간 (오늘)")
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = NbBrown10)
            Spacer(modifier = Modifier.height(8.dp))
            apps.forEach { app ->
                AppUsageRow(app = app)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NbWhite)
            .height(80.dp)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            fontFamily = Roboto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = NbBrown60
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = Jua,
            fontSize = 22.sp,
            color = NbBrown90
        )
    }
}

@Composable
private fun HistoryCard(
    verticalPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NbWhite)
            .padding(horizontal = 16.dp, vertical = verticalPadding),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = NbBrown90
    )
}

@Composable
private fun WeeklyBarChart(data: List<DayUsage>) {
    val maxHours = data.maxOfOrNull { it.hours }?.coerceAtLeast(1f) ?: 1f
    val maxBarHeight = 120.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${formatHours(day.hours)}H",
                    fontFamily = Jua,
                    fontSize = 10.sp,
                    color = NbBrown60
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(maxBarHeight * (day.hours / maxHours))
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (day.isToday) NbOrange40 else NbOrange10)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = day.day,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = NbBrown60
                )
            }
        }
    }
}

@Composable
private fun AppUsageRow(app: AppUsage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NbOrange10)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = app.name,
            fontFamily = Roboto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = NbBrown90,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatMinutes(app.minutes),
            fontFamily = Roboto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = NbBrown90
        )
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}

private fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) hours.toInt().toString() else hours.toString()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryPreview() {
    Scaffold(
        bottomBar = { TabBar(currentRoute = "history", onTabClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HistoryScreen()
        }
    }
}