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

	static {
		CLICK_TIMESTAMP = new HashMap<>();
		FAST_CLICK_INTERVAL = 800;
	}

	public static boolean isFastClick(Activity activity) {
		boolean result = false;
		long currentTime = System.currentTimeMillis();
		try {
			result = (currentTime - CLICK_TIMESTAMP.getOrDefault(activity, 0L)) < FAST_CLICK_INTERVAL;
		} finally {
			CLICK_TIMESTAMP.put(activity, currentTime);
			return result;
		}
	}
}
