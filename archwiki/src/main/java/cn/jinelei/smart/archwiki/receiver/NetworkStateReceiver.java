package cn.jinelei.smart.archwiki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

public class NetworkStateReceiver extends BroadcastReceiver {
	private static final String TAG = "NetworkStateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.d(TAG, "onReceive: NetworkBroadcast");
//		ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
//		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
//		if (networkInfo != null && networkInfo.isAvailable()) {
//			switch (networkInfo.getType()) {
//				case TYPE_MOBILE:
//					Toast.makeText(context, "正在使用2G/3G/4G网络", Toast.LENGTH_SHORT).show();
//					break;
//				case TYPE_WIFI:
//					Toast.makeText(context, "正在使用wifi上网", Toast.LENGTH_SHORT).show();
//					break;
//				default:
//					break;
//			}
//		} else {
//			Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
//		}

		Log.d(TAG, "onReceive: 网络状态发生变化");
		//检测API是不是小于21，因为到了API21之后getNetworkInfo(int networkType)方法被弃用
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

			//获得ConnectivityManager对象
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			//获取ConnectivityManager对象对应的NetworkInfo对象
			//获取WIFI连接的信息
			NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			//获取移动数据连接的信息
			NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
				Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
			} else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
				Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
			} else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
				Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
			}
		}else {
			//这里的就不写了，前面有写，大同小异
			System.out.println("API level 大于21");
			//获得ConnectivityManager对象
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			//获取所有网络连接的信息
			Network[] networks = connMgr.getAllNetworks();
			//用于存放网络连接信息
			StringBuilder sb = new StringBuilder();
			//通过循环将网络信息逐个取出来
			for (int i=0; i < networks.length; i++){
				//获取ConnectivityManager对象对应的NetworkInfo对象
				NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
				sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
			}
			Toast.makeText(context, sb.toString(),Toast.LENGTH_SHORT).show();
		}
	}
}
