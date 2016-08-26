package com.mylhyl.acp3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by hupei on 2016/8/26.
 */
class AcpManager {
    private static final String TAG = "AcpManager";

    private static final int REQUEST_CODE_PERMISSION = 0x38;

    private Context mContext;
    private Activity mActivity;
    private AcpService mService;
    private AcpListener mCallback;
    private String[] mPermissions;
    private final List<String> mDeniedPermissions = new LinkedList<>();
    private final Set<String> mManifestPermissions = new HashSet<>(1);

    private static final Object lock = new Object();
    private static volatile AcpManager mInstance;

    public static void getInstance() {
        if (mInstance == null)
            synchronized (lock) {
                if (mInstance == null) {
                    mInstance = new AcpManager();
                }
            }
        Acp.setAcpManager(mInstance);
    }

    AcpManager() {
        mContext = Acp.app();
        mService = new AcpService();
        getManifestPermissions();
    }

    private synchronized void getManifestPermissions() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null) {
                for (String perm : permissions) {
                    mManifestPermissions.add(perm);
                }
            }
        }
    }

    /**
     * 开始执行
     *
     * @param permissions
     * @param acpListener
     */
    synchronized void execute(String[] permissions, AcpListener acpListener) {
        mPermissions = permissions;
        mCallback = acpListener;
        checkSelfPermission();
    }

    /**
     * 检查权限
     */
    synchronized void checkSelfPermission() {

        if (mPermissions == null || mPermissions.length == 0)
            throw new IllegalArgumentException("mPermissions no found...");
        if (mCallback == null) throw new NullPointerException("AcpListener is null...");

        mDeniedPermissions.clear();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.i(TAG, "Build.VERSION.SDK_INT < Build.VERSION_CODES.M");
            mCallback.onGranted();
            onDestroy();
            return;
        }
        for (String permission : mPermissions) {
            //检查申请的权限是否在 AndroidManifest.xml 中
            if (mManifestPermissions.contains(permission)) {
                int checkSelfPermission = mService.checkSelfPermission(mContext, permission);
                Log.i(TAG, permission + " = checkSelfPermission:" + checkSelfPermission);
                //如果是拒绝状态则装入拒绝集合中
                if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                    mDeniedPermissions.add(permission);
                }
            }
        }
        //检查如果没有一个拒绝响应 onGranted 回调
        if (mDeniedPermissions.isEmpty()) {
            Log.i(TAG, "mDeniedPermissions.isEmpty()");
            mCallback.onGranted();
            onDestroy();
            return;
        }
        mCallback.onStart(new PermissionRequest() {
            @Override
            public void onPositive() {
                startAcpActivity();
            }
        });
    }

    /**
     * 启动处理权限过程的 Activity
     */
    synchronized void startAcpActivity() {
        Intent intent = new Intent(mContext, AcpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 检查权限是否存在拒绝不再提示
     *
     * @param activity
     */
    synchronized void checkRequestPermissionRationale(Activity activity) {
        mActivity = activity;
        boolean rationale = false;
        //如果有拒绝则提示申请理由提示框，否则直接向系统请求权限
        for (String permission : mDeniedPermissions) {
            rationale = rationale || mService.shouldShowRequestPermissionRationale(mActivity, permission);
        }
        Log.i(TAG, "rationale = " + rationale);
        //如选择了不再提醒，则回调
        if (rationale) mCallback.onShowRational(new PermissionRequest() {
            @Override
            public void onPositive() {
                requestPermissions();
            }
        });
        //处理不再提醒时，重新再次请求
        else requestPermissions();
    }

    /**
     * 向系统请求权限
     */
    synchronized void requestPermissions() {
        String[] permissions = mDeniedPermissions.toArray(new String[mDeniedPermissions.size()]);
        mService.requestPermissions(mActivity, permissions, REQUEST_CODE_PERMISSION);
    }

    /**
     * 响应向系统请求权限结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    synchronized void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                LinkedList<String> grantedPermissions = new LinkedList<>();
                LinkedList<String> deniedPermissions = new LinkedList<>();
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        grantedPermissions.add(permission);
                    else deniedPermissions.add(permission);
                }
                //全部允许才回调 onGranted 否则只要有一个拒绝都回调 onDenied
                if (!grantedPermissions.isEmpty() && deniedPermissions.isEmpty()) {
                    mCallback.onGranted();
                } else {
                    mCallback.onDenied(deniedPermissions);
                }
                onDestroy();
                break;
        }
    }

    /**
     * 摧毁本库的 AcpActivity
     */
    private void onDestroy() {
        if (mActivity != null) mActivity.finish();
    }
}
