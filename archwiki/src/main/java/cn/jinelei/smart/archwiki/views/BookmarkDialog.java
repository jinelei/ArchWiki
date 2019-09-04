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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cn.jinelei.smart.archwiki.R;
import cn.jinelei.smart.archwiki.common.anim.ScaleInAnimation;
import cn.jinelei.smart.archwiki.common.anim.ScaleOutAnimation;
import cn.jinelei.smart.archwiki.common.utils.BitmapUtils;
import cn.jinelei.smart.archwiki.common.utils.SharedUtils;
import cn.jinelei.smart.archwiki.models.BookmarkModel;

import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.GOTO_ANOTHER_URL;
import static cn.jinelei.smart.archwiki.common.constants.CommonConstants.Handler.REFRESH_BOOKMAEK;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_bookmark_list);
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
		rvBookmark.setItemAnimator(new DefaultItemAnimator());
	}

	private void initEvent() {
		findViewById(R.id.tv_confirm).setOnClickListener(v -> BookmarkDialog.this.dismiss());
	}

	private void refreshUI() {
		if (models.isEmpty()) {
			if (tvNothing != null)
				tvNothing.setVisibility(View.VISIBLE);
			if (rvBookmark != null)
				rvBookmark.setVisibility(View.GONE);
		} else {
			if (tvNothing != null)
				tvNothing.setVisibility(View.GONE);
			if (rvBookmark != null)
				rvBookmark.setVisibility(View.VISIBLE);
		}
		if (handler != null) {
			Message message = handler.obtainMessage();
			message.what = REFRESH_BOOKMAEK;
			message.obj = models;
			handler.sendMessage(message);
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
			if (null != bookmarkAdapter)
				bookmarkAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "clearAllData");
		} finally {
			writeToShared();
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
				if (null != bookmarkAdapter)
					bookmarkAdapter.notifyDataSetChanged();
				Toast.makeText(this.context, this.context.getString(R.string.add_bookmark_success), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "addData: " + bookmarkModel);
			Toast.makeText(this.context, this.context.getString(R.string.add_bookmark_failure), Toast.LENGTH_SHORT).show();
		} finally {
			writeToShared();
			refreshUI();
		}
	}

	public boolean removeData(BookmarkModel param) {
		Log.d(TAG, "removeData: " + (param != null ? param : "null"));
		boolean removeResult = false;
		try {
			removeResult = models.removeIf(model -> null != model && null != param
				&& !Strings.isNullOrEmpty(model.getTitle())
				&& !Strings.isNullOrEmpty(model.getUrl())
				&& model.getUrl().equals(param.getUrl())
				&& model.getTitle().equals(param.getTitle()));
			if (null != bookmarkAdapter)
				bookmarkAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "removeData: " + param);
		} finally {
			Toast.makeText(this.context, this.context.getString(removeResult ? R.string.remove_bookmark_success : R.string.remove_bookmark_failure), Toast.LENGTH_SHORT).show();
			refreshUI();
			writeToShared();
			return removeResult;
		}
	}

	public boolean containsUrl(String param) {
		return models.stream().anyMatch(iter -> null != param && null != iter && iter.getUrl().equals(param));
	}

	public boolean containsData(BookmarkModel param) {
		return models.stream().anyMatch(iter -> null != param && null != iter && iter.getTitle().equals(param.getTitle()) && iter.getUrl().equals(param.getTitle()));
	}

	public void writeToShared() {
		if (models != null)
			SharedUtils.setParam(BookmarkDialog.this.context, SharedUtils.DEFAULT_NAME, SharedUtils.TAG_ALL_BOOKMARK, models);
	}

	public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkViewHolder> {
		private ScaleInAnimation scaleInAnimation = new ScaleInAnimation();
		private ScaleOutAnimation scaleOutAnimation = new ScaleOutAnimation();

		@Override
		public void onViewAttachedToWindow(@NonNull BookmarkViewHolder holder) {
			super.onViewAttachedToWindow(holder);
			Arrays.stream(scaleInAnimation.getAnimators(holder.itemView))
				.forEach(animator -> {
					animator.setDuration(200).start();
					animator.setInterpolator(new LinearInterpolator());
				});
		}

		@Override
		public void onViewDetachedFromWindow(@NonNull BookmarkViewHolder holder) {
			super.onViewDetachedFromWindow(holder);
			Arrays.stream(scaleOutAnimation.getAnimators(holder.itemView))
				.forEach(animator -> {
					animator.setDuration(200).start();
					animator.setInterpolator(new LinearInterpolator());
				});
		}

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
				String icon = temp.getIcon();
				if (!Strings.isNullOrEmpty(icon) && !"".equals(icon) && icon.startsWith("data:")) {
					holder.ivIcon.setImageBitmap(BitmapUtils.base64ToBitmap(icon));
				}else{
					holder.ivIcon.setVisibility(View.GONE);
				}
				holder.container.setOnClickListener(v -> Optional.ofNullable(BookmarkDialog.this.handler)
					.ifPresent(handler1 -> {
						Message msg = Message.obtain(handler1);
						msg.what = GOTO_ANOTHER_URL;
						msg.obj = temp;
						handler1.sendMessage(msg);
						BookmarkDialog.this.dismiss();
						Toast.makeText(BookmarkDialog.this.context, BookmarkDialog.this.context.getString(R.string.loading_bookmark), Toast.LENGTH_SHORT).show();
					}));
				holder.container.setOnLongClickListener(v -> BookmarkDialog.this.removeData(temp));
			}
		}

		@Override
		public int getItemCount() {
			return models.size();
		}
	}

	public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
		private ConstraintLayout container;
		private TextView tvTitle;
		private TextView tvUrl;
		private ImageView ivIcon;

		public BookmarkViewHolder(@NonNull View itemView) {
			super(itemView);
			container = itemView.findViewById(R.id.vh_bookmark);
			tvTitle = itemView.findViewById(R.id.vh_bookmark_tv_title);
			tvUrl = itemView.findViewById(R.id.vh_bookmark_tv_url);
			ivIcon = itemView.findViewById(R.id.vh_bookmark_iv_icon);
		}
	}

}
