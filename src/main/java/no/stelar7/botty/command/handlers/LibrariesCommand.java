package no.stelar7.botty.command.handlers;

import com.google.gson.*;
import no.stelar7.botty.command.*;
import no.stelar7.botty.utils.SecretFile;
import org.kohsuke.github.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LibrariesCommand extends Command
{
    private static GitHub                         client;
    private final  Map<String, List<LibraryInfo>> languages = new ConcurrentHashMap<>();
    public static  Logger                         logger    = LoggerFactory.getLogger(LibrariesCommand.class);
    
    public LibrariesCommand()
    {
        try
        {
            this.setDisabled(true);
            client = GitHub.connect("stelar7", SecretFile.GH_TOKEN);
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::updateLibraryList, 0, 1, TimeUnit.DAYS);
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public void updateLibraryList()
    {
        try
        {
            if (this.isDisabled())
            {
                return;
            }
            
            GHRepository    repository = client.getRepository("WxWatch/riot-api-libraries");
            List<GHContent> libraries  = repository.getDirectoryContent("libraries");
            ExecutorService service    = Executors.newFixedThreadPool(libraries.size());
            for (GHContent language : libraries)
            {
                service.submit(() -> {
                    try
                    {
                        List<LibraryInfo> infos       = new ArrayList<>();
                        List<GHContent>   libraryList = repository.getDirectoryContent(language.getPath());
                        for (GHContent lib : libraryList)
                        {
                            GHContent   fileContent = repository.getFileContent(lib.getPath());
                            String      content     = new BufferedReader(new InputStreamReader(fileContent.read())).lines().collect(Collectors.joining("\n"));
                            LibraryInfo info        = LibraryInfo.from(content);
                            if (info != null)
                            {
                                infos.add(info);
                            }
                        }
                        
                        if (infos.size() > 0)
                        {
                            languages.put(language.getName(), infos);
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
            
            service.shutdown();
            service.awaitTermination(1, TimeUnit.DAYS);
            
            logger.info("Library list updated");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        if (params.getParameters().size() == 0)
        {
            String prefix  = "I have libraries for one of the following languages: ";
            String liblist = "`" + String.join(", ", this.languages.keySet()) + "`\n";
            String postfix = "Choose one you like and reply with !libs <your language>.";
            
            params.getMessage().getChannel().block()
                  .createMessage(prefix + liblist + postfix).block();
            
            return;
        }
        
        String            selected  = params.getParameters().get(0);
        List<LibraryInfo> libraries = languages.getOrDefault(selected, new ArrayList<>());
        
        if (libraries.size() == 0)
        {
            params.getMessage().getChannel().block()
                  .createMessage("Unable to find any libraries for language: " + selected).block();
            
            return;
        }
        
        libraries.sort(Comparator.comparing(LibraryInfo::getStars));
        params.getMessage().getChannel().block().createEmbed(embed -> {
            for (LibraryInfo info : libraries)
            {
                embed.addField(
                        info.name + " (★ " + info.stars + ")",
                        info.description + "\n" + info.getFormattedLinks(),
                        false
                              );
            }
        }).block();
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("libs", "lib", "library");
    }
    
    @Override
    public boolean isDisabled()
    {
        return true;
    }
    
    private static class LibraryInfo
    {
        private final String              name;
        private final String              owner;
        private final String              description;
        private final String              url;
        private final int                 stars;
        private final Map<String, String> links;
        
        public LibraryInfo(String name, String owner, String description, String url, int stars, Map<String, String> links)
        {
            this.name = name;
            this.owner = owner;
            this.description = description;
            this.url = url;
            this.stars = stars;
            this.links = links;
        }
        
        public static LibraryInfo from(String content) throws IOException
        {
            JsonObject element = JsonParser.parseString(content).getAsJsonObject();
            
            List<String> requiredFields = List.of("owner", "repo", "description");
            for (String requiredField : requiredFields)
            {
                if (!element.has(requiredField))
                {
                    return null;
                }
            }
            
            String owner       = element.get("owner").getAsString();
            String name        = element.get("repo").getAsString();
            String description = element.get("description").getAsString();
            
            String              mainRepo = owner + "/" + name;
            Map<String, String> links    = new HashMap<>();
            
            if (element.has("links"))
            {
                JsonArray linkData = element.get("links").getAsJsonArray();
                for (JsonElement link : linkData)
                {
                    JsonObject linkObj  = link.getAsJsonObject();
                    String     linkName = linkObj.get("name").getAsString();
                    String     linkUrl  = linkObj.get("url").getAsString();
                    links.put(linkName, linkUrl);
                }
            }
            
            try
            {
                GHRepository repo = client.getRepository(mainRepo);
                
                String url   = repo.getHtmlUrl().toExternalForm();
                int    stars = repo.getStargazersCount();
                
                return new LibraryInfo(name, owner, description, url, stars, links);
            } catch (GHFileNotFoundException exception)
            {
                logger.info("Unable to load repository for entry: " + mainRepo);
                return null;
            }
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getOwner()
        {
            return owner;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public String getUrl()
        {
            return url;
        }
        
        public int getStars()
        {
            return stars;
        }
        
        public Map<String, String> getLinks()
        {
            return links;
        }
        
        public String getFormattedLinks()
        {
            return links.entrySet()
                        .stream()
                        .map(e -> "[" + e.getKey() + "](" + e.getValue() + ")")
                        .collect(Collectors.joining(", "));
        }
    }
}
