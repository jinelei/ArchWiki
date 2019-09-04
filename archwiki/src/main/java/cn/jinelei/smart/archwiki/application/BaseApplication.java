package cn.jinelei.smart.archwiki.application;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * @author jinelei
 */
public class BaseApplication extends Application {
	private static final String TAG = "BaseApplication";

	public Optional<ClipboardManager> optClipboardManager = Optional.empty();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		Stetho.initializeWithDefaults(this);
//        Thread.setDefaultUncaughtExceptionHandler(JyCrashHandler.getInstance());
		optClipboardManager = Optional.ofNullable((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
		optClipboardManager.ifPresent(c -> c.addPrimaryClipChangedListener(mPrimaryClipChangedListener));
	}

	private ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = () -> {
		String clip = optClipboardManager
			.flatMap(clipboardManager -> Optional.ofNullable(clipboardManager.getPrimaryClip()))
			.filter(c -> c.getItemCount() > 0)
			.flatMap(c -> Optional.ofNullable(c.getItemAt(0)))
			.flatMap(i -> Optional.ofNullable(i.getText()))
			.map(CharSequence::toString)
			.orElse("nothing");
		if ("nothing".equals(clip)) {
			Log.d(TAG, "mPrimaryClipChangedListener: nothing");
		} else {
			Log.d(TAG, "mPrimaryClipChangedListener: " + clip);
		}
	};

}

