package com.mylhyl.acps.sample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mylhyl.acp3.Acp;
import com.mylhyl.acp3.AcpListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , new AcpListener() {
                    @Override
                    public void onStart() {
                        onStartDialog();
                    }

                    @Override
                    public void onShowRational(String[] permissions) {
                        onShowRationalDialog(permissions);
                    }

                    @Override
                    public void onGranted() {
                        Toast.makeText(MainActivity.this, "全部同意", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        onShowDeniedDialog();
                    }
                });
    }

    private void onStartDialog() {
        new AlertDialog.Builder(this)
                .setMessage("XX需要获取【存储空间】与【地地位置】权限，以保证XX正常使用以及您的账号安全")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Acp.checkSelfPermission();
                    }
                }).show();
    }


    private void onShowRationalDialog(final String[] permissions) {
        new AlertDialog.Builder(this)
                .setMessage("你要是再不同意，就不能用咯")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Acp.requestPermissions(permissions);
                    }
                }).show();
    }

    private void onShowDeniedDialog() {
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
                    }
                }).show();
    }
}
