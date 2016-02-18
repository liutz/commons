package com.huami.android.commons.toolbox;

import java.lang.reflect.Method;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class NetWorkAssistor {
    private static final String METHOD_GET_MOBILE_DATA_ENABLED = "getMobileDataEnabled";
    private static final String TAG = "NetWorkAssistor";

    public static boolean getMobileDataEnabled(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        try {
            return (Boolean) invokeMethod(context, METHOD_GET_MOBILE_DATA_ENABLED, null);
        } catch (Exception e) {
           e.printStackTrace();
        }
        return false;
    }

    public static int getWLANSignalLevel(final Context context) {
        if (context == null) {
            return 0;
        }
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        Log.i("Net", "ssi=" + wifiInfo.getRssi());
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
    }

    public static boolean isBluetoothEnabled() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return false;
            }
            return adapter.isEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isMobile2GNetworkConnected(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info == null) {
                return false;
            }
            for (NetworkInfo element : info) {
                if (element.getType() != ConnectivityManager.TYPE_MOBILE
                                || element.getState() != NetworkInfo.State.CONNECTED) {
                    continue;
                }
                int subType = element.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_CDMA
                                || subType == TelephonyManager.NETWORK_TYPE_EDGE
                                || subType == TelephonyManager.NETWORK_TYPE_LTE
                                || subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMobileNetwork(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetInfo == null) {
            return false;
        }
        if (mobileNetInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkAvailable(final Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
            return true;
        }
        if (mobileNetInfo != null && mobileNetInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkConnected(Context c) {
        ConnectivityManager conManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conManager.getActiveNetworkInfo() != null) {
            return conManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    /**
     * Returns whether the network is wifi
     */
    public static boolean isWifiNetworkConned(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        try {
            ConnectivityManager connManager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetInfo == null) {
                return false;
            }
            if (wifiNetInfo.isConnected()) {
                return true;
            }
            return false;
        } catch (Exception e) {
           e.printStackTrace();
        }
        return false;
    }

    private static Object invokeMethod(final Context context, final String methodName, final Object[] arg)
                    throws Exception {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<? extends ConnectivityManager> ownerClass = connectivity.getClass();
        if (arg == null) {
            Method method = ownerClass.getMethod(methodName);
            return method.invoke(connectivity);
        }
        Method method = ownerClass.getMethod(methodName, arg.getClass());
        return method.invoke(connectivity, arg);
    }

    private NetWorkAssistor() {}
}
