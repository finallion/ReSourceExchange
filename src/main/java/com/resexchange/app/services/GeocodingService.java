package com.resexchange.app.services;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeocodingService {

    // Nominatim API URL
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";

    public static double[] getCoordinatesFromAddress(String street, String city, String postalCode, String country) {
        try {
            String address = street + ", " + city + ", " + postalCode + ", " + country;

            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");

            String urlString = NOMINATIM_API_URL + "?q=" + encodedAddress + "&format=json&addressdetails=1";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Java Geocoding Service");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // JSON-Antwort parsen
            JSONArray jsonResponse = new JSONArray(response.toString());

            if (jsonResponse.length() > 0) {
                // Nimm das erste Ergebnis aus der Antwort
                JSONObject firstResult = jsonResponse.getJSONObject(0);
                double lat = firstResult.getDouble("lat");
                double lng = firstResult.getDouble("lon");

                return new double[]{lat, lng};
            } else {
                // Wenn keine Ergebnisse gefunden wurden
                System.out.println("Keine Koordinaten für die Adresse gefunden.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
