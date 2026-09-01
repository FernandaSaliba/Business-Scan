package com.rodertech.businessscan.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.rodertech.businessscan.model.Business
import java.io.File
import java.io.FileOutputStream


object PdfGenerator {

    /**
     * FUNÇÃO 1: Apenas o Relatório de CNPJ (Inalterada, sem mistura com assinatura)
     */
    fun generateBusinessReportPdf(context: Context, business: Business?): File? {
        val pdfDocument = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 18f
            isFakeBoldText = true
        }

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 13f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 11f
        }

        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var y = 50f

        fun checkAndCreateNewPage(requiredSpace: Float = 20f) {
            if (y + requiredSpace > 790f) {
                pdfDocument.finishPage(currentPage)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 50f
            }
        }

        // Cabeçalho
        canvas.drawText("Business Scan - Relatório Cadastral Avançado", 40f, y, titlePaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        if (business != null) {
            checkAndCreateNewPage(45f)
            canvas.drawText("Razão Social / Nome:", 40f, y, sectionPaint)
            y += 18f
            canvas.drawText(business.name, 40f, y, boldBodyPaint)
            y += 25f

            checkAndCreateNewPage(20f)
            canvas.drawText("Situação Cadastral:", 40f, y, sectionPaint)
            val statusText = business.situacaoCadastral.ifEmpty { "INATIVA" }
            canvas.drawText(statusText, 170f, y, bodyPaint)
            y += 20f

            if (business.cnae.isNotEmpty()) {
                checkAndCreateNewPage(40f)
                canvas.drawText("Atividade (CNAE):", 40f, y, sectionPaint)
                y += 18f
                canvas.drawText(business.cnae, 40f, y, bodyPaint)
                y += 22f
            }

            checkAndCreateNewPage(20f)
            canvas.drawText("Capital Social:", 40f, y, sectionPaint)
            canvas.drawText(business.capitalSocialFormatado, 170f, y, bodyPaint)
            y += 20f

            checkAndCreateNewPage(20f)
            canvas.drawText("Porte Estimado:", 40f, y, sectionPaint)
            canvas.drawText(business.estimativaFaturamento, 170f, y, bodyPaint)
            y += 20f

            if (business.endereco.isNotEmpty()) {
                checkAndCreateNewPage(45f)
                canvas.drawText("Endereço:", 40f, y, sectionPaint)
                y += 18f
                val fullAddress = "${business.endereco}, ${business.municipio} - ${business.uf}"
                canvas.drawText(fullAddress, 40f, y, bodyPaint)
                y += 25f
            }

            checkAndCreateNewPage(25f)
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            checkAndCreateNewPage(20f)
            canvas.drawText("👥 Quadro de Sócios e Administradores (QSA):", 40f, y, sectionPaint)
            y += 20f

            if (business.qsa.isEmpty()) {
                checkAndCreateNewPage(20f)
                canvas.drawText("• Nenhum sócio/administrador informado na Receita.", 45f, y, bodyPaint)
                y += 20f
            } else {
                business.qsa.forEach { socio ->
                    checkAndCreateNewPage(18f)
                    val cargo = socio.cargo.ifEmpty { "Sócio" }
                    canvas.drawText("• ${socio.nome} ($cargo)", 45f, y, bodyPaint)
                    y += 18f
                }
            }
        } else {
            canvas.drawText("Informações do negócio indisponíveis.", 40f, y, bodyPaint)
            y += 25f
        }

        pdfDocument.finishPage(currentPage)
        val cleanName = business?.name?.replace("[^a-zA-Z0-9_]".toRegex(), "_") ?: "Business"
        return saveAndReturnPdf(context, pdfDocument, "Relatorio_${cleanName}.pdf")
    }

    /**
     * FUNÇÃO 2: Apenas para Documentos Escaneados com Assinatura do RG (Totalmente Separada)
     */
    fun generateSignedDocumentPdf(context: Context, documentContent: String, customTitle: String): File? {
        val pdfDocument = PdfDocument()

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 11f
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val currentPage = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = currentPage.canvas
        var y = 50f

        // Cabeçalho do documento escaneado
        canvas.drawText(customTitle, 40f, y, titlePaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // Conteúdo do texto escaneado (OCR)
        canvas.drawText(documentContent, 40f, y, bodyPaint)

        // Aplica a assinatura do RG capturada anteriormente
        val signatureBitmap = SignatureManager.getSavedSignature(context)
        if (signatureBitmap != null) {
            y = 700f // Posição fixa inferior para a assinatura no documento escaneado
            canvas.drawLine(40f, y, 250f, y, linePaint)
            y += 10f
            canvas.drawText("Assinado digitalmente via RG", 40f, y, bodyPaint)
            y += 15f

            val scaledSignature = Bitmap.createScaledBitmap(signatureBitmap, 180, 60, true)
            canvas.drawBitmap(scaledSignature, 40f, y, null)
        }

        pdfDocument.finishPage(currentPage)
        return saveAndReturnPdf(context, pdfDocument, "DocumentoAssinado.pdf")
    }

    /**
     * Função auxiliar interna para salvar o arquivo (evita duplicação de código de salvamento)
     */
    private fun saveAndReturnPdf(context: Context, pdfDocument: PdfDocument, fileName: String): File? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Erro ao criar URI no MediaStore.")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                openPdfIntent(context, uri)
                return null
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                pdfDocument.close()
                Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                openPdfIntent(context, uri)
                return file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun openPdfIntent(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}