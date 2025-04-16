package com.rezantseva.words.db;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
    private static final String WORD_BUNDLE_KEY = "word-bundle-key";

    private long id;
    private long groupId;
    private String origin;
    private String translation;

    public Word() {
    }

    public Word(String origin, String translation) {
        this.origin = origin;
        this.translation = translation;
    }

    public Word(Parcel in) {
        readFromParcel(in);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && ((Word) o).getId() == getId();
    }

    @Override
    public String toString() {
        return getOrigin();
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readLong();
        this.groupId = in.readLong();
        this.origin = in.readString();
        this.translation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.groupId);
        dest.writeString(this.origin);
        dest.writeString(this.translation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public static Word fromBundle(Bundle bundle) {
        return bundle.getParcelable(WORD_BUNDLE_KEY);
    }

    public void toBundle(Bundle bundle) {
        bundle.putParcelable(WORD_BUNDLE_KEY, this);
    }
}
