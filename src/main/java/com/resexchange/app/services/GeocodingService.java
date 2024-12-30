package com.resexchange.app.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;


/**
 * Service-Klasse, die eine Methode zur Geocodierung von Adressen mit der Nominatim API bereitstellt.
 * Diese Klasse verwendet die OpenStreetMap Nominatim API, um geographische Koordinaten (Breiten- und Längengrad)
 * aus einer gegebenen Adresse zu extrahieren.
 *
 * @author Dominik
 */
@Service
public class GeocodingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeocodingService.class);
    // Nominatim API URL
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";

    /**
     * Ruft die geographischen Koordinaten (Breiten- und Längengrad) für eine gegebene Adresse ab.
     *
     * Diese Methode nutzt die Nominatim API von OpenStreetMap, um eine Adresse in geographische Koordinaten
     * umzuwandeln. Die Adresse wird durch die Kombination von Straßenname, Stadt, Postleitzahl und Land
     * gebildet. Falls die API eine Adresse findet, gibt die Methode die entsprechenden Koordinaten zurück.
     *
     * @param street    die Straße der Adresse
     * @param city      die Stadt der Adresse
     * @param postalCode die Postleitzahl der Adresse
     * @param country   das Land der Adresse
     * @return ein Array mit zwei Werten: dem Breiten- und dem Längengrad der Adresse,
     *         oder null, wenn keine Koordinaten gefunden wurden
     * @author Dominik
     */
    public static double[] getCoordinatesFromAddress(String street, String city, String postalCode, String country) {
        try {
            String address = street + ", " + city + ", " + postalCode + ", " + country;

            LOGGER.info("Start geocoding for address: {}", address);
            // Kodiert die Adresse für die URL
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");

            String urlString = NOMINATIM_API_URL + "?q=" + encodedAddress + "&format=json&addressdetails=1";

            // Öffnet die Verbindung zur Nominatim API
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

            // Prüft, ob Ergebnisse zurückgegeben wurden
            if (jsonResponse.length() > 0) {
                // Nimm das erste Ergebnis aus der Antwort
                JSONObject firstResult = jsonResponse.getJSONObject(0);
                double lat = firstResult.getDouble("lat");
                double lng = firstResult.getDouble("lon");

                LOGGER.info("Geocoding successful for address: {}, Latitude: {}, Longitude: {}", address, lat, lng);
                return new double[]{lat, lng};
            } else {
                LOGGER.warn("No coordinates found for address: {}", address);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred during geocoding for address: {}", street + ", " + city + ", " + postalCode + ", " + country, e);
            return null;
        }
    }
}
