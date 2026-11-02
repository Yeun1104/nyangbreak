package com.example.nb_frontend.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.R
import com.example.nb_frontend.ui.components.TabBar
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown10
import com.example.nb_frontend.ui.theme.NbBrown20
import com.example.nb_frontend.ui.theme.NbBrown5
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown80
import com.example.nb_frontend.ui.theme.NbBrown90
import com.example.nb_frontend.ui.theme.NbOrange10
import com.example.nb_frontend.ui.theme.NbOrange20
import com.example.nb_frontend.ui.theme.NbOrange40
import com.example.nb_frontend.ui.theme.NbOrange50
import com.example.nb_frontend.ui.theme.NbOrange70
import com.example.nb_frontend.ui.theme.NbWhite
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    continuousMinutes: Int = 20,
    limitMinutes: Int = 60,
    todayHours: Int = 4,
    todayMinutes: Int = 20,
    catCount: Int = 4
) {
    var isPaused by remember { mutableStateOf(false) }
    val remainMinutes = (limitMinutes - continuousMinutes).coerceAtLeast(0)

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
            text = "냥브레이크",
            fontFamily = Jua,
            fontSize = 32.sp,
            color = NbBrown90
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "오늘도 고양이가 지켜보고 있어요",
                fontSize = 16.sp,
                fontFamily = Jua,
                color = NbBrown60
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = null,
                tint = NbOrange20,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ic_cat),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-44).dp)
                    .size(200.dp)
            )

            Column {
                Column(modifier = Modifier.width(164.dp)) {
                    RemainCard(remainMinutes = remainMinutes)
                    Spacer(modifier = Modifier.height(12.dp))
                    PauseButton(
                        isPaused = isPaused,
                        onClick = { isPaused = !isPaused }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                UsageCard(
                    continuousMinutes = continuousMinutes,
                    limitMinutes = limitMinutes,
                    todayHours = todayHours,
                    todayMinutes = todayMinutes,
                    catCount = catCount
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RemainCard(remainMinutes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NbOrange10)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_timer),
            contentDescription = null,
            tint = NbBrown80,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "고양이 등장까지",
                fontSize = 14.sp,
                fontFamily = Jua,
                color = NbBrown80
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = numberUnitText(
                    "$remainMinutes" to " 분",
                    numberSize = 24.sp,
                    unitSize = 14.sp,
                    numberColor = NbOrange70
                )
            )
        }
    }
}

@Composable
private fun PauseButton(isPaused: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NbBrown20),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = NbWhite,
            contentColor = NbBrown90
        )
    ) {
        Icon(
            painter = painterResource(
                id = if (isPaused) R.drawable.ic_pause else R.drawable.ic_pause
            ),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isPaused) "다시 시작" else "일시정지",
            fontSize = 14.sp,
            fontFamily = Jua
        )
    }
}

@Composable
private fun UsageCard(
    continuousMinutes: Int,
    limitMinutes: Int,
    todayHours: Int,
    todayMinutes: Int,
    catCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NbWhite)
            .padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UsageRing(current = continuousMinutes, limit = limitMinutes)
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(thickness = 1.dp, color = NbBrown10)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            StatItem(
                label = "오늘 화면 사용 시간",
                value = numberUnitText("$todayHours" to "시간 ", "$todayMinutes" to "분"),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(thickness = 1.dp, color = NbBrown10)
            StatItem(
                label = "고양이 등장",
                value = numberUnitText("$catCount" to "회"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UsageRing(current: Int, limit: Int) {
    val ringSize = 240.dp
    val strokeWidth = 32.dp

    val progress = (current.toFloat() / limit).coerceIn(0f, 1f)
    val sweep = 360f * progress

    val endAngle = Math.toRadians((-90f + sweep).toDouble())
    val radius = (ringSize - strokeWidth) / 2
    val badgeX = radius * cos(endAngle).toFloat()
    val badgeY = radius * sin(endAngle).toFloat()

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            drawArc(
                color = NbOrange10,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = NbOrange40,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "연속사용",
                fontSize = 12.sp,
                fontFamily = Jua,
                color = NbBrown60
            )
            Row {
                Text(
                    text = "$current",
                    fontFamily = Jua,
                    fontSize = 56.sp,
                    color = NbBrown90,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = "분",
                    fontFamily = Jua,
                    fontSize = 24.sp,
                    color = NbBrown90,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Text(
                text = "/ ${limit}분",
                fontFamily = Jua,
                fontSize = 20.sp,
                color = NbBrown60
            )
        }

        Box(
            modifier = Modifier
                .offset(x = badgeX, y = badgeY)
                .size(48.dp)
                .clip(CircleShape)
                .background(NbWhite)
                .padding(3.dp)
                .clip(CircleShape)
                .background(NbOrange40),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = null,
                tint = NbWhite,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = Jua,
            color = NbBrown60
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value)
    }
}

private fun numberUnitText(
    vararg parts: Pair<String, String>,
    numberSize: TextUnit = 22.sp,
    unitSize: TextUnit = 15.sp,
    numberColor: Color = NbOrange50
): AnnotatedString = buildAnnotatedString {
    parts.forEach { (number, unit) ->
        withStyle(SpanStyle(fontFamily = Jua, fontSize = numberSize, color = numberColor)) {
            append(number)
        }
        withStyle(SpanStyle(fontFamily = Jua, fontSize = unitSize, color = NbBrown90)) {
            append(unit)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() {
    Scaffold(
        bottomBar = { TabBar(currentRoute = "home", onTabClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HomeScreen()
        }
    }
}