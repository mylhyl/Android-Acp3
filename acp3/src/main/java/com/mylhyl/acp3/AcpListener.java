package com.mylhyl.acp3;

import java.util.List;

/**
 * Created by hupei on 2016/8/26.
 */
public interface AcpListener {
    /**
     * 用户勾选了不再提醒时，此方法将会调用
     *
     * @return
     */
    boolean onShowRational();

    /**
     * 全部同意通过后，此方法将会调用
     */
    void onGranted();

    /**
     * 只要有一个拒绝，此方法就会调用
     */
    void onDenied(List<String> permissions);
}
