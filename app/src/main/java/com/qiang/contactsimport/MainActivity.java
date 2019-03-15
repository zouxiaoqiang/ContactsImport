package com.qiang.contactsimport;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaoqiang
 * @date 19-3-13
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    private List<String> mPermissionList = new ArrayList<>();

    private int mRequestCode = 1;

    /**
     * 应用需要使用的权限
     */
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkSelfPermissions();
    }

    private void checkSelfPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        if (mPermissionList.isEmpty()) {
            initView();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    PERMISSIONS, mRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == mRequestCode) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    private void initView() {
        TextView tvImport = findViewById(R.id.tv_import);
        tvImport.setOnClickListener(this);
        TextView tvExport = findViewById(R.id.tv_export);
        tvExport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_import:
                Intent i = new Intent(MainActivity.this, ContactsImportService.class);
                i.putExtra("op", "import");
                startService(i);
                break;
            case R.id.tv_export:
                Intent i1 = new Intent(MainActivity.this, ContactsImportService.class);
                i1.putExtra("op", "export");
                startService(i1);
                break;
            default:
        }
    }
}
