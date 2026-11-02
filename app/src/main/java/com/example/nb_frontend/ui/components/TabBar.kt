package com.example.nb_frontend.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.R
import com.example.nb_frontend.ui.theme.NbBrown10
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbOrange50
import com.example.nb_frontend.ui.theme.NbWhite
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding

enum class NbTab(
    val route: String,
    val label: String,
    val iconRes: Int
) {
    HOME("home", "메인", R.drawable.ic_home),
    HISTORY("history", "기록", R.drawable.ic_history),
    SETTINGS("settings", "설정", R.drawable.ic_settings)
}

@Composable
fun TabBar(
    currentRoute: String?,
    onTabClick: (NbTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NbWhite)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 1.dp, color = NbBrown10)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            NbTab.entries.forEach { tab ->
                TabBarItem(
                    tab = tab,
                    selected = currentRoute == tab.route,
                    onClick = { onTabClick(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabBarItem(
    tab: NbTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) NbOrange50 else NbBrown60

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = tab.iconRes),
            contentDescription = tab.label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.label,
            fontSize = 10.sp,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TabBarPreview() {
    TabBar(currentRoute = "home", onTabClick = {})
}