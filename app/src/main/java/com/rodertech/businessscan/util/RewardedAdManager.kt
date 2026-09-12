package com.rodertech.businessscan.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var isloading = false

    // ID oficial de teste do Google para Anúncio Premiado
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    fun loadAd() {
        if (rewardedAd == null && !isloading) {
            isloading = true
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
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
        val activity = context as? Activity
        if (activity != null && rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                // O usuário assistiu ao vídeo até o fim e ganhou a recompensa!
                onRewardEarned()
                // Recarrega o próximo anúncio para uso futuro
                rewardedAd = null
                loadAd()
            }
        } else {
            Log.d("RewardedAd", "O anúncio ainda não está pronto.")
            // Opcional: chamar loadAd() aqui caso não esteja carregado
            loadAd()
        }
    }
}

