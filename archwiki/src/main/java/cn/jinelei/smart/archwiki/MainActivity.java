package cn.jinelei.smart.archwiki;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.jinelei.smart.archwiki.common.utils.BitmapUtils;
import cn.jinelei.smart.archwiki.common.utils.CommonUtils;
import cn.jinelei.smart.archwiki.common.utils.ModelUtils;
import cn.jinelei.smart.archwiki.common.utils.SharedUtils;
import cn.jinelei.smart.archwiki.models.BookmarkModel;
import cn.jinelei.smart.archwiki.models.LanguageModel;
import cn.jinelei.smart.archwiki.models.NetworkType;
import cn.jinelei.smart.archwiki.receiver.NetworkStateObserver;
import cn.jinelei.smart.archwiki.receiver.NetworkStateReceiver;
import cn.jinelei.smart.archwiki.views.BookmarkDialog;
import cn.jinelei.smart.archwiki.views.LanguagePopupWindow;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.CANCEL_SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.CONFIRM_SELECT_LANGUAGE;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.GOTO_ANOTHER_URL;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.HIDE_LOADING;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.REFRESH_BOOKMAEK;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.SHOW_ERROR;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.SHOW_LOADING;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.TIMEOUT_HIDE_LOADING;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback, NetworkStateObserver {
	private static final String TAG = "MainActivity";
	private static final String ARCH_URI;
	private static final String JS_SRC_RULE;
	private static final List<String> ALL_JAVASCRIPT_FUNCTION;
	private static final long AUTO_HIDE_TIMEOUT = 5000;
	private BookmarkDialog bookmarkDialog;
	private ActionBar supportActionBar;
	private ProgressBar progressBar;
	private WebView webview;
	private ImageView ivBookmark;
	private ImageView ivSearch;
	private CoordinatorLayout clMain;
	private LanguagePopupWindow languagePopupWindow;

	private final Runnable autoHideLoading = new Runnable() {
		@Override
		public void run() {
			handler.sendEmptyMessage(TIMEOUT_HIDE_LOADING);
		}
	};
	private final Handler handler = new Handler(Looper.getMainLooper(), this);
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
			try {
				String url = Optional.ofNullable(request)
					.filter(r -> null != r.getUrl() && !Strings.isNullOrEmpty(r.getUrl().toString()))
					.map(r -> r.getUrl().toString()).orElseThrow(Throwable::new);
				Log.d(TAG, "shouldOverrideUrlLoading: " + url);
				handler.sendEmptyMessage(SHOW_LOADING);
				webview.loadUrl(url);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
				Message obtain = Message.obtain();
				obtain.what = SHOW_ERROR;
				obtain.obj = throwable.getMessage();
				handler.sendMessage(obtain);
			} finally {
				return true;
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			Log.d(TAG, "onPageStarted: " + url);
			if (!Strings.isNullOrEmpty(url) && ivBookmark != null) {
				ivBookmark.setImageResource(bookmarkDialog.containsUrl(url) ? R.drawable.ic_bookmark_full_dark : R.drawable.ic_bookmark_empty);
//				ivBookmark.setEnabled(!bookmarkDialog.containsUrl(url));
			}
			if (ivBookmark != null)
				handler.sendEmptyMessage(SHOW_LOADING);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished: " + url);
			try {
				if (shouldInjectJavaScript()) {
					// step 1: inject all javascript
					view.evaluateJavascript(String.format(JS_SRC_RULE,
						ALL_JAVASCRIPT_FUNCTION.stream().reduce("", (s, s2) -> s + s2)),
						value -> {
							Log.d(TAG, "step 1: inject javascript success");
							// step 2: query all available language
							if (shouldFindAllLanguage()) {
								webview.evaluateJavascript("javascript:findAllLanguageAll()", val -> {
									List<LanguageModel> collect = ModelUtils.convertStringToLanguageModelList(val);
									Log.d(TAG, "step 2: find all language success");
									Log.v(TAG, "findAllLanguageAll: " + collect);
									languagePopupWindow.resetAllLanguage(collect);
								});
							}
							// step 3: should hide some elements
							if (shouldHideElements()) {
								webview.evaluateJavascript("javascript:autoHideElement()", value1 -> Log.d(TAG, "step 3: auto hide elements success"));
							}
							handler.sendEmptyMessage(HIDE_LOADING);
						});
				} else {
					handler.sendEmptyMessage(HIDE_LOADING);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Message obtain = Message.obtain();
				obtain.what = SHOW_ERROR;
				obtain.obj = e.getMessage();
				handler.sendMessage(obtain);
			} finally {
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

	static {
//		ARCH_URI = "file:///android_asset/web/index.html";
		ALL_JAVASCRIPT_FUNCTION = new ArrayList<>();
		ARCH_URI = "https://wiki.archlinux.org/index.php/Main_page";
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

	private NetworkStateReceiver networkStateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestPermissions();
		initNetworkStateReceiver();
		initView();
		initEvent();
		init();
	}

	private void initNetworkStateReceiver() {
		networkStateReceiver = new NetworkStateReceiver();
		networkStateReceiver.registerNetworkStateObserver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkStateReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (networkStateReceiver != null) {
			networkStateReceiver.unregisterNetworkStateObserver(this);
			unregisterReceiver(networkStateReceiver);
		}
	}

	private void init() {
		Log.d(TAG, "init: ");
		webview.loadUrl(ARCH_URI);
	}

	private void initView() {
		clMain = findViewById(R.id.cl_main);
		webview = findViewById(R.id.webview);
		progressBar = findViewById(R.id.progressbar);
		initWebView();
		initActionBar();
		initLanguagePopupWindow();
		initBookmark();
	}

	private void initWebView() {
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		WebView.setWebContentsDebuggingEnabled(true);
		webview.setWebViewClient(webViewClient);
		webview.setWebChromeClient(webChromeClient);

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
	}

	private void initActionBar() {
		supportActionBar = getSupportActionBar();
		View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.action_bar_custom, clMain, false);
		ivBookmark = view.findViewById(R.id.iv_bookmark);
		ivBookmark.setOnClickListener(this);
		ivSearch = view.findViewById(R.id.iv_search);
		ivSearch.setOnClickListener(this);
		supportActionBar.setDisplayShowCustomEnabled(true);
		supportActionBar.setCustomView(view, new ActionBar.LayoutParams(Gravity.RIGHT | Gravity.CENTER_VERTICAL));
	}

	private void initLanguagePopupWindow() {
		languagePopupWindow = new LanguagePopupWindow(MainActivity.this);
		languagePopupWindow.setOutsideTouchable(true);
		languagePopupWindow.setHandler(handler);
		languagePopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		Point windowSize = new Point();
		MainActivity.this.getWindowManager().getDefaultDisplay().getSize(windowSize);
		languagePopupWindow.setHeight(windowSize.y / 5 * 2);
	}

	private void initBookmark() {
		bookmarkDialog = new BookmarkDialog(MainActivity.this);
		bookmarkDialog.addHandler(this.handler);
		Optional.ofNullable(SharedUtils.getParam(MainActivity.this, SharedUtils.DEFAULT_NAME, SharedUtils.TAG_ALL_BOOKMARK, new ArrayList<>()))
			.filter(o -> o instanceof List)
			.map(o -> (ArrayList<Object>) o)
			.ifPresent(list -> list.stream()
				.filter(o -> o instanceof BookmarkModel)
				.map(o -> (BookmarkModel) o)
				.filter(BookmarkModel::checkBaseInfomation)
				.forEach(bookmarkModel -> MainActivity.this.bookmarkDialog.addData(bookmarkModel)));
	}

	private void initEvent() {
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
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,}, 1);
			}
		}
	}

	private boolean shouldInjectJavaScript() {
		Log.d(TAG, "shouldInjectJavaScript: " + true);
		return true;
	}

	private boolean shouldFindAllLanguage() {
		Log.d(TAG, "shouldFindAllLanguage: true");
//		return ALL_LANGUAGE.size() <= 1;
		return true;
	}

	private boolean shouldHideElements() {
		Log.d(TAG, "shouldHideElements: " + true);
		return true;
	}

	private void showBookmark() {
		Log.d(TAG, "click bookmark");
		bookmarkDialog.show();
//		handler.postDelayed(() -> {
//			for (int i = 0; i < 30; i++) {
//				bookmarkDialog.addData(new BookmarkModel("title_" + i, "url_" + i));
//			}
//		}, 300);
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
		getMenuInflater().inflate(R.menu.menu, menu);
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
			case R.id.item_switch_language:
				languagePopupWindow.showAtLocation(clMain, Gravity.BOTTOM, 0, 0);
				break;
			case R.id.item_refresh:
				handler.sendEmptyMessage(SHOW_LOADING);
				webview.reload();
				break;
			case R.id.item_bookmark:
				showBookmark();
				break;
			case R.id.item_about:
				startActivity(new Intent(MainActivity.this, AboutActivity.class));
				break;
			case R.id.item_exit:
				MainActivity.this.finish();
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
			} else {
				if (CommonUtils.isFastClick(MainActivity.this)) {
					finish();
				} else {
					Toast.makeText(MainActivity.this, R.string.more_click_exit, Toast.LENGTH_SHORT).show();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_bookmark: // add bookmark
				try {
					TextView textView = new TextView(MainActivity.this);
					textView.setText(webview.getTitle() + "\n" + webview.getUrl());
					textView.setPadding(50, 20, 50, 20);
					boolean addOrRemoveData = bookmarkDialog.containsUrl(webview.getUrl());
					new AlertDialog.Builder(MainActivity.this)
						.setTitle(addOrRemoveData ? R.string.would_remove_bookmark : R.string.would_add_bookmark)
						.setView(textView)
						.setPositiveButton(R.string.confirm, ((dialog, which) -> {
							BookmarkModel tempModel = new BookmarkModel(webview.getTitle(), webview.getUrl(), BitmapUtils.bitmapToBase64(webview.getFavicon()));
							if (addOrRemoveData) {
								MainActivity.this.bookmarkDialog.removeData(tempModel);
							} else {
								MainActivity.this.bookmarkDialog.addData(tempModel);
							}
							dialog.dismiss();
						}))
						.setNegativeButton(R.string.cancel, ((dialog, which) -> {
							dialog.dismiss();
						})).create().show();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "onOptionsItemSelected: " + e.getMessage());
				}
				break;
			case R.id.iv_search: // show search dialog
				showSearchDialog();
				break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case SHOW_LOADING:
				Log.d(TAG, "handleMessage: SHOW_LOADING");
				webview.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				handler.removeCallbacks(autoHideLoading);
				handler.postDelayed(autoHideLoading, AUTO_HIDE_TIMEOUT);
				break;
			case HIDE_LOADING:
				Log.d(TAG, "handleMessage: HIDE_LOADING");
				handler.removeCallbacks(autoHideLoading);
				webview.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				break;
			case SHOW_ERROR:
				Log.d(TAG, "handleMessage: SHOW_ERROR");
				handler.removeCallbacks(autoHideLoading);
				webview.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				LinearLayout ll = new LinearLayout(MainActivity.this);
				ll.setPadding(40, 20, 40, 20);
				TextView textView = new TextView(MainActivity.this);
				textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				textView.setText(Optional.ofNullable(msg.obj).map(Object::toString).orElse(MainActivity.this.getString(R.string.page_load_error)));
				ll.addView(textView);
				new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.page_load_error)
					.setView(ll)
					.setPositiveButton(R.string.confirm, ((dialog, which) -> dialog.dismiss()))
					.create().show();
				break;
			case TIMEOUT_HIDE_LOADING:
				Log.d(TAG, "handleMessage: TIMEOUT_HIDE_LOADING");
				handler.removeCallbacks(autoHideLoading);
				webview.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				break;
			case CONFIRM_SELECT_LANGUAGE:
				Log.d(TAG, "handleMessage: CONFIRM_SELECT_LANGUAGE");
				webview.loadUrl(((LanguageModel) msg.obj).getHref());
				handler.sendEmptyMessage(SHOW_LOADING);
				break;
			case CANCEL_SELECT_LANGUAGE:
				Log.d(TAG, "handleMessage: CANCEL_SELECT_LANGUAGE");
				break;
			case GOTO_ANOTHER_URL:
				Log.d(TAG, "handleMessage: GOTO_ANOTHER_URL");
				Optional.ofNullable(msg.obj)
					.filter(o -> o instanceof BookmarkModel && !Strings.isNullOrEmpty(((BookmarkModel) o).getUrl()))
					.filter(o -> null != webview)
					.map(o -> ((BookmarkModel) o).getUrl())
					.ifPresent(url -> {
						handler.sendEmptyMessage(SHOW_LOADING);
						webview.loadUrl(url);
					});
				break;
			case REFRESH_BOOKMAEK: // refresh bookmark
				if (ivBookmark != null && webview != null) {
					ivBookmark.setImageResource(bookmarkDialog.containsUrl(webview.getUrl()) ? R.drawable.ic_bookmark_full_dark : R.drawable.ic_bookmark_empty);
//					ivBookmark.setEnabled(!bookmarkDialog.containsUrl(webview.getUrl()));
				}
				break;
		}
		return true;
	}

	@Override
	public void onNetworkChanged(NetworkType networkType) {
		switch (networkType) {
			case CONNECT_WIFI_AND_CELLULAR:
			case CONNECT_WIFI_DISCONNECT_CELLULAR:
				if (webview != null) {
					WebSettings settings = webview.getSettings();
					settings.setCacheMode(WebSettings.LOAD_DEFAULT);
					Log.d(TAG, "onNetworkChanged: set cache: LOAD_DEFAULT");
				}
				break;
			case DISCONNECT_WIFI_CONNECT_CELLULAR:
				if (webview != null) {
					WebSettings settings = webview.getSettings();
					settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
					Log.d(TAG, "onNetworkChanged: set cache: LOAD_CACHE_ELSE_NETWORK");
				}
				break;
			case DISCONNECT_WIFI_AND_CELLULAR:
				if (webview != null) {
					WebSettings settings = webview.getSettings();
					settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					Log.d(TAG, "onNetworkChanged: set cache: LOAD_CACHE_ONLY");
				}
				break;
			default:
				break;
		}
	}
}
