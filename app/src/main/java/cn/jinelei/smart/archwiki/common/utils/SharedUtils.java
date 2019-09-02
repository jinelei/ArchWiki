package cn.jinelei.smart.archwiki.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SharedUtils {
	private static final String TAG = "SharedUtils";
	public static final String DEFAULT_NAME = "DEFAULT_NAME";
	public static final String TAG_ALL_BOOKMARK = "TAG_ALL_BOOKMARK";
	
	/**
	 * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 *
	 * @param context
	 * @param key
	 * @param object
	 */
	public static void setParam(Context context, String name, String key, Object object) {
		String type = object.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		if ("String".equals(type)) {
			editor.putString(key, (String) object);
		} else if ("Integer".equals(type)) {
			editor.putInt(key, (Integer) object);
		} else if ("Boolean".equals(type)) {
			editor.putBoolean(key, (Boolean) object);
		} else if ("Float".equals(type)) {
			editor.putFloat(key, (Float) object);
		} else if ("Long".equals(type)) {
			editor.putLong(key, (Long) object);
		} else {
			try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(baos);
			) {
				os.writeObject(object);
				String output = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
				editor.putString(key, output);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		editor.apply();
	}
	
	
	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 *
	 * @param context
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object getParam(Context context, String name, String key, Object defaultObject) {
		String type = defaultObject.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		try {
			if ("String".equals(type)) {
				return sp.getString(key, (String) defaultObject);
			} else if ("Integer".equals(type)) {
				return sp.getInt(key, (Integer) defaultObject);
			} else if ("Boolean".equals(type)) {
				return sp.getBoolean(key, (Boolean) defaultObject);
			} else if ("Float".equals(type)) {
				return sp.getFloat(key, (Float) defaultObject);
			} else if ("Long".equals(type)) {
				return sp.getLong(key, (Long) defaultObject);
			} else {
				try {
					String input = sp.getString(key, defaultObject.toString());
					byte[] base64Bytes = Base64.decode(input.getBytes(), Base64.DEFAULT);
					ByteArrayInputStream ipos = new ByteArrayInputStream(base64Bytes);
					ObjectInputStream is = new ObjectInputStream(ipos);
					ipos.close();
					is.close();
					defaultObject = is.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Log.e("SharedPreUtil", "getParam", e);
			return defaultObject;
		}
		return defaultObject;
	}
}
