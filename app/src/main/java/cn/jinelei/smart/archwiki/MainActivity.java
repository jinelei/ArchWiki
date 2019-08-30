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
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.jinelei.smart.archwiki.constants.ArchConstants;
import cn.jinelei.smart.archwiki.intf.IJavaScriptInterface;
import cn.jinelei.smart.archwiki.views.LanguagePopupWindow;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final String ARCH_URI;
	private static final String JS_SRC_RULE;
	private static final List<String> ALL_JAVASCRIPT_FUNCTION = new ArrayList<>();
	private static final List<String> ALL_LANGUAGE_PREFERENCE = new ArrayList<>();
	private static final List<String> ALL_LANGUAGE_PREFERENCE_LANG = new ArrayList<>();
	public static final int LOAD_ORIGIN_PAGE = 2;
	private static final int GROUP_ID_LANGUAGE = 10;
	private static final int ITEM_ID_LANGUAGE_PREFERENCE = 11;
	private static final int LOAD_ALL_FINISH = 0;
	private CountDownLatch pageLoadStep = new CountDownLatch(LOAD_ORIGIN_PAGE);
	private FloatingActionsMenu fabMenu;
	private FloatingActionButton fab1;
	private FloatingActionButton fab2;
	private FloatingActionButton fab3;
	private ProgressBar progressBar;
	private WebView webview;
	private CoordinatorLayout clMain;
	private LanguagePopupWindow languagePopupWindow;

	private static final int SHOW_LOADING = 1;
	private static final int HIDE_LOADING = 2;

	private String autoLanguage = "zh-Hans";

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
				case ArchConstants.Handler.SELECT_LANGUAGE:
					Log.d(TAG, "handleMessage: SELECT_LANGUAGE: " + msg.obj);
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

	static {
//		ARCH_URI = "file:///android_asset/web/index.html";
		ARCH_URI = "https://wiki.archlinux.org/";
		JS_SRC_RULE = "var obj = document.createElement(\"script\");"
			+ "obj.type=\"text/javascript\";"
			+ "obj.innerText=\"%s\";"
			+ "document.body.appendChild(obj);";
		ALL_JAVASCRIPT_FUNCTION.add(
			"function findLanguage(language) {" +
				"    var nodeList = document.querySelectorAll('a.interlanguage-link-target');" +
				"    for (var i = 0; i < nodeList.length; i++) {" +
				"        if (nodeList[i].lang == language)" +
				"            return true;" +
				"    }" +
				"    return false;" +
				"};");
//		document.querySelectorAll("a.interlanguage-link-target[lang=en]")
		ALL_JAVASCRIPT_FUNCTION.add(
			"function findLanguageHref(language) {" +
				"    var nodeList = document.querySelectorAll('a.interlanguage-link-target');" +
				"    for (var i = 0; i < nodeList.length; i++) {" +
				"        if (nodeList[i].lang == language)" +
				"            return nodeList[i].href;" +
				"    }" +
				"    return 'javascript:void(0)';" +
				"};");
		ALL_JAVASCRIPT_FUNCTION.add(
			"function findAllLanguage(){" +
				"    var allLanguageNodeList = document.querySelectorAll('a.interlanguage-link-target');" +
				"    var allLanguageString = Array.from(allLanguageNodeList).flatMap(ele => ele.innerText);" +
				"    return allLanguageString;" +
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
		progressBar = findViewById(R.id.progressbar);
		languagePopupWindow = new LanguagePopupWindow(MainActivity.this);
		languagePopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		languagePopupWindow.setHeight(MainActivity.this.getWindowManager().getDefaultDisplay().getHeight() / 3);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initEvent() {
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		WebView.setWebContentsDebuggingEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl().toString());
				try {
					view.loadUrl(request.getUrl().toString());
					pageLoadStep = new CountDownLatch(LOAD_ORIGIN_PAGE);
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
				handler.sendEmptyMessage(SHOW_LOADING);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d(TAG, "onPageFinished: pageLoadStep: " + pageLoadStep.getCount() + "  " + url);
				view.evaluateJavascript(String.format(JS_SRC_RULE,
					ALL_JAVASCRIPT_FUNCTION.stream().reduce("", (s, s2) -> s + s2)),
					value -> {
						Log.d(TAG, "inject js script result: " + value);
						webview.evaluateJavascript("javascript:findAllLanguage()", val -> {
							List<String> collect = Stream.of(val)
								.filter(s -> !s.equals("[]") && s.matches("\\[.*\\]"))
								.map(s -> s.substring(1, s.length() - 1))
								.flatMap(s -> Stream.of(s.split(",")))
								.filter(s -> !s.equals("\"\""))
								.map(s -> s.substring(1, s.length() - 1))
								.collect(Collectors.toList());
							Log.d(TAG, "findAllLanguage: " + collect);
							ALL_LANGUAGE_PREFERENCE_LANG.clear();
							ALL_LANGUAGE_PREFERENCE_LANG.addAll(collect);
							languagePopupWindow.resetAllLanguage(collect);
						});
						if (null != autoLanguage && !"".equals(autoLanguage)) {
							pageLoadStep.countDown();
							view.evaluateJavascript("javascript:findLanguageHref('" + autoLanguage + "');",
								value1 -> {
									if (null != value1 && !"".equals(value1) && !"\"\"".equals(value1)) {
										Log.d(TAG, "findLanguageHref: " + value1);
										webview.loadUrl(value1.substring(1, value1.length() - 1));
									}
								});
						}
					});
				webview.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(HIDE_LOADING);
				super.onPageFinished(view, url);
			}
		});
		webview.setWebChromeClient(new WebChromeClient() {
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
		});
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
		fab1.setOnClickListener(v -> webview.evaluateJavascript("javascript:findAllLanguage()", value -> {
			Log.d(TAG, "received: " + value);
			Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
		}));
		fab2.setOnClickListener(v -> webview.evaluateJavascript("javascript:findLanguage('en')", value -> {
			Log.d(TAG, "received: " + value);
			Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
		}));
		fab3.setOnClickListener(v -> {
			Log.d(TAG, "initEvent: webview reload");
			pageLoadStep = new CountDownLatch(LOAD_ORIGIN_PAGE);
			webview.reload();
			Toast.makeText(MainActivity.this, "reload", Toast.LENGTH_SHORT).show();
		});
	}

	private String generateAllJsScript() {
		return ALL_JAVASCRIPT_FUNCTION.stream().reduce("", (s, s2) -> s + s2);
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
//		getMenuInflater().inflate(R.menu.menu, menu);
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
//				LinearLayout llLanguagePreferences = new LinearLayout(MainActivity.this);
//				llLanguagePreferences.setOrientation(LinearLayout.VERTICAL);
//				for (String lang : ALL_LANGUAGE_PREFERENCE_LANG) {
//					TextView textview = new TextView(MainActivity.this);
//					textview.setText(lang);
//					llLanguagePreferences.addView(textview);
//				}
//				new AlertDialog.Builder(MainActivity.this)
//					.setView(llLanguagePreferences)
//					.create().show();
				languagePopupWindow.showAtLocation(clMain, Gravity.BOTTOM, 0, 0);
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
				webview.goBack();
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
