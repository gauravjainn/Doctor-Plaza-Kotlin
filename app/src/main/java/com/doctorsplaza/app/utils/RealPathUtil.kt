package com.doctorsplaza.app.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.loader.content.CursorLoader


object RealPathUtil {
    fun getRealPath(context: Context, fileUri: Uri): String? {
        // SDK < API11
        val realPath: String? = when {
            Build.VERSION.SDK_INT < 11 -> {
                getRealPathFromURI_BelowAPI11(context, fileUri)
            }
            Build.VERSION.SDK_INT < 19 -> {
                getRealPathFromURI_API11to18(context, fileUri)
            }
            else -> {
                getRealPathFromURI_API19(context, fileUri)
            }
        }
        return realPath
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API11to18(context: Context, contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null
        val cursorLoader = CursorLoader(context, contentUri!!, proj, null, null, null)
        val cursor: Cursor = cursorLoader.loadInBackground()!!
        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
        }
        return result
    }

    fun getRealPathFromURI_BelowAPI11(context: Context, contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)!!
        var column_index = 0
        var result = ""
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
            return result
        }
        return result
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]

                    // This is for checking Main Memory
                    return if ("primary".equals(type, ignoreCase = true)) {
                        if (split.size > 1) {
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        } else {
                            Environment.getExternalStorageDirectory().toString() + "/"
                        }
                        // This is for checking SD Card
                    } else {
                        "storage" + "/" + docId.replace(":", "/")
                    }
                }
                isDownloadsDocument(uri) -> {
                    val fileName = getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                    }
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                }
                isDocument(uri) -> {
                    val fileName = getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/Document/" + fileName
                    }
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://documents/public_downloads"), java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        "document" -> {
                            contentUri = MediaStore.Files.getContentUri("external")
                        }
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isDocument(uri: Uri): Boolean {
        return "com.android.providers.documents.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}