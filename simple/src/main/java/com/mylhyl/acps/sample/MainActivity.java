package com.mylhyl.acps.sample;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mylhyl.acp3.Acp;
import com.mylhyl.acp3.AcpListener;
import com.mylhyl.acp3.PermissionRequest;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AcpListener {
    public static final int REQUEST_CODE_SETTING = 0x39;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Acp.execute(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE}, this);
    }

    @Override
    public void onStart(final PermissionRequest request) {
        //可以不用提示，直接 request.onExecute();
        new AlertDialog.Builder(this)
                .setMessage("XX需要获取【存储空间】与【地地位置】权限，以保证XX正常使用以及您的账号安全")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.onExecute();
                    }
                }).show();
    }

    @Override
    public void onShowRational(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("你要是再不同意，就不能用咯")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.onExecute();
                    }
                }).show();
    }

    @Override
    public void onGranted() {
        Toast.makeText(MainActivity.this, "全部同意", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDenied(List<String> permissions) {
        new AlertDialog.Builder(mContext)
                .setMessage("如果拒绝将不能用咯")
                .setCancelable(false)
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "选择了拒绝", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "跳转权限设置", Toast.LENGTH_SHORT).show();
                        startSetting();
                    }
                }).show();
    }

    private void startSetting() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_SETTING);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_SETTING);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Acp.reExecute();
    }
}
