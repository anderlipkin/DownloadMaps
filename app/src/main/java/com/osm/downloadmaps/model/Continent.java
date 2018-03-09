package com.osm.downloadmaps.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.osm.downloadmaps.interfaces.Region;

public class Continent extends Region {

    private int id;
    private String name;

    public Continent() {
        super();
    }

    public Continent(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Continent(String name) {
        this.name = name;
    }

    private Continent(Parcel in) {
        super();
        this.id = in.readInt();
        this.name = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Continent [id=" + id + ", name=" + name + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getId());
        parcel.writeString(getName());
    }

    public static final Parcelable.Creator<Continent> CREATOR = new Parcelable.Creator<Continent>() {
        public Continent createFromParcel(Parcel in) {
            return new Continent(in);
        }

        public Continent[] newArray(int size) {
            return new Continent[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Continent other = (Continent) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
