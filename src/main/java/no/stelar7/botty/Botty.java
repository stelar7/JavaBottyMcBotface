package no.stelar7.botty;

import discord4j.core.*;
import no.stelar7.botty.listener.EventListener;
import no.stelar7.botty.utils.SecretFile;
import org.slf4j.*;

public class Botty
{
    public static Logger logger = LoggerFactory.getLogger(Botty.class);
    
    public static void main(String[] args)
    {
        new Botty();
    }
    
    public Botty()
    {
        GatewayDiscordClient client = DiscordClientBuilder.create(SecretFile.BOT_TOKEN).build().login().block();
        logger.info("Registering events");
        EventListener.initListeners(client);
        logger.info("All events registered");
        client.onDisconnect().block();
    }
}
