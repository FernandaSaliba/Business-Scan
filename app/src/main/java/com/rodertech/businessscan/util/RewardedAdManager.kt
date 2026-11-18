package com.rodertech.businessscan.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var isloading = false

    // ID oficial de teste do Google para o Teste Fechado (evita bloqueio e No Fill antes da publicação)
    // Quando for aprovado para PRODUÇÃO, substitua por: "ca-app-pub-1038880007093527/5187270032"
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    private fun findActivity(ctx: Context): Activity? {
        var current = ctx
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    fun loadAd() {
        if (rewardedAd == null && !isloading) {
            isloading = true

            // Garante que o seu celular não gere impressões inválidas durante os testes
            val configuration = RequestConfiguration.Builder().build()
            MobileAds.setRequestConfiguration(configuration)

            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context.applicationContext, adUnitId, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isloading = false
                    Log.d("RewardedAd", "Anúncio carregado com sucesso.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isloading = false
                    Log.d("RewardedAd", "Falha ao carregar: ${adError.message}")
                }
            })
        }
    }

    fun showAd(onRewardEarned: () -> Unit) {
        val activity = findActivity(context)
        if (activity != null && rewardedAd != null) {
            rewardedAd?.show(activity) { _ ->
                // O usuário assistiu ao vídeo até o fim e ganhou a recompensa!
                onRewardEarned()
                // Recarrega o próximo anúncio para uso futuro
                rewardedAd = null
                loadAd()
            }
        } else {
            Log.d("RewardedAd", "O anúncio ainda não está pronto.")
            Toast.makeText(context, "Carregando vídeo, tente novamente em instantes...", Toast.LENGTH_SHORT).show()
            // Opcional: chamar loadAd() aqui caso não esteja carregado
            loadAd()
        }
    }
}