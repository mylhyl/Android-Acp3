package com.mylhyl.acp3;

import java.util.List;

/**
 * Created by hupei on 2016/8/26.
 */
public interface AcpListener {
    /**
     * 申请权限开始前，此方法会调用
     */
    void onStart(PermissionRequest request);

    /**
     * 用户勾选了不再提醒时，此方法将会调用
     */
    void onShowRational(PermissionRequest request);

    /**
     * 全部同意通过后，此方法将会调用
     */
    void onGranted();

    /**
     * 只要有一个拒绝，此方法就会调用
     */
    void onDenied(List<String> permissions);
}
