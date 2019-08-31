package cn.jinelei.smart.archwiki.utils;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

public class CommonUtils {
	private static final Map<Activity, Long> CLICK_TIMESTAMP;
	private static final long FAST_CLICK_INTERVAL;
	
	static {
		CLICK_TIMESTAMP = new HashMap<>();
		FAST_CLICK_INTERVAL = 500;
	}
	
	public static boolean isFastClick(Activity activity) {
		long currentTime = System.nanoTime();
		try {
			Long lastClickTime = CLICK_TIMESTAMP.getOrDefault(activity, currentTime);
			return (System.nanoTime() - lastClickTime) > FAST_CLICK_INTERVAL;
		} catch (Exception e) {
			return false;
		} finally {
			CLICK_TIMESTAMP.put(activity, currentTime);
		}
	}
}
