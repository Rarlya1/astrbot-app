package com.astrbot.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UbuntuDocumentsProvider extends DocumentsProvider {

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS
    };

    private File getUbuntuRoot() {
        Context ctx = getContext();
        if (ctx == null) return null;
        return new File(ctx.getFilesDir(), "ubuntu");
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        return null;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable Bundle signal)
            throws FileNotFoundException {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    // ── Simplified: serve files from ubuntu directory ──
    public static File getUbuntuFile(Context context, String path) {
        return new File(new File(context.getFilesDir(), "ubuntu"), path);
    }

    public static String getMimeType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime != null ? mime : "application/octet-stream";
    }
}
