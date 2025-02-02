package org.example.sites;

import com.squareup.okhttp.*;

import java.io.IOException;

public class WinnerController {

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://micros-prod1.gambling-solutions.ro/api/digitain-fetcher/v2/public/events";

    public String executePostRequest() throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        String jsonPayload = "{\n" +
                "    \"timeFrom\": \"2025-02-01T22:00:00Z\",\n" +
                "    \"timeTo\": \"2025-02-03T21:59:59Z\",\n" +
                "    \"sportId\": \"1\"\n" +
                "}";
        RequestBody body = RequestBody.create(mediaType, jsonPayload);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/10.3.0") // Optional
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
