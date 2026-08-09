package com.example.business_scan.screens



import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp



@Suppress("unused")

@Composable

fun HomeScreen() {

    Scaffold { paddingValues ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .padding(paddingValues),

            contentAlignment = Alignment.Center

        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(text = "Bem-vindo ao BusinessScan!", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { /* Lógica de scan aqui */ }) {

                    Text(text = "Escanear Novo Negócio")

                }

            }

        }

    }

}

