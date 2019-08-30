package cn.jinelei.smart.archwiki.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.jinelei.smart.archwiki.R;
import cn.jinelei.smart.archwiki.constants.ArchConstants;

import static cn.jinelei.smart.archwiki.constants.ArchConstants.Handler.CONFIRM_SELECT_LANGUAGE;

public class LanguagePopupWindow extends PopupWindow implements View.OnClickListener {
	private static final String TAG = "LanguagePopupWindow";
	private final List<LanguageModel> allLanguageString = new ArrayList<>();
	private LanguageModel selectLanguageModel = null;
	private Handler handler;
	private Context context;
	private View rootView;
	private RecyclerView.Adapter adapter;
	private TextView selectLanguage;

	public LanguagePopupWindow(Context context) {
		this(context, null);
	}

	public LanguagePopupWindow(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LanguagePopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public LanguagePopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initLanguagePopupWindow(context);
	}

	private void initLanguagePopupWindow(Context context) {
		this.context = context;
		this.adapter = new LanguageAdapter();
		LayoutInflater inflater = LayoutInflater.from(context);
		rootView = inflater.inflate(R.layout.popup_window_language, null);
		rootView.findViewById(R.id.tv_title).setOnClickListener(this);
		RecyclerView rvLanguage = rootView.findViewById(R.id.rv_language);
		selectLanguage = rootView.findViewById(R.id.vh_tv_select_language);
		selectLanguage.setOnClickListener(this);
		setContentView(rootView);
		rvLanguage.setLayoutManager(new LinearLayoutManager(context));
		rvLanguage.setAdapter(adapter);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setSelectLanguageModel(LanguageModel selectLanguageModel) {
		this.selectLanguageModel = selectLanguageModel;
		selectLanguage.setText(selectLanguageModel.getDetailLang());
	}

	public void resetAllLanguage(List<LanguageModel> languages) {
		allLanguageString.clear();
		allLanguageString.addAll(languages);
		this.adapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_title:
				break;
			case R.id.vh_tv_select_language:
				handler.sendEmptyMessage(CONFIRM_SELECT_LANGUAGE);
				dismiss();
				break;
		}
	}

	private class LanguageAdapter extends RecyclerView.Adapter<LanguageViewHolder> {
		@NonNull
		@Override
		public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_language, parent, false);
			return new LanguageViewHolder(rootView);
		}

		@Override
		public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
			try {
				LanguageModel lang = Optional.of(position)
					.filter(p -> p >= 0 && p < allLanguageString.size())
					.map(p -> allLanguageString.get(position))
					.orElseThrow(Throwable::new);
				holder.language.setText(lang.getDetailLang());
				holder.language.setOnClickListener(view -> {
					Message obtain = Message.obtain();
					obtain.what = ArchConstants.Handler.SELECT_LANGUAGE;
					obtain.obj = lang;
					Log.d(TAG, "select language: " + lang);
					handler.sendMessage(obtain);
				});
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}


		@Override
		public int getItemCount() {
			return allLanguageString.size();
		}
	}

	class LanguageViewHolder extends RecyclerView.ViewHolder {
		TextView language;

		private LanguageViewHolder(View itemView) {
			super(itemView);
			language = itemView.findViewById(R.id.vh_tv_language);
		}
	}

	public static class LanguageModel {
		private String summaryLang;
		private String detailLang;
		private String href;

		public String getSummaryLang() {
			return summaryLang;
		}

		public String getDetailLang() {
			return detailLang;
		}

		public String getHref() {
			return href;
		}

		public LanguageModel(String summaryLang, String detailLang, String href) {
			this.summaryLang = summaryLang;
			this.detailLang = detailLang;
			this.href = href;
		}

		@Override
		public String toString() {
			return "LanguageModel{" +
				"summaryLang='" + summaryLang + '\'' +
				", detailLang='" + detailLang + '\'' +
				", href='" + href + '\'' +
				'}';
		}
	}
}
