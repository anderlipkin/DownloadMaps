package com.osm.downloadmaps.utils;

import com.osm.downloadmaps.interfaces.Region;

public class RegionUtil {
    private static final String downloadServerUrl = "http://download.osmand.net/download.php?standard=yes&file=";
    private static final String suffixZip = "_2.obf.zip";

    public static String getNormalName(String name) {
        String spaceName = name.replaceAll("[-|_]", " ");
        return Character.toUpperCase(spaceName.charAt(0)) + spaceName.substring(1);
    }

    public static String getLoadPathForCounty(String pathCountry) {
        String path;
        String tempPath;
        if (pathCountry.startsWith("$name_")) {
            tempPath = Character.toUpperCase(pathCountry.charAt(6)) + pathCountry.substring(7);
            path = downloadServerUrl + tempPath + suffixZip;
        }
        else {
            tempPath = Character.toUpperCase(pathCountry.charAt(0)) + pathCountry.substring(1);
            path = downloadServerUrl + tempPath + suffixZip;
        }
        return path;
    }

    public static String getLoadPathForCity(Region city) {
        String resultPath;
        String pathCity = city.getLoadPath();
        String tempPath;

        if (pathCity.startsWith("$name_")) {
            tempPath = pathCity.replace("$name", city.getCountry().getName());
            resultPath = downloadServerUrl
                   + Character.toUpperCase(tempPath.charAt(0)) + tempPath.substring(1)
                   + suffixZip;
        }
        else {
            resultPath = downloadServerUrl
                    + Character.toUpperCase(pathCity.charAt(0)) + pathCity.substring(1)
                    + suffixZip;;
        }

        return resultPath;
    }

}
