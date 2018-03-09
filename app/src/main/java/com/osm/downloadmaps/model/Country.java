package com.osm.downloadmaps.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.osm.downloadmaps.interfaces.Region;

public class Country extends Region {

    private int id;
    private String name;
    private String loadPath;
    private int isLoadMap;

    private Continent continent;

    public Country() {
        super();
    }

    public Country(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Country(String name, String loadPath, int isLoadMap, Continent continent) {
        super();
        this.name = name;
        this.loadPath = loadPath;
        this.isLoadMap = isLoadMap;
        this.continent = continent;
    }

    public Country(int id, String name, String loadPath, int isLoadMap, Continent continent) {
        super();
        this.id = id;
        this.name = name;
        this.loadPath = loadPath;
        this.isLoadMap = isLoadMap;
        this.continent = continent;
    }

    private Country(Parcel in) {
        super();
        this.id = in.readInt();
        this.name = in.readString();
        this.loadPath = in.readString();
        this.isLoadMap = in.readInt();

        this.continent = in.readParcelable(Continent.class.getClassLoader());
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

    public Continent getContinent() {
        return continent;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    @Override
    public String toString() {
        return "Country [id=" + id + ", name=" + name
                + ", loadpath=" + loadPath
                + ", isLoadMap=" + isLoadMap
                + ", continent=" + continent + "]";
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
        Country other = (Country) obj;
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
        parcel.writeParcelable(getContinent(), flags);
    }

    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

}
