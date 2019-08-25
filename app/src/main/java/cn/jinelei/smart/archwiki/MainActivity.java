package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	//	private static final String ARCH_URI = "https://wiki.archlinux.org/";
	public static final String ARCH_URI = "https://www.jianshu.com/p/39a6f7252b6a?utm_campaign";
	private static final String js =
		"(function(){\n" +
			" document.querySelector('input#searchInput').value = 'asdf'\n" +
			" window.onload = function(){" +
			" document.querySelector('input#searchInput').value = 'asdf'\n" +
			"}\n" +
			"}())";
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
				view.postDelayed(() -> {
					Log.d(TAG, "onPageFinished: start inject js script");
//					String insertJavaScript = "javascript:(function() { "
//						+ "document.body.style.display = none; })();";
					String insertJavaScript = "javascript: alert('asdfasdf')";
					view.loadUrl(insertJavaScript);
				}, 1000);
				fabMenu.setEnabled(true);
				if (fabMenu.isExpanded()) {
					fabMenu.collapseImmediately();
//					view.loadUrl("javascript: alert('asdfasdfasdf')");
				}
			}
		});
		WebSettings webSettings = webview.getSettings();
		webSettings.setUseWideViewPort(true);
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
