package com.example.business_scan.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.business_scan.screens.PlanoType

class BillingManager(
    private val context: Context,
    private val onSubscriptionStatusChanged: (isPremium: Boolean) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        const val SUBSCRIPTION_MONTHLY_ID = "business_scan_premium_monthly" // R$ 29,90
        const val SUBSCRIPTION_YEARLY_ID = "business_scan_premium_yearly"   // R$ 19,90/mês
    }

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySubscriptionProducts()
                    checkActiveSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                startConnection()
            }
        })
    }

    private fun querySubscriptionProducts() {
        val productMonthly = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(SUBSCRIPTION_MONTHLY_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val productYearly = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(SUBSCRIPTION_YEARLY_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(productMonthly, productYearly))
            .build()

        // Ajuste no callback: extrai o 'productDetailsList' a partir de 'queryProductDetailsResult'
        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryProductDetailsResult.productDetailsList.forEach { details ->
                    productDetailsMap[details.productId] = details
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, planoType: PlanoType) {
        val targetId = if (planoType == PlanoType.MENSAL) SUBSCRIPTION_MONTHLY_ID else SUBSCRIPTION_YEARLY_ID
        val details = productDetailsMap[targetId]

        if (details == null) {
            Toast.makeText(context, "Produto não encontrado na Play Store. Verifique a configuração.", Toast.LENGTH_SHORT).show()
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(context, "Compra cancelada pelo usuário.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, "Erro no pagamento: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onSubscriptionStatusChanged(true)
                        Toast.makeText(context, "Assinatura ativada com sucesso! 🎉", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                onSubscriptionStatusChanged(true)
            }
        }
    }

    fun checkActiveSubscriptions() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSubscription = purchasesList.any { purchase ->
                    (purchase.products.contains(SUBSCRIPTION_MONTHLY_ID) || purchase.products.contains(SUBSCRIPTION_YEARLY_ID)) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                onSubscriptionStatusChanged(hasActiveSubscription)
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}