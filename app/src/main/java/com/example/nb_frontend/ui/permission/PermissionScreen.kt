package com.example.nb_frontend.ui.permission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.R
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown20
import com.example.nb_frontend.ui.theme.NbBrown5
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown80
import com.example.nb_frontend.ui.theme.NbBrown90
import com.example.nb_frontend.ui.theme.NbOrange10
import com.example.nb_frontend.ui.theme.NbOrange50
import com.example.nb_frontend.ui.theme.NbWhite
import com.example.nb_frontend.ui.theme.Roboto

@Composable
fun PermissionScreen(onStart: () -> Unit) {
    var usageGranted by remember { mutableStateOf(false) }
    var overlayGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }

    val allGranted = usageGranted && overlayGranted && notificationGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NbBrown5)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_cat),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "고양이가 지켜보려면\n3개의 권한이 필요해요",
            fontFamily = Jua,
            fontSize = 32.sp,
            lineHeight = 34.sp,
            color = NbBrown90
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "사용 기록은 이 폰 안에서만 쓰여요.\n언제든 설정에서 바꿀 수 있어요.",
            fontSize = 14.sp,
            fontFamily = Jua,
            lineHeight = 19.sp,
            color = NbBrown60
        )

        Spacer(modifier = Modifier.height(40.dp))

        PermissionCard(
            title = "사용 정보 접근",
            description = "어떤 앱을 얼마나 썼는지 측정해요",
            granted = usageGranted,
            onClick = { usageGranted = !usageGranted }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "다른 앱 위에 표시",
            description = "제한 시간이 되면 고양이가 화면을 덮어요",
            granted = overlayGranted,
            onClick = { overlayGranted = !overlayGranted }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "알림",
            description = "고양이 등장 5분 전에 미리 알려줘요",
            granted = notificationGranted,
            onClick = { notificationGranted = !notificationGranted }
        )

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = allGranted,
            enter = fadeIn() + slideInVertically { it }
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NbOrange50,
                    contentColor = NbWhite
                )
            ) {
                Text(text = "시작하기", fontFamily = Jua, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NbWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(NbOrange10)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                color = NbBrown90
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                fontFamily = Roboto,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = NbBrown60
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val buttonModifier = Modifier
            .width(76.dp)
            .height(32.dp)

        if (granted) {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NbBrown60,
                    contentColor = NbWhite
                )
            ) {
                Text(
                    text = "허용됨",
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        } else {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, NbBrown20),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NbBrown80)
            ) {
                Text(
                    text = "허용하기",
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PermissionPreview() {
    PermissionScreen(onStart = {})
}