package com.qiang.contactsimport;

import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * @author xiaoqiang
 * 19-3-15
 */
public class ContactsImportService extends Service {

    public static String TAG = "ContactsImportService";
    private static final String IMPORT = "import";
    private static final String EXPORT = "export";

    private String[] data = new String[2];

    private static final String FILE_URI = "content://com.qiang.contactsimport/contacts";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String op = intent.getStringExtra("op");
        if (IMPORT.equals(op)) {
            importContacts();
        } else if (EXPORT.equals(op)) {
            exportContacts();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void importContacts() {
        try (InputStream is = getContentResolver().openInputStream
                (Uri.parse(FILE_URI + "/contacts.txt"));
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String ret;
            while ((ret = br.readLine()) != null) {
                String[] ss = ret.trim().split(",");
                if (ss.length != data.length) {
                    continue;
                }
                System.arraycopy(ss, 0, data, 0, data.length);
                WorkHandler.getWorkHandler().post(new AddContact(data[0], data[1]));
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private class AddContact implements Runnable {
        private String displayName;
        private String number;

        AddContact(String displayName, String number) {
            this.displayName = displayName;
            this.number = number;
        }

        @Override
        public void run() {
            Cursor cursor = getContentResolver().query(Data.CONTENT_URI, null,
                    CommonDataKinds.Phone.NUMBER + "=?" + " AND " +
                            Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    new String[]{String.valueOf(number)}, null);

            if (cursor == null) {
                return;
            } else if (cursor.getCount() != 0) {
                cursor.close();
                return;
            }
            ContentResolver resolver = getContentResolver();
            cursor = resolver.query(RawContacts.CONTENT_URI, null, null, null, null);
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
            int contactId = count + 1;

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.CONTACT_ID, contactId)
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValue(Data.RAW_CONTACT_ID, contactId)
                    .withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValue(Data.RAW_CONTACT_ID, contactId)
                    .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, number)
                    .build());

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportContacts() {
        File out = new File(Environment.getExternalStorageDirectory(), "contacts/contacts.txt");
        if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
            Log.d(TAG, "新建路径上的文件夹失败");
            return;
        }
        try {
            if (!out.delete() && !out.createNewFile()) {
                Log.d(TAG, "新建输出文件失败");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WorkHandler.getWorkHandler().post(new ExportContacts());
    }

    private class ExportContacts implements Runnable {

        @Override
        public void run() {
            try (Cursor cursor = getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String displayName = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.d(TAG, "export {" + displayName + ", " + number + "}");
                        data[0] = displayName;
                        data[1] = number;
                        try (OutputStream os = getContentResolver().openOutputStream
                                (Uri.parse(FILE_URI + "/contacts.txt"), "wa");
                             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
                            for (String s : data) {
                                bw.append(s).append(",");
                            }
                            bw.append("\n");
                            bw.flush();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
