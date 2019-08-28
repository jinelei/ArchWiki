package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.concurrent.CountDownLatch;

import cn.jinelei.smart.archwiki.intf.IJavaScriptInterface;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	//	private static final String ARCH_URI = "file:///android_asset/web/index.html";
	private static final String ARCH_URI = "https://wiki.archlinux.org/";
//	private static final String JS_SRC_RULE = "var obj = document.createElement(\"script\");"
//		+ "obj.type=\"text/javascript\";"
//		+ "obj.src=\"%s\";"
//		+ "document.body.appendChild(obj);";
	private static final String JS_SRC_RULE = "var obj = document.createElement(\"script\");"
		+ "obj.type=\"text/javascript\";"
		+ "obj.innerText=\"%s\";"
		+ "document.body.appendChild(obj);";
	private CountDownLatch loadJavaScript = new CountDownLatch(1);
	private FloatingActionsMenu fabMenu;
	private FloatingActionButton fab1;
	private FloatingActionButton fab2;
	private FloatingActionButton fab3;
	private ProgressBar progressBar;
	private WebView webview;

	private static final int SHOW_LOADING = 1;
	private static final int HIDE_LOADING = 2;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
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
			}
		}
	};

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initEvent();
		init();
	}

	private void init() {
		Log.d(TAG, "init: ");
		webview.loadUrl(ARCH_URI);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initEvent() {
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setWebContentsDebuggingEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl().toString());
				try {
					view.loadUrl(request.getUrl().toString());
					loadJavaScript = new CountDownLatch(1);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				Log.d(TAG, "onPageStarted: " + url);
				handler.sendEmptyMessage(SHOW_LOADING);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (loadJavaScript.getCount() > 0) {
					Log.d(TAG, "onPageFinished: inject js script");
					loadJavaScript.countDown();
					view.evaluateJavascript(String.format(JS_SRC_RULE, "function callJS(){ alert('call from js')};"),
						value -> Log.d(TAG, "onPageFinished: evaluateJavascript: " + value));
				}
				super.onPageFinished(view, url);
				Log.d(TAG, "onPageFinished: " + url);
				handler.sendEmptyMessage(HIDE_LOADING);
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
		fab1.setOnClickListener(v -> webview.evaluateJavascript(showAlert("from java"), value -> Log.d(TAG, "received: " + value)));
//		fab2.setOnClickListener(v -> webview.evaluateJavascript(appendToContent("from java"), value -> Log.d(TAG, "received: " + value)));
		fab2.setOnClickListener(v -> webview.evaluateJavascript(callJS(), value -> Log.d(TAG, "received: " + value)));
		fab3.setOnClickListener(v -> {
			Log.d(TAG, "initEvent: webview reload");
			webview.reload();
		});
	}

	private void initView() {
		webview = findViewById(R.id.webview);
		fabMenu = findViewById(R.id.fab_menu);
		fab1 = findViewById(R.id.fab_1);
		fab2 = findViewById(R.id.fab_2);
		fab3 = findViewById(R.id.fab_3);
		progressBar = findViewById(R.id.progressbar);
	}

	public void requestPower() {
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

	public String injectJs() {
//		return "javascript:" +
//			String.format(JS_SRC_RULE, "document.body.style.display = 'none'");
		return "javascript:window.ANDROID_CLIENT.showSource("
			+ "document.body.innerHTML);";
//			"document.getElementById('logo').style.display = 'none';" +
//			"document.body.appendChild(document.createElement(\"script\"));";
//		"document.querySelector('input#searchInput').value = 'asdf'" +
	}

	public static String detectAllLanguage() {
		return "document.querySelectorAll(\"a.interlanguage-link-target\")";
	}

	public static String hideBody() {
		return "document.getElementById('firstHeading').style.display = 'none';";
	}

	public static String callJS() {
		return "javascript:callJS()";
	}

	public static String showAlert(String something) {
		return "javascript:alert('" + something + "')";
	}

	public static String appendToContent(String content) {
		return "javascript:appendContent('" + content + "')";
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
