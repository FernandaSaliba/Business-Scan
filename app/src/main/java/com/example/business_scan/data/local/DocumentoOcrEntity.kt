package com.example.business_scan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documentos_ocr")
data class DocumentoOcrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val textoExtraido: String,
    val dataLeitura: Long = System.currentTimeMillis()
)

