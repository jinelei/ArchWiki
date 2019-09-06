package cn.jinelei.smart.archwiki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.jinelei.smart.archwiki.common.utils.SharedUtils;
import cn.jinelei.smart.archwiki.models.NetworkType;

public class NetworkStateReceiver extends BroadcastReceiver {
	private static final String TAG = "NetworkStateReceiver";
	private final List<NetworkStateObserver> observers = new ArrayList<>();

	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkType networkType;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
				networkType = NetworkType.CONNECT_WIFI_AND_CELLULAR;
			} else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
				networkType = NetworkType.CONNECT_WIFI_DISCONNECT_CELLULAR;
			} else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
				networkType = NetworkType.DISCONNECT_WIFI_CONNECT_CELLULAR;
			} else {
				networkType = NetworkType.DISCONNECT_WIFI_AND_CELLULAR;
			}
		} else {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Network[] networks = connMgr.getAllNetworks();
			boolean wifiConnect = false;
			boolean cellularConnect = false;
			for (int i = 0; i < networks.length; i++) {
				NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
				wifiConnect = networkInfo.getTypeName().equals("WIFI") && networkInfo.isConnected();
				cellularConnect = networkInfo.getTypeName().equals("MOBILE") && networkInfo.isConnected();
			}
			if (wifiConnect && cellularConnect) {
				networkType = NetworkType.CONNECT_WIFI_AND_CELLULAR;
			} else if (wifiConnect && !cellularConnect) {
				networkType = NetworkType.CONNECT_WIFI_DISCONNECT_CELLULAR;
			} else if (!wifiConnect && cellularConnect) {
				networkType = NetworkType.DISCONNECT_WIFI_CONNECT_CELLULAR;
			} else {
				networkType = NetworkType.DISCONNECT_WIFI_AND_CELLULAR;
			}
		}
		Log.d(TAG, "onReceive: new network state: " + networkType.getName());
		SharedUtils.setParam(context, SharedUtils.DEFAULT_NAME, SharedUtils.TAG_NETWORK, networkType.getName());
		observers.forEach(networkStateObserver -> networkStateObserver.onNetworkChanged(networkType));
	}

	public void registerNetworkStateObserver(NetworkStateObserver networkStateObserver) {
		observers.add(networkStateObserver);
	}

	public void unregisterNetworkStateObserver(NetworkStateObserver networkStateObserver) {
		observers.remove(networkStateObserver);
	}

}
