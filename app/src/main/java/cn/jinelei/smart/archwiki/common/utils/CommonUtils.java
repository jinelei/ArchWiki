package cn.jinelei.smart.archwiki.common.utils;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jinelei
 */
public class CommonUtils {
	private static final Map<Activity, Long> CLICK_TIMESTAMP;
	private static final long FAST_CLICK_INTERVAL;
	private static final long FAST_LOAD_INTERVAL;
	private static long lastLoadTimestamp;
	
	static {
		CLICK_TIMESTAMP = new HashMap<>();
		FAST_CLICK_INTERVAL = 500;
		FAST_LOAD_INTERVAL = 500;
		lastLoadTimestamp = 0L;
	}
	
	public static boolean isFastClick(Activity activity) {
		long currentTime = System.currentTimeMillis();
		try {
			Long lastClickTime = CLICK_TIMESTAMP.getOrDefault(activity, currentTime);
			return (System.currentTimeMillis() - lastClickTime) > FAST_CLICK_INTERVAL;
		} catch (Exception e) {
			return false;
		} finally {
			CLICK_TIMESTAMP.put(activity, currentTime);
		}
	}
	
	public static boolean isFastLoadWebView() {
		long currentTime = System.currentTimeMillis();
		try {
			return currentTime - lastLoadTimestamp < FAST_LOAD_INTERVAL;
		} finally {
			lastLoadTimestamp = currentTime;
		}
	}
}
