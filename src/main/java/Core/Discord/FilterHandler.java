package Core.Discord;

import Core.Configuration.Config;
import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FilterHandler {
    private static final Logger log = LoggerFactory.getLogger(FilterHandler.class);
    private final ConfigLoader configLoader;

    public FilterHandler(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    public boolean shouldBeFiltered(MessageReceivedEvent event) {
        // No reactions to DMs
        if(event.getChannelType() != ChannelType.TEXT) {
            return false;
        }
        Config config = configLoader.getConfig();
        String serverName = event.getGuild().getId();
        Optional<ServerConfig> serverConfig = config.getServerConfig(serverName);
        if(serverConfig.isEmpty()) {
            return false;
        }
        if(config.getGlobalAdmins().stream().anyMatch(x -> x.equalsIgnoreCase(event.getAuthor().getId()))) {
            // Ignore global admins
            return false;
        }
        if(serverConfig.get().getFilters() == null) {
            // Skip if no filters
            return false;
        }
        if(serverConfig.get().getFilters().getRegexPatterns().stream().anyMatch(x -> x.matcher(event.getMessage().getContentDisplay().toLowerCase()).matches())) {
            // Filter pattern matches
            log.info("Filtering message '{}' by '{}' id='{}'",
                    event.getMessage().getContentDisplay().replaceAll("\n",""),
                    event.getAuthor().getName(),
                    event.getAuthor().getId());
            if(serverConfig.get().getFilters().getNotifyChannel() != null) {
                GuildChannel guildChannel = event.getJDA().getGuildChannelById(serverConfig.get().getFilters().getNotifyChannel());
                if(guildChannel instanceof TextChannel) {
                    TextChannel channel = (TextChannel) guildChannel;
                    // Remove the message
                    event.getMessage().delete().queue();
                    // Send notification to admin channel
                    channel.sendMessage(String.format("Filtered message:%n```%s```%nnUser: %s, id=%s, channel=%s",
                            event.getMessage().getContentDisplay().replaceAll("`", ""),
                            event.getAuthor().getName(),
                            event.getAuthor().getId(),
                            event.getChannel().getName())).queue();
                    log.info("Filtered message");
                }
            } else {
                log.info("Filtering message but no notification channel exists");
            }
            return true;
        }
        return false;
    }
}
