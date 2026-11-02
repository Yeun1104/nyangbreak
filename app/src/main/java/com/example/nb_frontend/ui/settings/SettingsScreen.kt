package com.example.nb_frontend.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.R
import com.example.nb_frontend.ui.components.TabBar
import androidx.compose.ui.unit.Dp
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown10
import com.example.nb_frontend.ui.theme.NbBrown20
import com.example.nb_frontend.ui.theme.NbBrown5
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown90
import com.example.nb_frontend.ui.theme.NbOrange10
import com.example.nb_frontend.ui.theme.NbOrange20
import com.example.nb_frontend.ui.theme.NbOrange40
import com.example.nb_frontend.ui.theme.NbWhite
import com.example.nb_frontend.ui.theme.Roboto

private val usageOptions = listOf(15, 30, 45, 60, 75, 90)
private val restOptions = listOf(1, 3, 5)
private val catImages = listOf(
    R.drawable.cat_tabby,
    R.drawable.cat_gray_white,
    R.drawable.cat_tuxedo
)

@Composable
fun SettingsScreen() {
    var usageLimit by remember { mutableIntStateOf(60) }
    var restTime by remember { mutableIntStateOf(3) }
    var emergencyEnabled by remember { mutableStateOf(true) }
    var preAlertEnabled by remember { mutableStateOf(true) }
    var selectedCat by remember { mutableIntStateOf(0) }

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
            text = "설정",
            fontFamily = Jua,
            fontSize = 32.sp,
            color = NbBrown90
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingCard {
            SectionTitle("연속사용 시간 설정")
            Spacer(modifier = Modifier.height(16.dp))
            OptionChips(
                options = usageOptions,
                selected = usageLimit,
                onSelect = { usageLimit = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard {
            SectionTitle("쉬는 시간")
            Spacer(modifier = Modifier.height(16.dp))
            OptionChips(
                options = restOptions,
                selected = restTime,
                onSelect = { restTime = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard(verticalPadding = 4.dp) {
            CheckRow(
                label = "긴급 해체 버튼 사용",
                checked = emergencyEnabled,
                onToggle = { emergencyEnabled = !emergencyEnabled }
            )
            HorizontalDivider(thickness = 1.dp, color = NbBrown10)
            CheckRow(
                label = "고양이 등장 5분 전 알림",
                checked = preAlertEnabled,
                onToggle = { preAlertEnabled = !preAlertEnabled }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard {
            SectionTitle("내 고양이 고르기")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                catImages.forEachIndexed { index, imageRes ->
                    CatOption(
                        imageRes = imageRes,
                        selected = selectedCat == index,
                        onClick = { selectedCat = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingCard(
    verticalPadding: Dp = 16.dp,
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
private fun OptionChips(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEach { minutes ->
                    OptionChip(
                        text = "${minutes}분",
                        selected = selected == minutes,
                        onClick = { onSelect(minutes) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) NbBrown60 else NbWhite)
            .border(1.dp, if (selected) NbBrown60 else NbBrown20, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Jua,
            fontSize = 14.sp,
            color = if (selected) NbWhite else NbBrown60
        )
    }
}

@Composable
private fun CheckRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Roboto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = NbBrown90,
            modifier = Modifier.weight(1f)
        )
        NbCheckBox(checked = checked)
    }
}

@Composable
private fun NbCheckBox(checked: Boolean) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(shape)
            .background(if (checked) NbOrange40 else NbWhite)
            .border(1.5.dp, if (checked) NbOrange40 else NbBrown20, shape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = NbWhite,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CatOption(
    @DrawableRes imageRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(if (selected) NbOrange10 else NbWhite)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NbOrange20 else NbBrown20,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsPreview() {
    Scaffold(
        bottomBar = { TabBar(currentRoute = "settings", onTabClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            SettingsScreen()
        }
    }
}