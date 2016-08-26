package com.mylhyl.acp3;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Created by hupei on 2016/8/26.
 */
public class Acp {

    private static Application mApp;
    private static AcpManager mAcpManager;

    private Acp() {
    }

    public static void init(Application app) {
        if (mApp == null) {
            mApp = app;
        }
    }

    public static Application app() {
        if (mApp == null) {
            try {
                Class<?> renderActionClass = Class.forName("com.android.layoutlib.bridge.impl.RenderAction");
                Method method = renderActionClass.getDeclaredMethod("getCurrentContext");
                Context context = (Context) method.invoke(null);
                mApp = new MockApplication(context);
            } catch (Throwable ignored) {
                throw new RuntimeException("please invoke acp.init(app) on Application#onCreate()"
                        + " and register your Application in manifest.");
            }
        }
        return mApp;
    }

    static void setAcpManager(AcpManager acpManager) {
        mAcpManager = acpManager;
    }

    /**
     * 开始请求
     *
     * @param permissions
     * @param acpListener
     */
    public static void execute(String[] permissions, AcpListener acpListener) {
        getAcpManager().execute(permissions, acpListener);
    }

    /**
     * 重新请求，用在如：跳转到权限设置界面，onActivityResult 接收时调用
     */
    public static void reExecute() {
        getAcpManager().checkSelfPermission();
    }

    static AcpManager getAcpManager() {
        if (mAcpManager == null)
            AcpManager.getInstance();
        return mAcpManager;
    }

    private static class MockApplication extends Application {
        public MockApplication(Context baseContext) {
            this.attachBaseContext(baseContext);
        }
    }
}
