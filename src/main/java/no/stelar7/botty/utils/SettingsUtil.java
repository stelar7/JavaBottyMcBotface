package no.stelar7.botty.utils;

import com.google.gson.*;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class SettingsUtil
{
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static Path getJarFolder()
    {
        try
        {
            return new File(SettingsUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
}
