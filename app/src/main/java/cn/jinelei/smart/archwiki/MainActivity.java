package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.concurrent.CountDownLatch;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final String ARCH_URI = "https://wiki.archlinux.org/";
	//	private static final String ARCH_URI = "https://m.baidu.com";
	private static final String JS_SRC_RULE = "var obj = document.createElement(\"script\");"
		+ "obj.type=\"text/javascript\";"
		+ "obj.src=\"%s\";"
		+ "document.body.appendChild(obj);";
	private CountDownLatch loadJavaScript = new CountDownLatch(1);
	private FloatingActionsMenu fabMenu;
	private FloatingActionButton fab1;
	private FloatingActionButton fab2;
	private FloatingActionButton fab3;
	private WebView webview;
	
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
	
	private void initEvent() {
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				Log.d(TAG, "shouldOverrideUrlLoading: ");
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
				super.onPageStarted(view, url, favicon);
				Log.d(TAG, "onPageStarted: ");
				fabMenu.setEnabled(false);
				if (fabMenu.isExpanded()) {
					fabMenu.collapseImmediately();
				}
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				Log.d(TAG, "onPageFinished: ");
				if (loadJavaScript.getCount() > 0L) {
					view.postDelayed(() -> {
						loadJavaScript.countDown();
						Log.d(TAG, "onPageFinished: start inject js script");
						view.loadUrl(injectJs());
					}, 1000);
				} else {
					loadJavaScript = new CountDownLatch(1);
				}
				fabMenu.setEnabled(true);
				if (fabMenu.isExpanded()) {
					fabMenu.collapseImmediately();
				}
			}
		});
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
		//安全性相关,默认先屏蔽
		setting.setSaveFormData(false);
		setting.setDomStorageEnabled(false);
		setting.setAllowFileAccess(false);
		setting.setSavePassword(false);
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
		return "javascript:" +
			String.format(JS_SRC_RULE, "document.body.style.display = 'none'");
//			"document.getElementById('logo').style.display = 'none';" +
//			"document.body.appendChild(document.createElement(\"script\"));";
//		"document.querySelector('input#searchInput').value = 'asdf'" +
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
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
