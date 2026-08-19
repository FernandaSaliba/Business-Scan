package com.example.business_scan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoOcrDao {

    // Busca todos os documentos, do mais novo para o mais antigo
    @Query("SELECT * FROM documentos_ocr ORDER BY dataLeitura DESC")
    fun getAllDocumentos(): Flow<List<DocumentoOcrEntity>>

    // Insere um novo documento lido
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: DocumentoOcrEntity)

    // Deleta um documento específico
    @Delete
    suspend fun deleteDocumento(documento: DocumentoOcrEntity)
}

