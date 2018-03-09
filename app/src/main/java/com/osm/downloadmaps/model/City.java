package com.osm.downloadmaps.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.osm.downloadmaps.interfaces.Region;

;

public class City extends Region {

    private int id;
    private String name;
    private String loadPath;
    private int isLoadMap;

    private Country country;

    public City() {
        super();
    }

    public City(int id, String name, String loadPath, int isLoadMap, Country country) {
        super();
        this.id = id;
        this.name = name;
        this.loadPath = loadPath;
        this.isLoadMap = isLoadMap;
        this.country = country;
    }

    public City(String name, String loadPath, int isLoadMap, Country country) {
        super();
        this.name = name;
        this.loadPath = loadPath;
        this.isLoadMap = isLoadMap;
        this.country = country;
    }

    private City(Parcel in) {
        super();
        this.id = in.readInt();
        this.name = in.readString();
        this.loadPath = in.readString();
        this.isLoadMap = in.readInt();

        this.country = in.readParcelable(Country.class.getClassLoader());
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
    public String getLoadPath() {
        return loadPath;
    }

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }

    public int getIsLoadMap() {
        return isLoadMap;
    }

    public void setIsLoadMap(int isLoadMap) {
        this.isLoadMap = isLoadMap;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "City [id=" + id + ", name=" + name
                + ", loadpath=" + loadPath
                + ", isLoadMap=" + isLoadMap
                + ", country=" + country + "]";
    }

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
        City other = (City) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getId());
        parcel.writeString(getName());
        parcel.writeString(getLoadPath());
        parcel.writeInt(getIsLoadMap());
        parcel.writeParcelable(getCountry(), flags);
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };

}
