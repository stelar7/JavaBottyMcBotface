package no.stelar7.botty.utils;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

public class HttpUtils
{
    public static final HttpClient client = HttpClient.newHttpClient();
    
    public static String doGetRequest(String link)
    {
        return doGetRequest(link, Map.of());
    }
    
    public static String doGetRequest(String link, Map<String, String> headers)
    {
        try
        {
            Builder request = HttpRequest.newBuilder(URI.create(link));
            headers.forEach(request::setHeader);
            
            return client.send(request.build(), BodyHandlers.ofString()).body();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
