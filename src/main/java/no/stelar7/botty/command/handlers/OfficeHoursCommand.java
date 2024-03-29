package no.stelar7.botty.command.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.*;
import no.stelar7.botty.command.*;
import no.stelar7.botty.database.*;
import no.stelar7.botty.utils.*;

import java.util.*;

public class OfficeHoursCommand extends Command
{
    public static OfficeHoursConnection database = new OfficeHoursConnection();
    
    public OfficeHoursCommand()
    {
        this.setAdminOnly(true);
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        TextChannel officeHoursChannel = (TextChannel) params.getMessage().getGuild().block()
                                                             .getChannelById(Snowflake.of(SettingsConnection.INSTANCE.getOfficeHoursChannel())).block();
        
        Role                everyone    = params.getMessage().getGuild().block().getEveryoneRole().block();
        PermissionSet       change      = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionOverwrite permissions = PermissionUtil.getOverrideFor(officeHoursChannel, everyone);
        boolean             isOpen      = PermissionUtil.isAllowed(permissions, change);
        
        if (List.of("open", "close").contains(params.getCommand().toLowerCase()))
        {
            handleOpenClose(params, officeHoursChannel, everyone, permissions, change);
            return;
        }
        
        if (params.getCommand().equalsIgnoreCase("ask"))
        {
            handleAsk(params, officeHoursChannel, isOpen);
        }
        
        if (params.getCommand().equalsIgnoreCase("unask"))
        {
            handleUnask(params);
        }
    }
    
    private void handleUnask(CommandParameters params)
    {
        User                author         = params.getMessage().getAuthor().get();
        String              id             = params.getParameters().get(0);
        OfficeHoursQuestion question       = database.getQuestion(id);
        List<Snowflake>     adminRoles     = database.getAdminRoles();
        Member              authorAsMember = author.asMember(params.getMessage().getGuild().block().getId()).block();
        
        if (question.getAuthor().equalsIgnoreCase(author.getId().asString()) ||
            RoleUtils.hasAnyRoles(authorAsMember, adminRoles))
        {
            database.removeQuestion(id);
            params.getMessage().getChannel().block().createMessage("Question has been removed");
        }
    }
    
    private void handleAsk(CommandParameters params, TextChannel officeHoursChannel, boolean isOpen)
    {
        User   author   = params.getMessage().getAuthor().get();
        String question = String.join(" ", params.getParameters());
        
        if (isOpen)
        {
            if (params.getMessage().getChannel().block().equals(officeHoursChannel))
            {
                return;
            }
    
            officeHoursChannel.createMessage(MentionUtil.user(author) + " " + question);
            params.getMessage().getChannel().block().createMessage("Question posted to " + MentionUtil.channel(officeHoursChannel) + " because its open");
            
        } else
        {
            OfficeHoursQuestion ohQuestion = database.addQuestion(params.getMessage().getAuthor().get(), question);
            params.getMessage().getChannel().block().createMessage("Your question was saved to the database with id " + ohQuestion.getId());
        }
    }
    
    private void handleOpenClose(CommandParameters params, TextChannel officeHoursChannel, Role everyone, PermissionOverwrite permissions, PermissionSet change)
    {
        if (params.getCommand().equalsIgnoreCase("open"))
        {
            database.getUnaskedQuestions().forEach(question -> {
                officeHoursChannel.createMessage(MentionUtil.user(question.getAuthor()) + " " + question.getQuestion());
                database.setAsked(question);
            });
            
            permissions = PermissionUtil.allow(everyone, permissions, change);
            officeHoursChannel.createMessage("open").block();
        }
        
        if (params.getCommand().equalsIgnoreCase("close"))
        {
            permissions = PermissionUtil.deny(everyone, permissions, change);
            officeHoursChannel.createMessage("close").block();
        }
    
        officeHoursChannel.addRoleOverwrite(everyone.getId(), permissions).block();
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("open", "close", "ask", "unask");
    }
    
    public static class OfficeHoursQuestion
    {
        private final String  id;
        private final String  author;
        private final String  question;
        private final boolean asked;
        
        public OfficeHoursQuestion(String id, String author, String question, boolean asked)
        {
            this.id = id;
            this.author = author;
            this.question = question;
            this.asked = asked;
        }
        
        public String getId()
        {
            return id;
        }
        
        public String getAuthor()
        {
            return author;
        }
        
        public String getQuestion()
        {
            return question;
        }
        
        public boolean isAsked()
        {
            return asked;
        }
    }
}
