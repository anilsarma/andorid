package com.example.asarma.njrails;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asarma on 11/9/2017.
 */

public class ParcelResult implements Parcelable {
    public ArrayList<HashMap<String, String>> data = new ArrayList<>();

    ParcelResult( ArrayList<HashMap<String, String>> data)
    {
        this.data = data;
    }
    ParcelResult(Parcel in)
    {
        int sz = in.readInt();
        for (int i = 0; i < sz; i++) {
            HashMap<String, String> map = new HashMap<>();
            in.readMap(map, String.class.getClassLoader());
            data.add(map);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.size());
        for (int i = 0; i < data.size(); i++) {
            dest.writeMap(data.get(i));
        }
    }

    public static final Parcelable.Creator<ParcelResult> CREATOR = new Parcelable.Creator<ParcelResult>() {
        public ParcelResult createFromParcel(Parcel in) {
            return new ParcelResult(in);
        }

        public ParcelResult[] newArray(int size) {
            return new ParcelResult[size];
        }
    };
}
