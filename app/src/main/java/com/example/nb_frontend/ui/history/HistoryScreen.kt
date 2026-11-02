package com.example.nb_frontend.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.nb_frontend.ui.theme.NbBrown5

@Composable
fun HistoryScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NbBrown5)
    ) {
        Text("기록")
    }
}