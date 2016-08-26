package com.mylhyl.acps.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mylhyl.acp3.Acp;
import com.mylhyl.acp3.AcpListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        setContentView(R.layout.activity_main);
        Acp.getInstance(this).request(null, new AcpListener() {
            @Override
            public boolean onShowRational() {
                onShowRationalDialog();
                return true;
            }

            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> permissions) {

            }
        });
    }

    private void onShowRationalDialog() {
        new AlertDialog.Builder(mContext)
                .setMessage("你要是再不同意，就不能用咯")
                .setPositiveButton("知道了", null).show();
    }

    private void onShowDeniedDialog() {
        new android.app.AlertDialog.Builder(mContext)
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
