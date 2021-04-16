package no.stelar7.botty.utils;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public class StringUtils
{
    public static int levenshteinDistance(String a, String b)
    {
        if (a.equals(b))
        {
            return 0;
        }
        
        if (a.length() == 0)
        {
            return b.length();
        }
        
        if (b.length() == 0)
        {
            return a.length();
        }
        
        int   size = Math.max(a.length(), b.length()) + 1;
        int[] v0   = new int[size];
        int[] v1   = new int[size];
        
        for (int i = 0; i < b.length(); i++)
        {
            v0[i] = i;
            v1[i] = 0;
        }
        
        for (int i = 0; i < a.length(); i++)
        {
            v1[0] = i + 1;
            
            for (int j = 0; j < b.length(); j++)
            {
                int cost = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
                
                int deletionCost   = v0[j + 1] + 1;
                int insertCost     = v1[j] + 1;
                int substituteCost = v0[j] + cost;
                int minCost        = Math.min(Math.min(deletionCost, insertCost), substituteCost);
                
                v1[j + 1] = minCost;
            }
            System.arraycopy(v1, 0, v0, 0, v1.length);
        }
        
        return v1[b.length()];
    }
    
    public static <T> T toType(String json, Class<T> clazz)
    {
        return SettingsUtil.gson.fromJson(json, clazz);
    }
    
    public static <T> List<T> toTypeList(String json, Class<T> clazz)
    {
        return SettingsUtil.gson.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
    }
    
    public static String toUrl(String text, String link)
    {
        return "[" + text + "](" + link + ")";
    }
}