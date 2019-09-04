package cn.jinelei.smart.archwiki.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Strings;

import java.io.Serializable;

public class BookmarkModel implements Cloneable, Parcelable, Serializable {
	private String title;
	private String url;
	private String icon;
	private Long createTimestamp;

	public BookmarkModel(String title, String url, String icon) {
		this(title, url, icon, System.currentTimeMillis());
	}

	public BookmarkModel(String title, String url, String icon, Long createTimestamp) {
		this.title = title;
		this.url = url;
		this.icon = icon;
		this.createTimestamp = createTimestamp;
	}

	public BookmarkModel(String title, String url) {
		this(title, url, "");
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Long getCreateTimestamp() {
		return createTimestamp;
	}

	public boolean checkBaseInfomation() {
		return null != this
			&& !Strings.isNullOrEmpty(this.getUrl())
			&& !Strings.isNullOrEmpty(this.getTitle())
			&& !Strings.isNullOrEmpty(this.getIcon());
	}

	public boolean compareBaseInfomation(BookmarkModel param) {
		return null != this && null != param
			&& !Strings.isNullOrEmpty(this.getUrl())
			&& getUrl().equals(param.getUrl())
			&& !Strings.isNullOrEmpty(this.getTitle())
			&& getTitle().equals(param.getTitle())
			&& getCreateTimestamp().equals(param.getCreateTimestamp())
			&& !Strings.isNullOrEmpty(this.getIcon())
			&& getIcon().equals(param.getIcon());
	}

	@Override
	protected BookmarkModel clone() {
		return new BookmarkModel(title, url, icon, createTimestamp);
	}

	@Override
	public String toString() {
		return "BookmarkModel{" +
			"title='" + title + '\'' +
			", url='" + url + '\'' +
			", icon='" + icon + '\'' +
			", createTimestamp=" + createTimestamp +
			'}';
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.title);
		dest.writeString(this.url);
		dest.writeString(this.icon);
		dest.writeValue(this.createTimestamp);
	}

	protected BookmarkModel(Parcel in) {
		this.title = in.readString();
		this.url = in.readString();
		this.icon = in.readString();
		this.createTimestamp = (Long) in.readValue(Long.class.getClassLoader());
	}

	public static final Creator<BookmarkModel> CREATOR = new Creator<BookmarkModel>() {
		@Override
		public BookmarkModel createFromParcel(Parcel source) {
			return new BookmarkModel(source);
		}

		@Override
		public BookmarkModel[] newArray(int size) {
			return new BookmarkModel[size];
		}
	};
}

