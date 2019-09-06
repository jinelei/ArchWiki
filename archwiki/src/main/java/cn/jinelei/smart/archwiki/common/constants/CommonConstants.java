package cn.jinelei.smart.archwiki.common.constants;

public class CommonConstants {
	public static class Handler {
		public static final int SHOW_LOADING = 10001;
		public static final int HIDE_LOADING = 10002;
		public static final int TIMEOUT_HIDE_LOADING = 10003;
		public static final int SHOW_ERROR = 10004;
		public static final int CONFIRM_SELECT_LANGUAGE = 10012;
		public static final int CANCEL_SELECT_LANGUAGE = 10013;
		public static final int GOTO_ANOTHER_URL = 10014;
		public static final int REFRESH_BOOKMAEK = 10015;
	}

	public static class Javascript {
		public static final String JS_SRC_RULE = "var obj = document.createElement(\"script\");"
			+ "obj.type=\"text/javascript\";"
			+ "obj.innerText=\"%s\";"
			+ "document.body.appendChild(obj);";
		public static final String INVOKE_findAllLanguage = "javascript:findAllLanguage()";
		public static String INVOKE_autoHideElements = "javascript:autoHideElement()";
		public static final String INVOKE_searchKey = "javascript:searchKey('%s')";
		public static final String INVOKE_findAllRelatedArticles = "javascript:findAllRelatedArticles()";
	}
}
