package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.jinelei.smart.archwiki.intf.IJavaScriptInterface;
import cn.jinelei.smart.archwiki.views.LanguagePopupWindow;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.CONFIRM_SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.HIDE_LOADING;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.SHOW_LOADING;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final String ARCH_URI;
	private static final String JS_SRC_RULE;
	private static final List<String> ALL_JAVASCRIPT_FUNCTION = new ArrayList<>();
	private static final List<LanguagePopupWindow.LanguageModel> ALL_LANGUAGE = new ArrayList<>();
	private LanguagePopupWindow.LanguageModel selectLanguageModel =
		new LanguagePopupWindow.LanguageModel("zh-Hans", "简体中文", "");
	private static final int GROUP_ID_LANGUAGE = 10;
	private static final int ITEM_ID_REFRESH = 12;
	private static final int ITEM_ID_LANGUAGE_PREFERENCE = 11;
	private FloatingActionsMenu fabMenu;
	private FloatingActionButton fab1;
	private FloatingActionButton fab2;
	private FloatingActionButton fab3;
	private ProgressBar progressBar;
	private WebView webview;
	private CoordinatorLayout clMain;
	private LanguagePopupWindow languagePopupWindow;
	private boolean isAutoLanguage = false;


	private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_LOADING:
					progressBar.setVisibility(View.VISIBLE);
					fabMenu.setEnabled(false);
					if (fabMenu.isExpanded()) {
						fabMenu.collapseImmediately();
					}
					break;
				case HIDE_LOADING:
					progressBar.setVisibility(View.GONE);
					fabMenu.setEnabled(true);
					if (fabMenu.isExpanded()) {
						fabMenu.collapseImmediately();
					}
					break;
				case SELECT_LANGUAGE:
					Log.d(TAG, "handleMessage: SELECT_LANGUAGE: " + msg.obj);
					if (msg.obj instanceof LanguagePopupWindow.LanguageModel) {
						selectLanguageModel = (LanguagePopupWindow.LanguageModel) msg.obj;
						languagePopupWindow.setSelectLanguageModel(selectLanguageModel);
					}
					break;
				case CONFIRM_SELECT_LANGUAGE:
					break;
			}
			return true;
		}
	});
	private final IJavaScriptInterface javaScriptInterface = new IJavaScriptInterface() {
		@JavascriptInterface
		public void startFunction() {
			Log.d(TAG, "js调用了java函数");
			Toast.makeText(MainActivity.this, "js调用了java函数", Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface
		public void startFunction(final String str) {
			Log.d(TAG, "js调用了java函数传递参数: " + str);
			Toast.makeText(MainActivity.this, "js调用了java函数传递参数: " + str, Toast.LENGTH_SHORT).show();
		}
	};
	private final WebViewClient webViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl().toString());
			try {
				view.loadUrl(request.getUrl().toString());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			webview.setVisibility(View.INVISIBLE);
			super.onPageStarted(view, url, favicon);
			Log.d(TAG, "onPageStarted: " + url);
			isAutoLanguage = false;
			handler.sendEmptyMessage(SHOW_LOADING);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished: " + url);
			//inject js script
			view.evaluateJavascript(String.format(JS_SRC_RULE,
				ALL_JAVASCRIPT_FUNCTION.stream().reduce("", (s, s2) -> s + s2)),
				value -> {
					Log.d(TAG, "inject js script result: ");
					// findAllLanguageAll
					webview.evaluateJavascript("javascript:findAllLanguageAll()", val -> {
						List<LanguagePopupWindow.LanguageModel> collect = Stream.of(val)
							.filter(s -> !s.equals("[]") && s.matches("\\[.*]"))
							.map(s -> s.substring(1, s.length() - 1))
							.flatMap(s -> Stream.of(s.split(",")))
							.filter(s -> !s.equals("\"\""))
							.map(s -> s.substring(1, s.length() - 1))
							.map(s -> s.split("\\^"))
							.filter(s -> s.length >= 3)
							.map(ss -> new LanguagePopupWindow.LanguageModel(ss[0], ss[1], ss[2]))
							.collect(Collectors.toList());
						Log.d(TAG, "evaluateJavascript: findAllLanguage: " + collect);
						ALL_LANGUAGE.clear();
						ALL_LANGUAGE.addAll(collect);
						languagePopupWindow.resetAllLanguage(collect);
						if (null != selectLanguageModel) {
							// findLanguageHref
							for (LanguagePopupWindow.LanguageModel languageModel : collect) {
								if (languageModel.getSummaryLang().equals(selectLanguageModel.getSummaryLang())
									&& null != languageModel.getHref() && !languageModel.getHref().equals("")) {
									isAutoLanguage = true;
									webview.loadUrl(languageModel.getHref());
								}
							}
						}
					});
					// autoHideLang
					webview.evaluateJavascript("javascript:autoHideElement()", val -> Log.d(TAG, "evaluateJavascript: autoHideElement"));
				});
			if (!isAutoLanguage) {
				webview.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(HIDE_LOADING);
			}
			super.onPageFinished(view, url);
		}
	};
	private final WebChromeClient webChromeClient = new WebChromeClient() {
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			new AlertDialog.Builder(MainActivity.this)
				.setTitle("JsAlert")
				.setMessage(message)
				.setPositiveButton("OK", (dialog, which) -> result.confirm())
				.setCancelable(false)
				.show();
			return true;
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
			new AlertDialog.Builder(MainActivity.this)
				.setTitle("JsConfirm")
				.setMessage(message)
				.setPositiveButton("OK", (dialog, which) -> result.confirm())
				.setNegativeButton("Cancel", (dialog, which) -> result.cancel())
				.setCancelable(false)
				.show();
			return true;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
			final EditText et = new EditText(MainActivity.this);
			et.setText(defaultValue);
			new AlertDialog.Builder(MainActivity.this)
				.setTitle(message)
				.setView(et)
				.setPositiveButton("OK", (dialog, which) -> result.confirm(et.getText().toString()))
				.setNegativeButton("Cancel", (dialog, which) -> result.cancel())
				.setCancelable(false)
				.show();
			return true;
		}
	};
	private final View.OnClickListener onClickListener = v -> {
		switch (v.getId()) {
			case R.id.fab_1:
				webview.evaluateJavascript("javascript:findAllLanguage()", value -> {
					Log.d(TAG, "received: " + value);
					Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
				});
				break;
			case R.id.fab_2:
				webview.evaluateJavascript("javascript:findLanguage('en')", value -> {
					Log.d(TAG, "received: " + value);
					Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
				});
				break;
			case R.id.fab_3:
				Log.d(TAG, "initEvent: webview reload");
				webview.reload();
				Toast.makeText(MainActivity.this, "reload", Toast.LENGTH_SHORT).show();
				break;
		}
	};


	static {
//		ARCH_URI = "file:///android_asset/web/index.html";
		ARCH_URI = "https://wiki.archlinux.org/";
		JS_SRC_RULE = "var obj = document.createElement(\"script\");"
			+ "obj.type=\"text/javascript\";"
			+ "obj.innerText=\"%s\";"
			+ "document.body.appendChild(obj);";
		ALL_JAVASCRIPT_FUNCTION.add(
			"function findAllLanguageAll(){" +
				"    var allLanguageNodeList = document.querySelectorAll('a.interlanguage-link-target');" +
				"    var allLanguageString = Array.from(allLanguageNodeList).flatMap(ele => ele.lang + '^' + ele.innerText + '^' + ele.href);" +
				"    return allLanguageString;" +
				"};"
		);
		ALL_JAVASCRIPT_FUNCTION.add(
			"function autoHideElement(){" +
				"    document.querySelector('div#p-lang').style.display = 'none';" +
				"    document.querySelector('div#mw-navigation').style.display = 'none';" +
				"    document.querySelector('div#mw-head-base').style.display = 'none';" +
				"    document.querySelector('div#mw-page-base').style.display = 'none';" +
				"    document.querySelector('div#archnavbar').style.display = 'none';" +
				"};"
		);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestPermissions();
		initView();
		initEvent();
		init();
	}

	private void init() {
		Log.d(TAG, "init: ");
		webview.loadUrl(ARCH_URI);
	}

	private void initView() {
		clMain = findViewById(R.id.cl_main);
		webview = findViewById(R.id.webview);
		fabMenu = findViewById(R.id.fab_menu);
		fab1 = findViewById(R.id.fab_1);
		fab2 = findViewById(R.id.fab_2);
		fab3 = findViewById(R.id.fab_3);
		fabMenu.setVisibility(View.GONE);
		progressBar = findViewById(R.id.progressbar);
		languagePopupWindow = new LanguagePopupWindow(MainActivity.this);
		languagePopupWindow.setSelectLanguageModel(selectLanguageModel);
		languagePopupWindow.setOutsideTouchable(true);
		languagePopupWindow.setHandler(handler);
		languagePopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		languagePopupWindow.setHeight(MainActivity.this.getWindowManager().getDefaultDisplay().getHeight() / 3);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initEvent() {
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		WebView.setWebContentsDebuggingEnabled(true);
		webview.setWebViewClient(webViewClient);
		webview.setWebChromeClient(webChromeClient);
		webview.addJavascriptInterface(javaScriptInterface, "javaScriptInterface");

		WebSettings setting = webview.getSettings();
		setting.setUseWideViewPort(true);
		setting.setJavaScriptEnabled(true);
		setting.setUseWideViewPort(true);
		setting.setSupportZoom(true);
		setting.setBuiltInZoomControls(true);
		setting.setDisplayZoomControls(false);
		setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		setting.setLoadWithOverviewMode(true);

		setting.setCacheMode(WebSettings.LOAD_DEFAULT);
		setting.setDatabaseEnabled(false);
		setting.setAppCacheEnabled(false);
		setting.setDomStorageEnabled(false);
		setting.setGeolocationEnabled(true);
		setting.setSaveFormData(false);
		setting.setDomStorageEnabled(false);
		setting.setAllowFileAccess(false);
		setting.setSavePassword(false);
		fab1.setOnClickListener(onClickListener);
		fab2.setOnClickListener(onClickListener);
		fab3.setOnClickListener(onClickListener);
	}

	public void requestPermissions() {
		// checkSelfPermission 判断是否已经申请了此权限
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
			!= PERMISSION_GRANTED) {
			//如果应用之前请求过此权限但用户拒绝了请求，shouldShowRequestPermissionRationale将返回 true。
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.INTERNET)) {
				Toast.makeText(MainActivity.this, "拒绝了该请求", Toast.LENGTH_SHORT).show();
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,}, 1);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.d(TAG, "onCreateOptionsMenu: ");
		menu.addSubMenu(GROUP_ID_LANGUAGE, ITEM_ID_LANGUAGE_PREFERENCE, 0, R.string.language_preference);
		menu.add(GROUP_ID_LANGUAGE, ITEM_ID_REFRESH, 1, R.string.refresh);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Log.d(TAG, "onPrepareOptionsMenu: ");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: " + item.toString());
		switch (item.getItemId()) {
			case ITEM_ID_LANGUAGE_PREFERENCE:
				if (ALL_LANGUAGE.size() > 0 && selectLanguageModel != null) {
					languagePopupWindow.showAtLocation(clMain, Gravity.BOTTOM, 0, 0);
				}
				break;
			case ITEM_ID_REFRESH:
				webview.reload();
				break;
			default:
				Toast.makeText(MainActivity.this, "default", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (webview.canGoBack()) {
				webview.goBackOrForward(isAutoLanguage ? -2 : -1);
			} else {
				finish();
			}
			//不执行父类点击事件
			return true;
		}
		//继续执行父类其他点击事件
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
	                                       int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			for (int i = 0; i < permissions.length; i++) {
				if (grantResults[i] == PERMISSION_GRANTED) {
					Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
