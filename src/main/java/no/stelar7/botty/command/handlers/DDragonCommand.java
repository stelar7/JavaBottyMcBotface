package no.stelar7.botty.command.handlers;

import com.google.gson.reflect.TypeToken;
import discord4j.core.spec.EmbedCreateSpec;
import no.stelar7.botty.command.*;
import no.stelar7.botty.utils.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DDragonCommand extends Command
{
    private static List<ChampionSummary> championData = new ArrayList<>();
    private static List<PerkData>        perkData     = new ArrayList<>();
    private static List<ItemData>        itemData     = new ArrayList<>();
    private static List<SummonerData>    summonerData = new ArrayList<>();
    
    private static final String championUrl = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champion-summary.json";
    private static final String skinUrl     = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/skins.json";
    private static final String perkUrl     = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/perks.json";
    private static final String itemUrl     = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/items.json";
    private static final String summonerUrl = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells.json";
    
    public DDragonCommand()
    {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::updateDDragonData, 0, 1, TimeUnit.DAYS);
    }
    
    private void updateDDragonData()
    {
        perkData = SettingsUtil.gson.fromJson(HttpUtils.doGetRequest(perkUrl), new TypeToken<List<PerkData>>() {}.getType());
        summonerData = SettingsUtil.gson.fromJson(HttpUtils.doGetRequest(itemUrl), new TypeToken<List<SummonerData>>() {}.getType());
        itemData = SettingsUtil.gson.fromJson(HttpUtils.doGetRequest(itemUrl), new TypeToken<List<ItemData>>() {}.getType());
        championData = SettingsUtil.gson.fromJson(HttpUtils.doGetRequest(championUrl), new TypeToken<List<ChampionSummary>>() {}.getType());
        Map<String, SkinData> skinData = SettingsUtil.gson.fromJson(HttpUtils.doGetRequest(skinUrl), new TypeToken<Map<String, SkinData>>() {}.getType());
        
        skinData.forEach((key, value) -> {
            int             id   = Integer.parseInt(key) / 1000;
            ChampionSummary data = championData.stream().filter(entry -> entry.getId() == id).findFirst().get();
            data.getSkins().add(value.getName());
        });
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        if (params.getCommand().equalsIgnoreCase("item"))
        {
            handleItem(params);
            return;
        }
        
        if (params.getCommand().equalsIgnoreCase("champ"))
        {
            handleChamp(params);
            return;
        }
        
        if (params.getCommand().equalsIgnoreCase("perk"))
        {
            handlePerk(params);
        }
        
        if (params.getCommand().equalsIgnoreCase("spell"))
        {
            handleSpell(params);
        }
    }
    
    private void handleSpell(CommandParameters params)
    {
        // TODO: find spell
        SummonerData item = summonerData.get(0);
        
        String path = "https://raw.communitydragon.org/latest/plugins" +
                      item.getIconPath()
                          .replace("lol-game-data", "rcp-be-lol-game-data/global/default")
                          .replace("/assets", "")
                          .toLowerCase();
        
        params.getMessage().getChannel().block()
              .createEmbed(embed -> generateEmbed(embed, classToMap(item), path, summonerUrl)).block();
    }
    
    private void handleChamp(CommandParameters params)
    {
        // TODO: find champ
        ChampionSummary item = championData.get(0);
        
        String path = "https://cdn.communitydragon.org/latest/champion/" + item.getId() + "/square";
        
        params.getMessage().getChannel().block()
              .createEmbed(embed -> generateEmbed(embed, classToMap(item), path, championUrl)).block();
    }
    
    private void handlePerk(CommandParameters params)
    {
        // TODO: find perk
        PerkData item = perkData.get(0);
        
        String path = "https://raw.communitydragon.org/latest/plugins" +
                      item.getIconPath()
                          .replace("lol-game-data", "rcp-be-lol-game-data/global/default")
                          .replace("/assets", "")
                          .toLowerCase();
        
        params.getMessage().getChannel().block()
              .createEmbed(embed -> generateEmbed(embed, classToMap(item), path, perkUrl)).block();
    }
    
    private void handleItem(CommandParameters params)
    {
        // TODO: find item
        ItemData item = itemData.get(0);
        
        String path = "https://raw.communitydragon.org/latest/plugins" +
                      item.getIconPath()
                          .replace("lol-game-data", "rcp-be-lol-game-data/global/default")
                          .replace("/assets", "")
                          .toLowerCase();
        
        params.getMessage().getChannel().block()
              .createEmbed(embed -> generateEmbed(embed, classToMap(item), path, itemUrl)).block();
    }
    
    private Map<String, String> classToMap(Object item)
    {
        Map<String, String> data = new HashMap<>();
        try
        {
            for (Field f : item.getClass().getDeclaredFields())
            {
                Object value      = f.get(item);
                String fieldValue = value.toString();
                
                if (List.class.isAssignableFrom(f.getType()))
                {
                    List<String> items = (List<String>) f.get(item);
                    fieldValue = String.join(",", items);
                    
                    if (items.size() > 4)
                    {
                        fieldValue = items.stream().limit(4).collect(Collectors.joining(",")) + " " + (items.size() - 4) + " more";
                    }
                }
                
                data.put(f.getName(), fieldValue);
            }
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return data;
    }
    
    private void generateEmbed(EmbedCreateSpec embed, Map<String, String> data, String thumbnail, String url)
    {
        embed.setThumbnail(thumbnail);
        embed.setUrl(url);
        
        data.forEach((key, value) -> embed.addField(key, value, false));
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("item", "champ", "perk");
    }
    
    static class ChampionSummary
    {
        private int          id;
        private String       name;
        private String       alias;
        private String       squarePortraitPath;
        private List<String> skins;
        
        public int getId()
        {
            return id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getAlias()
        {
            return alias;
        }
        
        public String getIconPath()
        {
            return squarePortraitPath;
        }
        
        public List<String> getSkins()
        {
            return skins;
        }
    }
    
    static class SkinData
    {
        private int    id;
        private String name;
        
        public int getId()
        {
            return id;
        }
        
        public String getName()
        {
            return name;
        }
    }
    
    static class PerkData
    {
        private int          id;
        private String       name;
        private String       tooltip;
        private String       iconPath;
        private List<String> endOfGameStatDescs;
        
        public int getId()
        {
            return id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getTooltip()
        {
            return tooltip;
        }
        
        public String getIconPath()
        {
            return iconPath;
        }
        
        public List<String> getEndOfGameStatDescs()
        {
            return endOfGameStatDescs;
        }
    }
    
    static class ItemData
    {
        private int           id;
        private String        description;
        private List<Integer> from;
        private List<Integer> to;
        private int           price;
        private int           priceTotal;
        private String        iconPath;
        
        public int getId()
        {
            return id;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public List<Integer> getFrom()
        {
            return from;
        }
        
        public List<Integer> getTo()
        {
            return to;
        }
        
        public int getPrice()
        {
            return price;
        }
        
        public int getPriceTotal()
        {
            return priceTotal;
        }
        
        public String getIconPath()
        {
            return iconPath;
        }
    }
    
    static class SummonerData
    {
        private int    id;
        private String name;
        private String description;
        private int    summonerLevel;
        private int    cooldown;
        private String iconPath;
        
        public int getId()
        {
            return id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public int getSummonerLevel()
        {
            return summonerLevel;
        }
        
        public int getCooldown()
        {
            return cooldown;
        }
        
        public String getIconPath()
        {
            return iconPath;
        }
    }
}
