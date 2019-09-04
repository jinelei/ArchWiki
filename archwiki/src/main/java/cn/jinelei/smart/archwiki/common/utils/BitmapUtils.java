package cn.jinelei.smart.archwiki.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitmapUtils {
	public static String bitmapToBase64(Bitmap bitmap) {
		if (bitmap == null)
			return "";
		String result = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			baos.flush();
			baos.close();
			byte[] bitmapBytes = baos.toByteArray();
			result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}

	/**
	 * base64转为bitmap
	 *
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
}
