package cn.jinelei.smart.archwiki.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.jinelei.smart.archwiki.R;
import cn.jinelei.smart.archwiki.common.constants.CommonConstants;
import cn.jinelei.smart.archwiki.models.BookmarkModel;

public class RelatedArticlesPopupWindow extends PopupWindow {
	private static final String TAG = "RelatedArticlesPopupWin";
	private final List<BookmarkModel> allRelatedArticlesString = new ArrayList<>();
	private BookmarkModel tempSelectBookmarkModel = null;
	private Handler handler;
	private Context context;
	private View rootView;
	private RecyclerView.Adapter adapter;

	public RelatedArticlesPopupWindow(Context context) {
		this(context, null);
	}

	public RelatedArticlesPopupWindow(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RelatedArticlesPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public RelatedArticlesPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initRelatedArticlesPopupWindow(context);
	}

	private void initRelatedArticlesPopupWindow(Context context) {
		this.context = context;
		this.adapter = new RelatedArticlesAdapter();
		LayoutInflater inflater = LayoutInflater.from(context);
		rootView = inflater.inflate(R.layout.popup_window_related_articles, null);
		RecyclerView rvRelatedArticles = rootView.findViewById(R.id.rv_related_articles);
		setContentView(rootView);
		rvRelatedArticles.setLayoutManager(new LinearLayoutManager(context));
		rvRelatedArticles.setAdapter(adapter);
		this.setAnimationStyle(R.style.popupWindowAnimStyle);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void resetAllRelatedArticles(List<BookmarkModel> languages) {
		allRelatedArticlesString.clear();
		allRelatedArticlesString.addAll(languages);
		this.adapter.notifyDataSetChanged();
	}

	private class RelatedArticlesAdapter extends RecyclerView.Adapter<RelatedArticlesViewHolder> {
		@NonNull
		@Override
		public RelatedArticlesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_language, parent, false);
			return new RelatedArticlesViewHolder(rootView);
		}

		@Override
		public void onBindViewHolder(@NonNull RelatedArticlesViewHolder holder, int position) {
			try {
				BookmarkModel lang = Optional.of(position)
					.filter(p -> p >= 0 && p < allRelatedArticlesString.size())
					.map(p -> allRelatedArticlesString.get(position))
					.orElseThrow(Throwable::new);
				holder.textView.setText(lang.getTitle());
				holder.textView.setOnClickListener(view -> {
					if (handler != null && lang != null) {
						Message obtain = Message.obtain();
						obtain.what = CommonConstants.Handler.GOTO_ANOTHER_URL;
						obtain.obj = lang;
						handler.sendMessage(obtain);
					}
				});
				holder.textView.setBackgroundResource(null == tempSelectBookmarkModel ? R.color.buttonUnpressed : R.color.buttonPressed);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}

		@Override
		public int getItemCount() {
			return allRelatedArticlesString.size();
		}
	}

	class RelatedArticlesViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		private RelatedArticlesViewHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.vh_tv);
		}
	}

}
