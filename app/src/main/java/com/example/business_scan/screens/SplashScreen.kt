package com.example.business_scan.screens


import androidx.compose.animation.core.Animatable

import androidx.compose.animation.core.tween

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Business

import androidx.compose.material.icons.filled.Shield

import androidx.compose.material3.Icon

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.alpha

import androidx.compose.ui.draw.scale

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import kotlinx.coroutines.delay



@Composable

fun SplashScreen(

    onTimeout: () -> Unit

) {

// Animação de entrada (Escala e Transparência)

    val scale = remember { Animatable(0.6f) }

    val alpha = remember { Animatable(0f) }



    LaunchedEffect(key1 = true) {

// Anima a logo crescendo e aparecendo

        scale.animateTo(

            targetValue = 1f,

            animationSpec = tween(durationMillis = 800)

        )

        alpha.animateTo(

            targetValue = 1f,

            animationSpec = tween(durationMillis = 800)

        )



// Espera 2 segundos na tela de Splash antes de navegar

        delay(2000)

        onTimeout()

    }



    Box(

        modifier = Modifier

            .fillMaxSize()

            .background(

                Brush.verticalGradient(

                    colors = listOf(

                        Color(0xFF0F172A),

                        Color(0xFF1E1B4B)

                    )

                )

            ),

        contentAlignment = Alignment.Center

    ) {

        Column(

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center,

            modifier = Modifier

                .scale(scale.value)

                .alpha(alpha.value)

        ) {

// --- LOGO CUSTOMIZADA ---

            Box(

                contentAlignment = Alignment.Center,

                modifier = Modifier.size(120.dp)

            ) {

// Glow/Sombra de fundo para a logo

                Surface(

                    modifier = Modifier.size(110.dp),

                    shape = RoundedCornerShape(28.dp),

                    color = Color(0xFF6366F1).copy(alpha = 0.2f)

                ) {}



// Card principal da Logo com Gradiente

                Surface(

                    modifier = Modifier.size(96.dp),

                    shape = RoundedCornerShape(24.dp),

                    color = Color.Transparent

                ) {

                    Box(

                        modifier = Modifier

                            .fillMaxSize()

                            .background(

                                Brush.linearGradient(

                                    colors = listOf(

                                        Color(0xFF818CF8),

                                        Color(0xFF4F46E5)

                                    )

                                )

                            ),

                        contentAlignment = Alignment.Center

                    ) {

// Ícone do Prédio/Empresa

                        Icon(

                            imageVector = Icons.Default.Business,

                            contentDescription = "Business Icon",

                            tint = Color.White,

                            modifier = Modifier.size(48.dp)

                        )

                    }

                }



// Mini Badge/Ícone de Varredura/Segurança no canto da Logo

                Surface(

                    modifier = Modifier

                        .size(32.dp)

                        .align(Alignment.BottomEnd),

                    shape = CircleShape,

                    color = Color(0xFF1E1B4B),

                    shadowElevation = 4.dp

                ) {

                    Box(

                        modifier = Modifier

                            .fillMaxSize()

                            .background(Color(0xFFF59E0B)),

                        contentAlignment = Alignment.Center

                    ) {

                        Icon(

                            imageVector = Icons.Default.Shield,

                            contentDescription = "Scan Badge",

                            tint = Color.White,

                            modifier = Modifier.size(18.dp)

                        )

                    }

                }

            }



            Spacer(modifier = Modifier.height(24.dp))



// --- NOME DO APP ---

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(

                    text = "Business",

                    fontSize = 32.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color.White

                )

                Text(

                    text = "Scan",

                    fontSize = 32.sp,

                    fontWeight = FontWeight.ExtraBold,

                    color = Color(0xFF818CF8)

                )

            }



            Spacer(modifier = Modifier.height(8.dp))



// --- SUBTÍTULO ---

            Text(

                text = "Consulta & Inteligência Cadastral",

                fontSize = 14.sp,

                fontWeight = FontWeight.Medium,

                color = Color(0xFF94A3B8)

            )

        }



// Rodapé da Splash

        Text(

            text = "v1.0.0 • Inteligência Fiscal",

            color = Color(0xFF64748B),

            fontSize = 12.sp,

            modifier = Modifier

                .align(Alignment.BottomCenter)

                .padding(bottom = 32.dp)

        )

    }

}

