package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier {
    private static final String BOT_TOKEN = "7801461461:AAEVSW29o-4qRnj_nNfXb0SZdIeoepeg6ww";

    public static void sendMessageToTelegram(String message) {
        try {
            String chatId = "5208483861";

            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + message + "\", \"parse_mode\": \"Markdown\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            Thread.sleep(500);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Message sent to Telegram successfully!");
            } else {
                System.out.println("Failed to send message to Telegram. Response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
