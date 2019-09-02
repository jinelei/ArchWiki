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
import cn.jinelei.smart.archwiki.common.constants.CommonConstants;
import cn.jinelei.smart.archwiki.models.LanguageModel;

public class LanguagePopupWindow extends PopupWindow implements View.OnClickListener {
	private static final String TAG = "LanguagePopupWindow";
	private final List<LanguageModel> allLanguageString = new ArrayList<>();
	private LanguageModel selectLanguageModel = null;
	private LanguageModel tempSelectLanguageModel = null;
	private Handler handler;
	private Context context;
	private View rootView;
	private RecyclerView.Adapter adapter;
	private TextView tvTitle;
	
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
		rootView.findViewById(R.id.tv_confirm).setOnClickListener(this);
		rootView.findViewById(R.id.tv_cancel).setOnClickListener(this);
		tvTitle = rootView.findViewById(R.id.tv_title);
		RecyclerView rvLanguage = rootView.findViewById(R.id.rv_language);
		setContentView(rootView);
		setOnDismissListener(() -> tvTitle.setText(context.getString(R.string.select_preference_language) + ": " + selectLanguageModel.getDetailLang()));
		rvLanguage.setLayoutManager(new LinearLayoutManager(context));
		rvLanguage.setAdapter(adapter);
		this.setAnimationStyle(R.style.popupWindowAnimStyle);
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void setSelectLanguageModel(LanguageModel selectLanguageModel) {
		this.selectLanguageModel = selectLanguageModel;
		tvTitle.setText(selectLanguageModel.getDetailLang());
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
			case R.id.tv_confirm:
				if (null != tempSelectLanguageModel) {
					selectLanguageModel = tempSelectLanguageModel;
					Message obtain = Message.obtain();
					obtain.what = CommonConstants.Handler.CONFIRM_SELECT_LANGUAGE;
					obtain.obj = tempSelectLanguageModel;
					Log.d(TAG, "select language: " + tempSelectLanguageModel);
					tvTitle.setText(context.getString(R.string.select_preference_language) + ": " + selectLanguageModel.getDetailLang());
					handler.sendMessage(obtain);
				}
				dismiss();
				break;
			case R.id.tv_cancel:
				tvTitle.setText(context.getString(R.string.select_preference_language) + ": " + selectLanguageModel.getDetailLang());
				handler.sendEmptyMessage(CommonConstants.Handler.CANCEL_SELECT_LANGUAGE);
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
					tempSelectLanguageModel = lang;
					tvTitle.setText(context.getText(R.string.select_preference_language) + ": " + tempSelectLanguageModel.getDetailLang());
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
	
}
