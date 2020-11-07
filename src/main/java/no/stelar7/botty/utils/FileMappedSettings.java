package no.stelar7.botty.utils;

import discord4j.common.util.Snowflake;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileMappedSettings
{
    private final Path                file;
    private       Map<String, Object> settings;
    
    public FileMappedSettings(Path file)
    {
        this.file = file;
        this.load();
    }
    
    public Object get(String key)
    {
        return this.settings.get(key);
    }
    
    public Snowflake getSnowflake(String key)
    {
        return Snowflake.of(Long.parseLong(get(key).toString()));
    }
    
    public void putSnowflake(String key, Snowflake value)
    {
        this.put(key, value.asString());
    }
    
    public Object getOrDefault(String key, String def)
    {
        return this.settings.getOrDefault(key, def);
    }
    
    
    public void put(String key, Object value)
    {
        this.settings.put(key, value);
        this.save();
    }
    
    public Object getOrPut(String key, Object value)
    {
        Object newValue = this.settings.compute(key, (k, v) -> v == null ? value : v);
        if (newValue.equals(value))
        {
            this.save();
            return newValue;
        }
        
        return newValue;
    }
    
    public void save()
    {
        try
        {
            String content = SettingsUtil.gson.toJson(this.settings);
            Files.writeString(this.file, content);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void load()
    {
        try
        {
            String content = Files.readString(this.file);
            this.settings = SettingsUtil.gson.fromJson(content, Map.class);
            if (this.settings == null)
            {
                this.settings = new HashMap<>();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
