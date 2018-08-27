package com.conan.networkmonitor;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkStatsHelper {

    private static NetworkStatsManager networkStatsManager;
    private static NetworkStatsHelper mInstance;

    private NetworkStatsHelper(NetworkStatsManager networkStatsManager) {
        this.networkStatsManager = networkStatsManager;
    }

    public static synchronized NetworkStatsHelper getInstance(NetworkStatsManager networkStatsManager) {
        mInstance = new NetworkStatsHelper(networkStatsManager);
        return mInstance;
    }


    public long getPackageRxBytesMobile(Context context, String packageName, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            Log.i("zpy", "getPackageRxBytesMobile:" + e.getMessage());
            return -1;
        }

        long rxBytes = 0L;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            rxBytes += bucket.getRxBytes();
        }
        networkStats.close();
        return rxBytes;
    }

    public long getPackageTxBytesMobile(Context context, String packageName, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            Log.i("zpy", "getPackageTxBytesMobile:" + e.getMessage());
            return -1;
        }

        long txBytes = 0L;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            txBytes += bucket.getTxBytes();
        }
        networkStats.close();
        return txBytes;
    }

    public long getPackageRxBytesWifi(String packageName, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            Log.i("zpy", "getPackageRxBytesWifi:" + e.getMessage());
            return -1;
        }

        long rxBytes = 0L;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket);
            rxBytes += bucket.getRxBytes();
        }
        networkStats.close();
        return rxBytes;
    }

    public long getPackageTxBytesWifi(String packageName, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            Log.i("zpy", "getPackageTxBytesWifi:" + e.getMessage());
            return -1;
        }

        long txBytes = 0L;

        while (networkStats.hasNextBucket()) {
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            networkStats.getNextBucket(bucket);
            txBytes += bucket.getTxBytes();
        }
        networkStats.close();
        return txBytes;
    }

    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    public long getAllBytes(Context context, String packageName, int packageUid,long currentTime) {
        long rx = getRxBytes(context, ConnectivityManager.TYPE_MOBILE, packageUid,currentTime) + getRxBytes(context, ConnectivityManager.TYPE_WIFI, packageUid,currentTime);
        long tx = getTxBytes(context, ConnectivityManager.TYPE_MOBILE, packageUid,currentTime) + getTxBytes(context, ConnectivityManager.TYPE_WIFI, packageUid,currentTime);
        return rx + tx;
    }

    public long getAllRxBytes(Context context, int packageUid,long currentTime) {
        return getRxBytes(context, ConnectivityManager.TYPE_MOBILE, packageUid,currentTime) + getRxBytes(context, ConnectivityManager.TYPE_WIFI, packageUid,currentTime);
    }

    private long getRxBytes(Context context, int type, int packageUid,long currentTime) {
        NetworkStats networkStatsByApp;
        long currentUsage = 0L;
        try {
            networkStatsByApp = networkStatsManager.querySummary(type, getSubscriberId(context, type), 0, currentTime);
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStatsByApp.getNextBucket(bucket);
                if (bucket.getUid() == packageUid) {
                    //rajeesh : in some devices this is immediately looping twice and the second iteration is returning correct value. So result returning is moved to the end.
                    currentUsage = bucket.getRxBytes();
                }
            } while (networkStatsByApp.hasNextBucket());


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return currentUsage;
    }

    public long getAllTxBytes(Context context, int packageUid,long currentTime) {
        long mobile = getTxBytes(context, ConnectivityManager.TYPE_MOBILE, packageUid,currentTime);
        long wifi = getTxBytes(context, ConnectivityManager.TYPE_WIFI, packageUid,currentTime);
        return mobile + wifi;
    }

    private long getTxBytes(Context context, int type, int packageUid,long currentTime) {
        NetworkStats networkStatsByApp;
        long currentUsage = 0L;
        try {
            networkStatsByApp = networkStatsManager.querySummary(type, getSubscriberId(context, type), 0, currentTime);
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStatsByApp.getNextBucket(bucket);
                if (bucket.getUid() == packageUid) {
                    //rajeesh : in some devices this is immediately looping twice and the second iteration is returning correct value. So result returning is moved to the end.
                    currentUsage = bucket.getTxBytes();
                }
            } while (networkStatsByApp.hasNextBucket());


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return currentUsage;
    }

}
