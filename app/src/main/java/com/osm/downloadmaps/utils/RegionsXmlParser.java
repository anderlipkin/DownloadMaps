package com.osm.downloadmaps.utils;

import android.content.Context;
import android.util.Xml;

import com.osm.downloadmaps.db.CityDAO;
import com.osm.downloadmaps.db.ContinentDAO;
import com.osm.downloadmaps.db.CountryDAO;
import com.osm.downloadmaps.model.City;
import com.osm.downloadmaps.model.Continent;
import com.osm.downloadmaps.model.Country;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class RegionsXmlParser {
    private String TAG = RegionsXmlParser.class.getName();

    private int continentDepth = 2;
    private int countryDepth = 3;
    private int cityDepth = 5; //4 - okrug/ 5 - city

    private Context context;

    private ContinentDAO continentDAO;
    private CountryDAO countryDAO;
    private CityDAO cityDAO;

    public RegionsXmlParser(Context context, ContinentDAO continentDAO) {
        this.context = context;
        this.continentDAO = continentDAO;
    }

    public void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            readContinent(parser);
        } finally {
            in.close();
        }

    }

    private void readContinent(XmlPullParser parser) throws XmlPullParserException, IOException {
        Continent continent;

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getDepth() == continentDepth) {
                continent = new Continent(parser.getAttributeValue(null, "name"));
                String loadSuffix = parser.getAttributeValue(null, "inner_download_suffix");
                String loadPrefix = null;
                if (loadSuffix == null) {
                    loadSuffix = parser.getAttributeValue(null, "download_suffix");
                    if (parser.getAttributeValue(null, "name").equals("russia")) {
                        loadPrefix = parser.getAttributeValue(null, "inner_download_prefix");
                    }
                }
                continentDAO.save(continent);
                readCountries(parser, continent.getName(), loadSuffix, loadPrefix);
            }
        }
    }


    private void readCountries(XmlPullParser parser, String nameContinent, String loadSuffix, String loadPrefix) throws XmlPullParserException, IOException {
        if (countryDAO == null) {
            countryDAO = new CountryDAO(context);
        }
        Country country;
        int countCities = 0;

        while (parser.nextTag() != XmlPullParser.END_DOCUMENT && parser.getDepth() > continentDepth) {
            countCities = 0;

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getDepth() != countryDepth) {
                continue;
            }

            String nameCountry = parser.getAttributeValue(null, "name");
            String tempPrefix = parser.getAttributeValue(null, "inner_download_prefix");

            country = new Country();

            if (tempPrefix != null ||
                    parser.getAttributeValue(null, "join_map_files") != null) {

                if (tempPrefix != null) {
                    loadPrefix = tempPrefix;
                }

                country.setName(nameCountry);
                country.setLoadPath("");
                country.setIsLoadMap(0);
                country.setContinent(continentDAO.getContinentByName(nameContinent));
                countryDAO.save(country);
                countCities = readCities(parser, country.getName(), loadSuffix, loadPrefix);
            } else {
                country.setLoadPath(
                        buildLoadPath(loadPrefix, nameCountry, loadSuffix));
                country.setName(nameCountry);
                country.setIsLoadMap(0);
                country.setContinent(continentDAO.getContinentByName(nameContinent));
                countryDAO.save(country);
            }

            if (countCities == 0) {
                Country countryUpdate = countryDAO.getCountryByName(country.getName());;
                countryUpdate.setLoadPath(
                        buildLoadPath(loadPrefix, nameCountry, loadSuffix));
                countryDAO.update(countryUpdate);
            }

        }
    }

    private int readCities(XmlPullParser parser, String nameCountry,
                            String loadSuffix, String loadPrefix)
            throws XmlPullParserException, IOException {
        if (cityDAO == null) {
            cityDAO = new CityDAO(context);
        }

        int countCity = 0;
        City city;
        String parentNameCity = "";
        String innerDownloadPrefix = "";
        String loadPath = "";
        String nameCity = "";
        boolean hasInnerMap = false;

        parser.next();

        String type = null;
        String map = null;

        while (parser.next() != XmlPullParser.END_DOCUMENT && parser.getDepth() >= countryDepth) {

            if (parser.getEventType() != XmlPullParser.START_TAG && parser.getDepth() != cityDepth-1) {
                continue;
            }

            //!hasInnerMap && !parentNameCity.isEmpty() &&
            if (parser.getEventType() == XmlPullParser.TEXT) {
                continue;
            }

            switch (parser.getEventType()) {
                //1- and 2-level city
                case XmlPullParser.START_TAG:
                    String tempPrefix = parser.getAttributeValue(null, "inner_download_prefix");

                    if (tempPrefix != null && parser.getDepth() == cityDepth-1) {
                        hasInnerMap = true;
                        innerDownloadPrefix = tempPrefix;
                        parentNameCity = parser.getAttributeValue(null, "name");
                        continue;
                    }

                    nameCity = parser.getAttributeValue(null, "name");
                    type = parser.getAttributeValue(null, "type");
                    map = parser.getAttributeValue(null, "map");

                    if (!innerDownloadPrefix.isEmpty() &&
                            (type != null && (type.equals("srtm") || type.equals("hillshade")) ||
                            (map != null && map.equals("no")) ||
                            parser.getDepth() > cityDepth )) {
                        hasInnerMap = false;
                        continue;
                    } else if (hasInnerMap){
                        loadPath = buildLoadPath(innerDownloadPrefix, nameCity, loadSuffix); // 2-level city
                    } else {
                        continue; // 1-level city
                    }
                    break;
                // Only 1-level city
                case XmlPullParser.END_TAG:
                    if (!parentNameCity.isEmpty()) {
                        nameCity = parentNameCity;
                        loadPath = buildLoadPath(loadPrefix, nameCity, loadSuffix);
                        hasInnerMap = false;
                        parentNameCity = "";
                    }
                    else if ((type != null && (type.equals("srtm") || type.equals("hillshade"))) ||
                                    (map != null && map.equals("no"))) {

                        loadPath = "";
                    }
                    else {
                        loadPath = buildLoadPath(loadPrefix, nameCity, loadSuffix);
                    }
                    break;
            }

            if (nameCity != null && !nameCity.isEmpty() && !loadPath.isEmpty()) {
                city = new City();
                city.setName(nameCity);
                city.setLoadPath(loadPath);
                city.setIsLoadMap(0);
                city.setCountry(countryDAO.getCountryByName(nameCountry));
                cityDAO.save(city);
                countCity++;
            }
        }
        return countCity;
    }

    private String buildLoadPath(String loadPrefix, String name, String loadSuffix) {
        StringBuilder loadPath = new StringBuilder();
        if (loadPrefix != null && loadSuffix != null) {
            if (name != null) {
                loadPath.append(loadPrefix).append("_").append(name)
                        .append("_").append(loadSuffix);
            } else {
                loadPath.append(loadPrefix).append("_").append(loadSuffix);
            }
        }
        else if (loadPrefix == null || loadPrefix.isEmpty()) {
            loadPath.append(name).append("_").append(loadSuffix);
        }
        else {
            loadPath.append(loadPrefix).append("_").append(name);
        }

        return loadPath.toString();
    }

}
