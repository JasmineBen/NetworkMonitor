package com.conan.networkmonitor;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class PermissionHelper {

    //检查悬浮窗  流量获取权限
    public static void checkPermissions(Context context) {
        checkOverlayPermission(context);
        checkNetworkHistoryPermission(context);
    }

    //检查悬浮窗权限
    private static void checkOverlayPermission(Context context) {
        if (!hasOverlayPermission(context)) {
            if (!Settings.canDrawOverlays(context)) {
                context.startActivity(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                );
                Toast.makeText(context, "请先授予 \"NetworkMonitor\" 悬浮窗权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    //流量获取权限
    private static void checkNetworkHistoryPermission(Context context) {
        if (!hasNetworkHistoryPermissions(context)) {
            requestNetworkHistoryPermission(context);
        }
    }


    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }


    private static boolean hasNetworkHistoryPermissions(Context context) {
        return hasPermissionToReadNetworkHistory(context) && hasPermissionToReadPhoneStats(context);
    }

    private static boolean hasPermissionToReadPhoneStats(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasPermissionToReadNetworkHistory(final Context context) {
        final AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        return false;
    }

    public static void requestNetworkHistoryPermission(Context context) {
        if (!PermissionHelper.hasPermissionToReadNetworkHistory(context)) {
            requestReadNetworkHistoryAccess(context);
        }
        if (!PermissionHelper.hasPermissionToReadPhoneStats(context)) {
            requestPhoneStateStats(context);
        }
    }


    private static final int READ_PHONE_STATE_REQUEST = 37;

    private static void requestPhoneStateStats(Context context) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private static void requestReadNetworkHistoryAccess(final Context context) {
        final AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                context.getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), context.getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                    }
                });
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    public static boolean checkAccessibility(Context context) {
        // 判断辅助功能是否开启
        if (!isAccessibilitySettingsOn(context)) {
            // 引导至辅助功能设置页面
            context.startActivity(
                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            return false;
        }
        return true;
    }

    private static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }
}
