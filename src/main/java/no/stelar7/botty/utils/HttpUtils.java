package no.stelar7.botty.utils;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;

public class HttpUtils
{
    public static final HttpClient client = HttpClient.newHttpClient();
    
    public static String doGetRequest(String link)
    {
        try
        {
            return client.send(HttpRequest.newBuilder(URI.create(link)).build(), BodyHandlers.ofString()).body();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
