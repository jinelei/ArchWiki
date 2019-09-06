package cn.jinelei.smart.archwiki.receiver;

import cn.jinelei.smart.archwiki.models.NetworkType;

public interface NetworkStateObserver {
	void onNetworkChanged(NetworkType networkType);
}
