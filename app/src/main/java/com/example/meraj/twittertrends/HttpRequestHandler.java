package com.example.meraj.twittertrends;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by meraj on 26/02/2018.
 */

public class HttpRequestHandler {
    final String TAG = HttpRequestHandler.class.getSimpleName();

    StringBuilder response = new StringBuilder();
    String method = "GET";
    String url = "";
    String authorization = "";
    String contentType = "";
    String param = "";
    BufferedReader bufferedReader = null;
    HttpURLConnection conn = null;

    HttpRequestHandler(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String makeRequest() {
        try {
            URL rURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) rURL.openConnection();
            conn.setRequestMethod(method);

            if (!"".equals(authorization)) {
                conn.setRequestProperty("Authorization", authorization);
            }

            if (!"".equals(contentType)) {
                conn.setRequestProperty("Content-Type", contentType);
            }

            conn.connect();

            if (!"".equals(param)) {
                OutputStream oStream =  conn.getOutputStream();

                oStream.write(param.getBytes());
                oStream.flush();
                oStream.close();
            }

            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
        } catch (ProtocolException e) {
            Log.d(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "IOException" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response.toString();
    }
}
