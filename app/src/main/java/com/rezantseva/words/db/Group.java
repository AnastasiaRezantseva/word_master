package com.rezantseva.words.db;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Group implements Parcelable {

    private static final String GROUP_BUNDLE_KEY = "group-bundle-key";

    public static Map<String, Word[]> getInitialGroups() {
        Map<String, Word[]> map = new HashMap<>();
        map.put("Sport", new Word[]{
                new Word("Football", "Футбол"),
                new Word("Ball", "Мяч"),
                new Word("Run", "Бежать"),
                new Word("Basketball", "Баскетбол"),
                new Word("Hockey", "Хоккей"),
                new Word("Skiing", "Лыжи"),
                new Word("Boxing", "Бокс"),
                new Word("Skates", "Коньки"),
                new Word("Judo", "Дзюдо"),
                new Word("Tennis", "Теннис")});
        map.put("Fruits", new Word[]{
                new Word("Apple", "Яблоко"),
                new Word("Orange", "Апельсин"),
                new Word("Mango", "Манго"),
                new Word("Limon", "Лимон")});
        map.put("Vegetables",
                new Word[]{});
        map.put("House",
                new Word[]{});
        return map;
    }

    private long id;
    private String name;

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(Parcel in) {
        readFromParcel(in);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && ((Group) o).getId() == getId();
    }

    @Override
    public String toString() {
        return getName();
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public static Group fromBundle(Bundle bundle) {
        return bundle.getParcelable(GROUP_BUNDLE_KEY);
    }

    public void toBundle(Bundle bundle) {
        bundle.putParcelable(GROUP_BUNDLE_KEY, this);
    }
}
