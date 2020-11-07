package no.stelar7.botty.utils;

import com.google.gson.reflect.TypeToken;
import discord4j.common.util.Snowflake;

import java.util.*;

public class DatabaseMappedSettings
{
    private final DatabaseTable       connection;
    private       Map<String, String> settings;
    
    public DatabaseMappedSettings(DatabaseTable connection)
    {
        this.connection = connection;
        this.load();
    }
    
    public String get(String key)
    {
        return this.settings.get(key);
    }
    
    public String getOrDefault(String key, String def)
    {
        return this.settings.getOrDefault(key, def);
    }
    
    public void put(String key, String value)
    {
        this.settings.put(key, value);
        this.save();
    }
    
    public String getOrPut(String key, String value)
    {
        String newValue = this.settings.compute(key, (k, v) -> v == null ? value : v);
        if (newValue.equals(value))
        {
            this.save();
            return newValue;
        }
        
        return newValue;
    }
    
    public void save()
    {
        String content = SettingsUtil.gson.toJson(this.settings);
        connection.writeRow(content);
    }
    
    public void load()
    {
        String content = connection.readRow();
        this.settings = SettingsUtil.gson.fromJson(content, Map.class);
        if (this.settings == null)
        {
            this.settings = new HashMap<>();
        }
    }
    
    public Snowflake getSnowflake(String key)
    {
        return Snowflake.of(get(key));
    }
    
    public void putSnowflake(String key, Snowflake value)
    {
        this.put(key, value.asString());
    }
    
    public <T> List<T> getList(String key)
    {
        String  content = this.getOrDefault(key, "[]");
        List<T> data    = SettingsUtil.gson.fromJson(content, new TypeToken<List<T>>() {}.getType());
        return data;
    }
    
    public <T> void putList(String key, List<T> data)
    {
        String content = SettingsUtil.gson.toJson(data);
        this.put(key, content);
    }
    
    /**
     * Only primitive types can be used
     */
    public <K, V> Map<K, V> getMap(String key)
    {
        String    content = this.getOrDefault(key, "{}");
        Map<K, V> data    = SettingsUtil.gson.fromJson(content, new TypeToken<Map<K, V>>() {}.getType());
        return data;
    }
    
    /**
     * Only primitive types can be used
     */
    public <K, V> void putMap(String key, Map<K, V> data)
    {
        String content = SettingsUtil.gson.toJson(data);
        this.put(key, content);
    }
    
    
}
