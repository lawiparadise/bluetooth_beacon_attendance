package com.law.blueinnofora;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAXHandler for Google Weather API
 */
public class WeatherHandler extends DefaultHandler {

    private boolean in_current_conditions = false;
    private WeatherCurrentCondition weather = new WeatherCurrentCondition();

    public WeatherCurrentCondition getWeather() {
        return weather;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("current_conditions")) {
            this.in_current_conditions = true;
        } else {
            String dataAttribute = atts.getValue("data");

            if (localName.equals("city")) {
            } else if (localName.equals("postal_code")) {
            } else if (localName.equals("latitude_e6")) {

            } else if (localName.equals("longitude_e6")) {

            } else if (localName.equals("forecast_date")) {
            } else if (localName.equals("current_date_time")) {
            } else if (localName.equals("unit_system")) {
            } else if (localName.equals("day_of_week")) {
                if (this.in_current_conditions) {
                    weather.setDayofWeek(dataAttribute);
                }
            } else if (localName.equals("icon")) {
                if (this.in_current_conditions) {
                    weather.setIconURL(dataAttribute);
                }
            } else if (localName.equals("condition")) {
                if (this.in_current_conditions) {
                    weather.setCondition(dataAttribute);
                }
            } else if (localName.equals("temp_f")) {
                weather.setTempFahrenheit(Integer.parseInt(dataAttribute));
            } else if (localName.equals("temp_c")) {
                weather.setTempCelcius(Integer.parseInt(dataAttribute));
            } else if (localName.equals("humidity")) {
                weather.setHumidity(dataAttribute);
            } else if (localName.equals("wind_condition")) {
                weather.setWindCondition(dataAttribute);
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("current_conditions")) {
            this.in_current_conditions = false;
        }
    }

    public void characters(char ch[], int start, int length) {
    }
}