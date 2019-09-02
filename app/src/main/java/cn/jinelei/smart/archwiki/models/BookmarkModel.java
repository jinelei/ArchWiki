package cn.jinelei.smart.archwiki.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class BookmarkModel implements Cloneable, Parcelable, Serializable {
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
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.title);
		dest.writeString(this.url);
	}
	
	protected BookmarkModel(Parcel in) {
		this.title = in.readString();
		this.url = in.readString();
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

