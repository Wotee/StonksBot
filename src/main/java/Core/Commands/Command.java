package Core.Commands;

import Core.Configuration.CommandConfig;
import Core.Configuration.Config;
import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Command class defines behaviour for a single command
 * @author etsubu
 * @version 26 Jul 2018
 *
 */
public abstract class Command {
    /**
     * Contains the name of the command that follows prefix
     */
    protected final List<String> names;
    protected final ConfigLoader configLoader;
    protected boolean allowByDefault;
    
    /**
     * Initializes Command
     * @param name Command name that follows the prefix
     */
    public Command(String name, ConfigLoader configLoader, boolean allowByDefault) {
        this.configLoader = configLoader;
        this.allowByDefault = allowByDefault;
        names = List.of(name.trim().toLowerCase().replaceAll("!", ""));
    }

    public Command(List<String> names, ConfigLoader configLoader, boolean allowByDefault) {
        this.configLoader = configLoader;
        this.allowByDefault = allowByDefault;
        this.names = Collections.unmodifiableList(names);
    }

    public boolean isUserAllowed(User user, Guild server, List<Role> groups) {
        Config config = configLoader.getConfig();
        Optional<List<String>> globalAdmins = Optional.ofNullable(config.getGlobalAdmins());
        if(globalAdmins.isPresent() && globalAdmins.get().stream().anyMatch(x -> x.equalsIgnoreCase(user.getId()))) {
            // Allow global admins to use any command
            return true;
        }
        if(server == null || groups == null || groups.isEmpty()) {
            return allowByDefault;
        }
        Optional<ServerConfig> serverConfig = config.getServerConfig(server.getId());
        if(serverConfig.isPresent()) {
            Optional<Map<String, CommandConfig>> commandConfigs = Optional.ofNullable(serverConfig.get().getCommands());
            if(commandConfigs.isPresent()) {
                for(String name : names) {
                    CommandConfig commandConfig;
                    if((commandConfig = commandConfigs.get().get(name)) != null) {
                        Optional<List<String>> allowedGroups = Optional.ofNullable(commandConfig.getAllowedGroups());
                        if(allowedGroups.isPresent()) {
                            for(Role group : groups) {
                                if(allowedGroups.get().stream().anyMatch(x -> x.equalsIgnoreCase(group.getId()))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return allowByDefault;
    }
    
    /**
     * 
     * @return Name of the command
     */
    public List<String> getNames() {
        return names;
    }
    
    /**
     * Defines command execution
     * @param command Command the user typed
     * @return CommandResult containing the result
     */
    public CommandResult execute(String command, User user, List<Role> groups, Guild server) {
        if(isUserAllowed(user, server, groups)) {
            return exec(command);
        }
        return new CommandResult("You lack permissions to use this command", false);
    }

    public abstract CommandResult exec(String command);

    /**
     * @return Help texts telling the user how to use the command
     */
    public abstract String help();
}