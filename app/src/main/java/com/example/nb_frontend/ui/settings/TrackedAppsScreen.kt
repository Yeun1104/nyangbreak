package com.example.nb_frontend.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown10
import com.example.nb_frontend.ui.theme.NbBrown20
import com.example.nb_frontend.ui.theme.NbBrown5
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown90
import com.example.nb_frontend.ui.theme.NbOrange10
import com.example.nb_frontend.ui.theme.NbOrange40
import com.example.nb_frontend.ui.theme.NbWhite
import com.example.nb_frontend.ui.theme.Roboto

data class TrackedAppItem(
    val packageName: String,
    val name: String
)

@Composable
fun TrackedAppsScreen(
    apps: List<TrackedAppItem>,
    trackedPackages: Set<String>,
    onToggle: (packageName: String, enabled: Boolean) -> Unit,
    onToggleAll: (enabled: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val allOn = apps.isNotEmpty() && apps.all { it.packageName in trackedPackages }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NbBrown5)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로",
            tint = NbBrown90,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBack)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "추적할 앱",
            fontFamily = Jua,
            fontSize = 32.sp,
            color = NbBrown90
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "추적을 허용할 앱을 선택하여 주세요",
            fontFamily = Roboto,
            fontSize = 13.sp,
            color = NbBrown60
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NbWhite)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "전체 앱",
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NbBrown90,
                modifier = Modifier.weight(1f)
            )
            NbSwitch(checked = allOn, onCheckedChange = onToggleAll)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(20.dp))
                .background(NbWhite),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                AppToggleRow(
                    app = app,
                    checked = app.packageName in trackedPackages,
                    onCheckedChange = { onToggle(app.packageName, it) }
                )
                if (index < apps.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = NbBrown10)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AppToggleRow(
    app: TrackedAppItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NbOrange10),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(1),
                fontFamily = Jua,
                fontSize = 16.sp,
                color = NbBrown60
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = app.name,
            fontFamily = Roboto,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = NbBrown90,
            modifier = Modifier.weight(1f)
        )

        NbSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NbSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = NbWhite,
            checkedTrackColor = NbOrange40,
            checkedBorderColor = NbOrange40,
            uncheckedThumbColor = NbWhite,
            uncheckedTrackColor = NbBrown10,
            uncheckedBorderColor = NbBrown20
        )
    )
}