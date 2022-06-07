package com.doctorsplaza.app.utils

import android.R
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsProvider
import java.io.File
import java.io.FileNotFoundException


class MyContentProvider : DocumentsProvider() {
    var matrixCursor: MatrixCursor? = null
    var matrixRootCursor: MatrixCursor? = null
    override fun onCreate(): Boolean {
        matrixRootCursor = MatrixCursor(rootColumns)
        matrixRootCursor!!.addRow(arrayOf<Any>(1, 1, "TEST", R.mipmap.sym_def_app_icon))
        matrixCursor = MatrixCursor(docColumns)
        matrixCursor!!.addRow(arrayOf<Any>(1, 1, "a.txt", "text/plain", R.mipmap.sym_def_app_icon))
        matrixCursor!!.addRow(arrayOf<Any>(2, 2, "b.txt", "text/plain", R.mipmap.sym_def_app_icon))
        matrixCursor!!.addRow(arrayOf<Any>(3, 3, "c.txt", "text/plain", R.mipmap.sym_def_app_icon))
        return true
    }

    @Throws(FileNotFoundException::class)
    override fun queryRoots(projection: Array<String>): Cursor {
        return matrixRootCursor!!
    }

    @Throws(FileNotFoundException::class)
    override fun queryDocument(documentId: String, projection: Array<String>): Cursor {
        return matrixCursor!!
    }

    @Throws(FileNotFoundException::class)
    override fun queryChildDocuments(
        parentDocumentId: String, projection: Array<String>,
        sortOrder: String
    ): Cursor {
        return matrixCursor!!
    }

    @Throws(FileNotFoundException::class)
    override fun openDocument(
        documentId: String, mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val id: Int = try {
            Integer.valueOf(documentId)
        } catch (e: NumberFormatException) {
            throw FileNotFoundException("Incorrect document ID $documentId")
        }
        var filename = "/sdcard/"
        filename += when (id) {
            1 -> "a.txt"
            2 -> "b.txt"
            3 -> "c.txt"
            else -> throw FileNotFoundException("Unknown document ID $documentId")
        }
        return ParcelFileDescriptor.open(
            File(filename),
            ParcelFileDescriptor.MODE_READ_WRITE
        )
    }

    companion object {
        private val rootColumns = arrayOf(
            "_id", "root_id", "title", "icon"
        )
        private val docColumns = arrayOf(
            "_id", "document_id", "_display_name", "mime_type", "icon"
        )
    }
}