package cn.jinelei.smart.archwiki.views;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class LanguageDialog extends AlertDialog {
	protected LanguageDialog(@NonNull Context context) {
		super(context);
	}

	protected LanguageDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected LanguageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}
}
