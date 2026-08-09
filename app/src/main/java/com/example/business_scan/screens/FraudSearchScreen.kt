package com.example.business_scan.screens



import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import com.example.business_scan.model.Business



@Suppress("unused")

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun FraudSearchScreen(

    business: Business? = null,

    onBack: () -> Unit = {},

    onQueryChanged: (String) -> Unit = {}

) {

    var query by remember { mutableStateOf(business?.cnpj ?: "") }



    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(16.dp)

    ) {

        OutlinedTextField(

            value = query,

            onValueChange = { newValue ->

                query = newValue

                onQueryChanged(newValue)

            },

            label = { Text("CNPJ para Análise") },

            modifier = Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(16.dp))



        Button(onClick = onBack) {

            Text("Voltar")

        }

    }

}

