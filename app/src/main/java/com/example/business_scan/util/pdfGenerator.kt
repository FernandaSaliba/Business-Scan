package com.example.business_scan.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.business_scan.model.Business
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateBusinessReportPdf(context: Context, business: Business?): File? {
        val pdfDocument = PdfDocument()

        // Configuração de Pincéis (Fontes e Cores)
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

        // Controle de Páginas
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var y = 50f

        // Função auxiliar para quebra de página automática
        fun checkAndCreateNewPage(requiredSpace: Float = 20f) {
            if (y + requiredSpace > 790f) { // Limite inferior da página A4
                pdfDocument.finishPage(currentPage)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 50f
            }
        }

        // Cabeçalho do documento
        canvas.drawText("Business Scan - Relatório Cadastral Avançado", 40f, y, titlePaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // Conteúdo da Empresa
        if (business != null) {
            // Razão Social
            checkAndCreateNewPage(45f)
            canvas.drawText("Razão Social / Nome:", 40f, y, sectionPaint)
            y += 18f
            canvas.drawText(business.name, 40f, y, boldBodyPaint)
            y += 25f

            // Situação Cadastral
            checkAndCreateNewPage(20f)
            canvas.drawText("Situação Cadastral:", 40f, y, sectionPaint)
            val statusText = business.situacaoCadastral.ifEmpty { "INATIVA" }
            canvas.drawText(statusText, 170f, y, bodyPaint)
            y += 20f

            // CNAE / Atividade
            if (business.cnae.isNotEmpty()) {
                checkAndCreateNewPage(40f)
                canvas.drawText("Atividade (CNAE):", 40f, y, sectionPaint)
                y += 18f
                canvas.drawText(business.cnae, 40f, y, bodyPaint)
                y += 22f
            }

            // Capital Social
            checkAndCreateNewPage(20f)
            canvas.drawText("Capital Social:", 40f, y, sectionPaint)
            canvas.drawText(business.capitalSocialFormatado, 170f, y, bodyPaint)
            y += 20f

            // Porte Estimado
            checkAndCreateNewPage(20f)
            canvas.drawText("Porte Estimado:", 40f, y, sectionPaint)
            canvas.drawText(business.estimativaFaturamento, 170f, y, bodyPaint)
            y += 20f

            // Endereço
            if (business.endereco.isNotEmpty()) {
                checkAndCreateNewPage(45f)
                canvas.drawText("Endereço:", 40f, y, sectionPaint)
                y += 18f
                val fullAddress = "${business.endereco}, ${business.municipio} - ${business.uf}"
                canvas.drawText(fullAddress, 40f, y, bodyPaint)
                y += 25f
            }

            // Divisor para Quadro de Sócios
            checkAndCreateNewPage(25f)
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            // Quadro de Sócios e Administradores (QSA)
            checkAndCreateNewPage(20f)
            canvas.drawText("👥 Quadro de Sócios e Administradores (QSA):", 40f, y, sectionPaint)
            y += 20f

            if (business.qsa.isEmpty()) {
                checkAndCreateNewPage(20f)
                canvas.drawText("• Nenhum sócio/administrador informado na Receita.", 45f, y, bodyPaint)
                y += 20f
            } else {
                business.qsa.forEach { socio ->
                    checkAndCreateNewPage(18f) // Verifica se o próximo sócio cabe na página
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
        val fileName = "Relatorio_${cleanName}.pdf"

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
                Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_LONG).show()

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
                Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_LONG).show()

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