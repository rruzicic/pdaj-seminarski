package com.simple.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StoreHandler implements HttpHandler {

    private Map<Integer, ValueWrapper> keyValueStore;

    public StoreHandler() {
        keyValueStore = new HashMap<>();
    }

    public Map<Integer, ValueWrapper> getKeyValueStore() {
        return this.keyValueStore;
    }

    public void setKeyValueStore(Map<Integer, ValueWrapper> keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        System.out.println(t.getRequestMethod() + " " + t.getRequestURI());
        if (t.getRequestMethod().equals("GET")) {
            String response = keyValueStore.toString() + "\n";
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (t.getRequestMethod().equals("PUT")) {
            Map<String, String> queryParams = new LinkedHashMap<String, String>();
            try {
                queryParams = splitQuery(t.getRequestURI());
            } catch (Exception e) {
                System.out.println("HERE:" + e.getMessage());
            }
            int key;
            try {
                key = Integer.parseInt(queryParams.get("key"));
            } catch (NumberFormatException e) {
                System.out.println("Could not parse the ID!");
                return;
            }
            keyValueStore.put(
                    key,
                    new ValueWrapper(queryParams.get("value")));
            String response = "OK\n";
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            String response = "Not found\n";
            t.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static Map<String, String> splitQuery(URI uri) {
        Map<String, String> queryPairs = new LinkedHashMap<String, String>();
        String query = uri.getQuery();
        System.out.println(query);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] splitPair = pair.split("=");
            queryPairs.put(splitPair[0], splitPair[1]);
        }
        System.out.println(queryPairs);
        return queryPairs;
    }
}
