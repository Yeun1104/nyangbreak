package com.example.nb_frontend.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import com.example.nb_frontend.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nb_frontend.ui.theme.NbBrown5
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nb_frontend.ui.theme.Jua
import com.example.nb_frontend.ui.theme.NbBrown60
import com.example.nb_frontend.ui.theme.NbBrown90
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@Composable
fun SplashScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NbBrown5),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(260.dp))

        Text(
            text = "냥브레이크",
            fontFamily = Jua,
            fontSize = 48.sp,
            color = NbBrown90
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "쉬어야 할 때, 고양이가 나타나요",
            fontFamily = Jua,
            fontSize = 16.sp,
            color = NbBrown60)

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.ic_cat),
            contentDescription = null,
            modifier = Modifier
                .width(360.dp)
                .height(270.dp)

        )


    }
}

@Preview
@Composable
fun SplashPreview(){
    SplashScreen()
}
