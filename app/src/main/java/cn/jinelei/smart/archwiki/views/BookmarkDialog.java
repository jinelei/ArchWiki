package cn.jinelei.smart.archwiki.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.jinelei.smart.archwiki.R;

import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.GOTO_ANOTHER_URL;

public class BookmarkDialog extends Dialog {
	private static final String TAG = "BookmarkDialog";
	private final List<BookmarkModel> models = new ArrayList<>();
	private Context context;
	private boolean isCanceledOnTouchOutside = true;
	private boolean isCancelable = true;
	private RecyclerView rvBookmark;
	private RecyclerView.Adapter bookmarkAdapter;
	private Handler handler = null;
	private TextView tvNothing;

	public BookmarkDialog(@NonNull Context context) {
		this(context, 0);
	}

	public BookmarkDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
		this.context = context;
		initBookmarkDialog();
	}

	protected BookmarkDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		this.context = context;
		initBookmarkDialog();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bookmark_list);
		initBookmarkDialog();
		initView();
		initEvent();
		refreshUI();
	}

	private void initBookmarkDialog() {
		setCanceledOnTouchOutside(isCanceledOnTouchOutside);
		setCancelable(isCancelable);
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		int width = context.getResources().getDisplayMetrics().widthPixels;
		int height = context.getResources().getDisplayMetrics().heightPixels;
		int dialogWidth = (int) (width * 0.8f);
		int dialogHeight = (int) (height * 0.7f);
		lp.width = dialogWidth;
//		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = dialogHeight;
		window.setAttributes(lp);
		window.setGravity(Gravity.CENTER);
		window.setWindowAnimations(R.style.dialogAnimStyle);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	private void initView() {
		rvBookmark = findViewById(R.id.rv_bookmark);
		tvNothing = findViewById(R.id.tv_nothing);
		rvBookmark.setLayoutManager(new LinearLayoutManager(this.context));
		bookmarkAdapter = new BookmarkAdapter();
		rvBookmark.setAdapter(bookmarkAdapter);
	}

	private void initEvent() {
		findViewById(R.id.tv_confirm).setOnClickListener(v -> BookmarkDialog.this.dismiss());
	}

	private void refreshUI() {
		if (models.isEmpty()) {
			tvNothing.setVisibility(View.VISIBLE);
			rvBookmark.setVisibility(View.GONE);
		} else {
			tvNothing.setVisibility(View.GONE);
			rvBookmark.setVisibility(View.VISIBLE);
		}
	}

	public void updateCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
		this.isCanceledOnTouchOutside = canceledOnTouchOutside;
		this.setCanceledOnTouchOutside(canceledOnTouchOutside);
	}

	public void updateSetCancelable(boolean setCancelable) {
		this.isCancelable = setCancelable;
		this.setCancelable(setCancelable);
	}

	public boolean isCanceledOnTouchOutside() {
		return isCanceledOnTouchOutside;
	}

	public boolean isSetCancelable() {
		return isCancelable;
	}

	public void addHandler(Handler handler) {
		if (handler != null)
			this.handler = handler;
	}

	public void clearAllData() {
		try {
			Log.d(TAG, "clearAllData");
			models.clear();
			bookmarkAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "clearAllData");
		} finally {
			refreshUI();
		}
	}

	public void addData(BookmarkModel bookmarkModel) {
		try {
			Log.d(TAG, "addData: " + bookmarkModel);
			if (null == bookmarkModel || Strings.isNullOrEmpty(bookmarkModel.getUrl()) || Strings.isNullOrEmpty(bookmarkModel.getTitle())) {
				Toast.makeText(this.context, this.context.getString(R.string.add_bookmark_failure), Toast.LENGTH_SHORT).show();
				return;
			}
			boolean existData = models.stream().anyMatch(model1 -> null != model1
				&& bookmarkModel.getTitle().equals(model1.getTitle())
				&& bookmarkModel.getUrl().equals(model1.getUrl())
			);
			if (existData) {
				Toast.makeText(this.context, this.context.getString(R.string.already_exist_same_bookmark), Toast.LENGTH_SHORT).show();
			} else {
				models.add(bookmarkModel);
				bookmarkAdapter.notifyDataSetChanged();
				Toast.makeText(this.context, this.context.getString(R.string.add_bookmark_success), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "addData: " + bookmarkModel);
			Toast.makeText(this.context, this.context.getString(R.string.add_bookmark_failure), Toast.LENGTH_SHORT).show();
		} finally {
			refreshUI();
		}
	}

	public boolean removeData(BookmarkModel bookmarkModel) {
		Log.d(TAG, "removeData: " + (bookmarkModel != null ? bookmarkModel : "null"));
		boolean removeResult = false;
		if (null == bookmarkModel || Strings.isNullOrEmpty(bookmarkModel.getUrl()) || Strings.isNullOrEmpty(bookmarkModel.getTitle()))
			return false;
		try {
			removeResult = models.remove(bookmarkModel);
			bookmarkAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "removeData: " + bookmarkModel);
		} finally {
			Toast.makeText(this.context, this.context.getString(removeResult ? R.string.remove_bookmark_success : R.string.remove_bookmark_failure), Toast.LENGTH_SHORT).show();
			refreshUI();
			return removeResult;
		}
	}

	public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkViewHolder> {

		@NonNull
		@Override
		public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_bookmark, parent, false);
			return new BookmarkViewHolder(rootView);
		}

		@Override
		public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
			BookmarkModel temp = models.get(position);
			if (null != temp && !Strings.isNullOrEmpty(temp.getTitle()) && !Strings.isNullOrEmpty(temp.getUrl())) {
				holder.tvTitle.setText(temp.getTitle());
				holder.tvUrl.setText(temp.getUrl());
				holder.llContainer.setOnClickListener(v -> Optional.ofNullable(BookmarkDialog.this.handler).ifPresent(handler1 -> {
					Message msg = Message.obtain(handler1);
					msg.what = GOTO_ANOTHER_URL;
					msg.obj = temp;
					handler1.sendMessage(msg);
					BookmarkDialog.this.dismiss();
					Toast.makeText(BookmarkDialog.this.context, BookmarkDialog.this.context.getString(R.string.loading_bookmark), Toast.LENGTH_SHORT).show();
				}));
				holder.llContainer.setOnLongClickListener(v -> BookmarkDialog.this.removeData(temp));
			}
		}

		@Override
		public int getItemCount() {
			return models.size();
		}
	}

	public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
		private LinearLayout llContainer;
		private TextView tvTitle;
		private TextView tvUrl;

		public BookmarkViewHolder(@NonNull View itemView) {
			super(itemView);
			llContainer = itemView.findViewById(R.id.vh_bookmark);
			tvTitle = itemView.findViewById(R.id.vh_bookmark_tv_title);
			tvUrl = itemView.findViewById(R.id.vh_bookmark_tv_url);
		}
	}

	public static class BookmarkModel implements Cloneable {
		private String title;
		private String url;

		public BookmarkModel(String title, String url) {
			this.title = title;
			this.url = url;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		protected BookmarkModel clone() {
			return new BookmarkModel(title, url);
		}

		@Override
		public String toString() {
			return "BookmarkModel{" +
				"title='" + title + '\'' +
				", url='" + url + '\'' +
				'}';
		}
	}
}
