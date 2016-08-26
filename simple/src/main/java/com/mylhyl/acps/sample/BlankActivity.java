package com.mylhyl.acps.sample;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mylhyl.acp3.Acp;
import com.mylhyl.acp3.AcpListener;
import com.mylhyl.acp3.PermissionRequest;

import java.util.List;

public class BlankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, BlankFragment.newInstance()).commitAllowingStateLoss();
    }

    public static void gotoAct(Activity act) {
        act.startActivity(new Intent(act, BlankActivity.class));
    }

    public static class BlankFragment extends Fragment implements View.OnClickListener, AcpListener {
        public static final int REQUEST_CODE_SETTING = 0x39;

        public BlankFragment() {
        }


        public static BlankFragment newInstance() {
            BlankFragment fragment = new BlankFragment();
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Button button = new Button(getActivity());
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            button.setGravity(Gravity.CENTER);
            button.setText("测试Fragment中申请权限");
            button.setOnClickListener(this);
            return button;
        }

        @Override
        public void onClick(View v) {
            Acp.execute(new String[]{Manifest.permission.CAMERA}, this);
        }

        @Override
        public void onStart(final PermissionRequest request) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("XX需要获取【相机】权限，以保证XX正常使用以及您的账号安全")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            request.onExecute();
                        }
                    }).show();
        }

        @Override
        public void onShowRational(final PermissionRequest request) {
            new AlertDialog.Builder(getActivity())
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
            Toast.makeText(getActivity(), "全部同意", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDenied(List<String> permissions) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("如果拒绝将不能用咯")
                    .setCancelable(false)
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getActivity(), "选择了拒绝", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), "跳转权限设置", Toast.LENGTH_SHORT).show();
                            startSetting();
                        }
                    }).show();
        }

        private void startSetting() {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + getActivity().getPackageName()));
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
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Acp.reExecute();
        }
    }
}
