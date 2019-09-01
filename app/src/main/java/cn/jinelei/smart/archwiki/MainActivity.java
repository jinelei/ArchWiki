package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import cn.jinelei.smart.archwiki.intf.IJavaScriptInterface;
import cn.jinelei.smart.archwiki.utils.CommonUtils;
import cn.jinelei.smart.archwiki.views.LanguagePopupWindow;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.CANCEL_SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.CONFIRM_SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.HIDE_LOADING;
import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.SHOW_LOADING;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final String ARCH_URI;
	private static final String JS_SRC_RULE;
	private static final List<String> ALL_JAVASCRIPT_FUNCTION;
	private static final List<LanguagePopupWindow.LanguageModel> ALL_LANGUAGE;
	private static final int GROUP_ID_LANGUAGE = 10;
	private static final int ITEM_ID_LANGUAGE_PREFERENCE = 11;
	private static final int ITEM_ID_REFRESH = 12;
	private static final long AUTO_HIDE_TIMEOUT = 5000;
	private static LanguagePopupWindow.LanguageModel selectLanguageModel;
	private CountDownLatch needBackKeyDown = new CountDownLatch(2);
	private ActionBar supportActionBar;
	private FloatingActionsMenu fabMenu;
	private FloatingActionButton fab1;
	private FloatingActionButton fab2;
	private FloatingActionButton fab3;
	private ProgressBar progressBar;
	private WebView webview;
	private CoordinatorLayout clMain;
	private LanguagePopupWindow languagePopupWindow;
	private boolean autoSetLanguage = false;
	private boolean backAction = false;
	
	private final Runnable autoHideLoading = new Runnable() {
		@Override
		public void run() {
			handler.sendEmptyMessage(HIDE_LOADING);
		}
	};
	private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_LOADING:
					Log.d(TAG, "handleMessage: SHOW_LOADING");
					webview.setVisibility(View.GONE);
					progressBar.setVisibility(View.VISIBLE);
					fabMenu.setEnabled(false);
					if (fabMenu.isExpanded()) {
						fabMenu.collapseImmediately();
					}
					handler.postDelayed(autoHideLoading, AUTO_HIDE_TIMEOUT);
					break;
				case HIDE_LOADING:
					Log.d(TAG, "handleMessage: HIDE_LOADING");
					handler.removeCallbacks(autoHideLoading);
					webview.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					fabMenu.setEnabled(true);
					if (fabMenu.isExpanded()) {
						fabMenu.collapseImmediately();
					}
					break;
				case CONFIRM_SELECT_LANGUAGE:
					Log.d(TAG, "handleMessage: CONFIRM_SELECT_LANGUAGE");
					selectLanguageModel = (LanguagePopupWindow.LanguageModel) msg.obj;
					webview.reload();
					break;
				case CANCEL_SELECT_LANGUAGE:
					Log.d(TAG, "handleMessage: CANCEL_SELECT_LANGUAGE");
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
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);
			Log.e(TAG, "onReceivedError: " + error.getErrorCode() + ": " + error.getDescription());
			TextView textView = new TextView(MainActivity.this);
			textView.setText(error.getDescription());
			textView.setPadding(40, 10, 40, 10);
			new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.page_load_error)
				.setView(textView)
				.setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
				.setNegativeButton(R.string.retry, ((dialog, which) -> {
					webview.reload();
					dialog.dismiss();
				}))
				.create().show();
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl().toString());
			try {
				resetLoadJavaScriptCondition();
				handler.sendEmptyMessage(SHOW_LOADING);
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
			Log.d(TAG, "onPageStarted: " + url);
			handler.sendEmptyMessage(SHOW_LOADING);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished: " + url);
			if (shouldInjectJavaScript()) {
				view.evaluateJavascript(String.format(JS_SRC_RULE,
					ALL_JAVASCRIPT_FUNCTION.stream().reduce("", (s, s2) -> s + s2)),
					value -> {
						if (shouldFindAllLanguage()) {
							webview.evaluateJavascript("javascript:findAllLanguageAll()", val -> {
								List<LanguagePopupWindow.LanguageModel> collect = LanguagePopupWindow.Utils.convertStringToLanguageModelList(val);
								Log.d(TAG, "findAllLanguage: " + collect);
								ALL_LANGUAGE.clear();
								ALL_LANGUAGE.addAll(collect);
								languagePopupWindow.resetAllLanguage(collect);
								if (shouldAutoSetLanguage()) {
									autoSetLanguage = true;
									Log.d(TAG, "auto set language: load url: " + selectLanguageModel.getHref());
									// 这里会重新加载网页
									webview.loadUrl(selectLanguageModel.getHref());
								}
							});
						}
						if (shouldHideElements()) {
							webview.evaluateJavascript("javascript:autoHideElement()", null);
						}
					});
			}
			if (!shouldAutoSetLanguage()) {
				webview.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(HIDE_LOADING);
				resetLoadJavaScriptCondition();
				super.onPageFinished(view, url);
			}
		}
	};
	private final WebChromeClient webChromeClient = new WebChromeClient() {
		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			super.onReceivedIcon(view, icon);
			Optional.ofNullable(supportActionBar)
				.filter(s -> null != icon)
				.ifPresent(actionBar -> actionBar.setIcon(new BitmapDrawable(null, icon)));
		}
		
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			Optional.ofNullable(supportActionBar)
				.filter(s -> null != title && !title.equals(""))
				.ifPresent(actionBar -> actionBar.setSubtitle(title));
		}
		
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
		ALL_JAVASCRIPT_FUNCTION = new ArrayList<>();
		ALL_LANGUAGE = new ArrayList<>();
		ARCH_URI = "https://wiki.archlinux.org/";
		selectLanguageModel = new LanguagePopupWindow.LanguageModel("zh-Hans", "简体中文", "https://wiki.archlinux.org/index.php/Main_page_(%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87)");
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
			"function autoHideElement() {" +
				"    var eles = ['div#p-lang', 'div#mw-navigation'," +
				"        'div#mw-head-base', 'div#mw-page-base'," +
				"        'div#archnavbar', 'div#footer'];" +
				"    for (var i in eles) {" +
				"        var temp = document.querySelector(eles[i]);" +
				"        if (!!temp) {" +
				"            temp.style.display = 'none';" +
				"        }" +
				"    }" +
				"};"
		);
		ALL_JAVASCRIPT_FUNCTION.add(
			"function searchKey(key){" +
				"    document.querySelector('input#searchInput').value = key;" +
				"    document.querySelector('input#searchButton').click();" +
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
		initActionBar();
		initLanguagePopupWindow();
	}
	
	private void initActionBar() {
		supportActionBar = getSupportActionBar();
		LinearLayout linearLayout = new LinearLayout(MainActivity.this);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		ImageView ivBookmark = new ImageView(MainActivity.this);
		ivBookmark.setImageResource(R.drawable.ic_bookmark);
		ivBookmark.setOnClickListener(v -> showBookmark());
		linearLayout.addView(ivBookmark, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		ImageView ivSearch = new ImageView(MainActivity.this);
		ivSearch.setImageResource(R.drawable.ic_search);
		ivSearch.setOnClickListener(v -> showSearchDialog());
		linearLayout.addView(ivSearch, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		supportActionBar.setDisplayShowCustomEnabled(true);
		supportActionBar.setCustomView(linearLayout, new ActionBar.LayoutParams(Gravity.RIGHT | Gravity.CENTER_VERTICAL));
	}
	
	private void initLanguagePopupWindow() {
		languagePopupWindow = new LanguagePopupWindow(MainActivity.this);
		languagePopupWindow.setSelectLanguageModel(selectLanguageModel);
		languagePopupWindow.setOutsideTouchable(true);
		languagePopupWindow.setHandler(handler);
		languagePopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		Point windowSize = new Point();
		MainActivity.this.getWindowManager().getDefaultDisplay().getSize(windowSize);
		languagePopupWindow.setHeight(windowSize.y / 5 * 2);
	}
	
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
		fab1.setOnClickListener(onClickListener);
		fab2.setOnClickListener(onClickListener);
		fab3.setOnClickListener(onClickListener);
	}
	
	private void requestPermissions() {
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
	
	private boolean shouldInjectJavaScript() {
		Log.d(TAG, "shouldInjectJavaScript: " + true);
		return true;
	}
	
	private boolean shouldAutoSetLanguage() {
		boolean result = !autoSetLanguage
			&& !backAction
			&& null != selectLanguageModel
			&& !Strings.isNullOrEmpty(selectLanguageModel.getDetailLang())
			&& !Strings.isNullOrEmpty(selectLanguageModel.getSummaryLang())
			&& !Strings.isNullOrEmpty(selectLanguageModel.getHref())
			&& LanguagePopupWindow.Utils.contains(ALL_LANGUAGE, selectLanguageModel); // 如果包含说明可以自动选择语言
		Log.d(TAG, "shouldAutoSetLanguage: " + result);
		return result;
	}
	
	private boolean shouldFindAllLanguage() {
		Log.d(TAG, "shouldFindAllLanguage: " + true);
		return true;
	}
	
	private boolean shouldHideElements() {
		Log.d(TAG, "shouldHideElements: " + true);
		return true;
	}
	
	private void resetLoadJavaScriptCondition() {
		Log.d(TAG, "resetLoadJavaScriptCondition");
		backAction = false;
		autoSetLanguage = false;
	}
	
	private void showBookmark() {
		Log.d(TAG, "click bookmark");
		new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.bookmark)
			.setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
			.create().show();
	}
	
	private void showSearchDialog() {
		Log.d(TAG, "showSearchDialog");
		LinearLayout ll = new LinearLayout(MainActivity.this);
		ll.setPadding(40, 20, 40, 20);
		EditText editText = new EditText(MainActivity.this);
		editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.addView(editText);
		new AlertDialog.Builder(MainActivity.this)
			.setTitle(R.string.search)
			.setView(ll)
			.setPositiveButton(R.string.search, (dialog, which) -> {
				if (!Strings.isNullOrEmpty(editText.getText().toString())) {
					webview.evaluateJavascript("javascript:searchKey('" + editText.getText() + "')",
						value -> Log.d(TAG, "searchKey: " + editText.getText()));
				}
			})
			.create().show();
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
				handler.sendEmptyMessage(SHOW_LOADING);
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
			Log.d(TAG, "canGoBackOrForward: " + webview.canGoBack());
			if (webview.canGoBack()) {
				// 如果有自动设置语言就回退两次
				handler.sendEmptyMessage(SHOW_LOADING);
				webview.goBack();
				backAction = true;
			} else {
				if (CommonUtils.isFastClick(MainActivity.this)) {
					needBackKeyDown.countDown();
					if (needBackKeyDown.getCount() == 1) {
						Toast.makeText(MainActivity.this, R.string.more_click_exit, Toast.LENGTH_SHORT).show();
					} else {
						finish();
					}
				} else {
					needBackKeyDown = new CountDownLatch(2);
				}
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
