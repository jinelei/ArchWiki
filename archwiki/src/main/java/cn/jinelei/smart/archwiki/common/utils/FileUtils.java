package cn.jinelei.smart.archwiki.common.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FileUtils {
	public static String getFromAssets(Context context, String fileName) {
		StringBuffer result = new StringBuffer();
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line;
			while ((line = bufReader.readLine()) != null)
				result.append(line);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result.toString();
		}
	}
}
