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
        if (permissions == null || permissions.length == 0)
            throw new IllegalArgumentException("mPermissions no found...");
        if (acpListener == null) throw new NullPointerException("AcpListener is null...");
        mPermissions = permissions;
        mCallback = acpListener;
        mCallback.onStart();
    }

    /**
     * 检查权限
     */
    synchronized void checkSelfPermission() {
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
                Log.i(TAG, "checkSelfPermission = " + checkSelfPermission);
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
        startAcpActivity();
    }

    /**
     * 启动处理权限过程的 Activity
     */
    private synchronized void startAcpActivity() {
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
        boolean shouldShowRational = false;
        //如果有则提示申请理由提示框，否则直接向系统请求权限
        for (String permission : mDeniedPermissions) {
            shouldShowRational = shouldShowRational || mService.shouldShowRequestPermissionRationale(mActivity, permission);
        }
        Log.i(TAG, "shouldShowRational = " + shouldShowRational);
        String[] permissions = mDeniedPermissions.toArray(new String[mDeniedPermissions.size()]);
        //如选择了不再提醒，则回调，根据返回 boolean 确定是否继续
        if (shouldShowRational) mCallback.onShowRational(permissions);
        else requestPermissions(permissions);
    }

    /**
     * 向系统请求权限
     *
     * @param permissions
     */
    synchronized void requestPermissions(String[] permissions) {
        mService.requestPermissions(mActivity, permissions, Acp.REQUEST_CODE_PERMISSION);
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
            case Acp.REQUEST_CODE_PERMISSION:
                LinkedList<String> grantedPermissions = new LinkedList<>();
                LinkedList<String> deniedPermissions = new LinkedList<>();
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        grantedPermissions.add(permission);
                    else deniedPermissions.add(permission);
                }
                onDestroy();
                //全部允许才回调 onGranted 否则只要有一个拒绝都回调 onDenied
                if (!grantedPermissions.isEmpty() && deniedPermissions.isEmpty()) {
                    mCallback.onGranted();
                } else {
                    mCallback.onDenied(deniedPermissions);
                }
                break;
        }
    }

    /**
     * 响应设置权限返回结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCallback == null || requestCode != Acp.REQUEST_CODE_SETTING) {
            onDestroy();
            return;
        }
        checkSelfPermission();
    }

    /**
     * 摧毁本库的 AcpActivity
     */
    private void onDestroy() {
        if (mActivity != null) mActivity.finish();
    }
}
