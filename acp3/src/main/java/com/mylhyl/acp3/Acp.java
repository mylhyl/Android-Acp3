package com.mylhyl.acp3;

import android.content.Context;

/**
 * Created by hupei on 2016/8/26.
 */
public class Acp {

    private static Acp mInstance;
    private AcpManager mAcpManager;

    public static Acp getInstance(Context context) {
        if (mInstance == null)
            synchronized (Acp.class) {
                if (mInstance == null) {
                    mInstance = new Acp(context);
                }
            }
        return mInstance;
    }

    private Acp(Context context) {
        mAcpManager = new AcpManager(context.getApplicationContext());
    }

    /**
     * 开始请求
     *
     * @param permissions
     * @param acpListener
     */
    public void request(String[] permissions, AcpListener acpListener) {
        mAcpManager.request(permissions, acpListener);
    }

    AcpManager getAcpManager() {
        return mAcpManager;
    }
}
