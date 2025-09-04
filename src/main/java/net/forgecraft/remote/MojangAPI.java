package net.forgecraft.remote;

import net.forgecraft.LCaRLP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class MojangAPI {
    public static boolean checkPlayer(final String username, final UUID uuid) {
        try {
            final URL urlUUID = new URI("https://api.mojang.com/users/profiles/minecraft/" + username).toURL();
            HttpURLConnection connection = (HttpURLConnection) urlUUID.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            LCaRLP.getInstance().getLogger().info(String.valueOf(responseCode));
            if (responseCode != 200) return false;

            final String officialUUID = readBody(connection, "id");
            final URL urlSession = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + officialUUID).toURL();
            connection = (HttpURLConnection) urlSession.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            responseCode = connection.getResponseCode();
            LCaRLP.getInstance().getLogger().info(String.valueOf(responseCode));
            if (responseCode != 200) return false;

            final String playerName = readBody(connection, "name");
            return playerName.equalsIgnoreCase(username);

        } catch (Exception e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }

    private static String readBody(final HttpURLConnection connection, final String key) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final StringBuilder response = new StringBuilder();

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        LCaRLP.getInstance().getLogger().info(response.toString());
        final JSONObject json = new JSONObject(response.toString());
        return json.getString(key);
    }
}
