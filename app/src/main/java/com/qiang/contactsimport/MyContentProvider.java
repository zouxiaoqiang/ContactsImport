package com.qiang.contactsimport;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * @author xiaoqiang
 * @date 19-3-13
 */
public class MyContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.qiang.contactsimport";

    private static final int OP_FILE_CODE = 1;

    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "contacts/*", OP_FILE_CODE);
    }

    @Override
    public ParcelFileDescriptor openFile(@NotNull Uri uri, @NotNull String mode) throws FileNotFoundException {
        switch (sUriMatcher.match(uri)) {
            case OP_FILE_CODE:
                File f = new File(Environment.getExternalStorageDirectory().getPath(), uri.getPath());
                if (!f.exists()) {
                    Log.d(MainActivity.TAG, "文件被删除, 重新操作");
                    return null;
                }
                int imode = 0;
                imode = mode.contains("w") ? imode | ParcelFileDescriptor.MODE_WRITE_ONLY : imode;
                imode = mode.contains("r") ? imode | ParcelFileDescriptor.MODE_READ_ONLY : imode;
                imode = mode.contains("wa") ? imode | ParcelFileDescriptor.MODE_APPEND : imode;
                return ParcelFileDescriptor.open(f, imode);
            default:
                return null;
        }
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
