package cn.jinelei.smart.archwiki.application;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * @author jinelei
 */
public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";
    public static final SimpleDateFormat
            sdf_yyyy_MM_dd_hh_mm_ss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    public static final SimpleDateFormat
            sdf_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat
            sdf_hh_mm_ss = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());

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

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}

