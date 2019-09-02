package cn.jinelei.smart.archwiki.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class LanguageModel implements Cloneable, Serializable, Parcelable {
	private String summaryLang;
	private String detailLang;
	private String hrefSuffix;
	
	public String getSummaryLang() {
		return summaryLang;
	}
	
	public String getDetailLang() {
		return detailLang;
	}
	
	public String getHrefSuffix() {
		return hrefSuffix;
	}
	
	public LanguageModel(String summaryLang, String detailLang, String hrefSuffix) {
		this.summaryLang = summaryLang;
		this.detailLang = detailLang;
		this.hrefSuffix = hrefSuffix;
	}
	
	@Override
	public LanguageModel clone() {
		return new LanguageModel(this.summaryLang, this.detailLang, this.hrefSuffix);
	}
	
	@Override
	public String toString() {
		return "LanguageModel{" +
			"summaryLang='" + summaryLang + '\'' +
			", detailLang='" + detailLang + '\'' +
			", hrefSuffix='" + hrefSuffix + '\'' +
			'}';
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.summaryLang);
		dest.writeString(this.detailLang);
		dest.writeString(this.hrefSuffix);
	}
	
	protected LanguageModel(Parcel in) {
		this.summaryLang = in.readString();
		this.detailLang = in.readString();
		this.hrefSuffix = in.readString();
	}
	
	public static final Parcelable.Creator<LanguageModel> CREATOR = new Parcelable.Creator<LanguageModel>() {
		@Override
		public LanguageModel createFromParcel(Parcel source) {
			return new LanguageModel(source);
		}
		
		@Override
		public LanguageModel[] newArray(int size) {
			return new LanguageModel[size];
		}
	};
}

