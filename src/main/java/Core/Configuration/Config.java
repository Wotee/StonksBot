package Core.Configuration;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Setter
@NoArgsConstructor
public class Config {
    private String oauth;
    private FeatureConfig omxhNews;
    private FeatureConfig shareville;
    private List<String> globalAdmins;
    private List<ServerConfig> servers;

    public Optional<ServerConfig> getServerConfig(String id) {
        if(servers == null) {
            return Optional.empty();
        }
        return servers.stream().filter(x -> x.getName().trim().equalsIgnoreCase(id)).findFirst();
    }

    public String getOauth() { return oauth; }

    public FeatureConfig getOmxhNews() { return omxhNews; }

    public List<String> getGlobalAdmins() { return Optional.ofNullable(globalAdmins).orElseGet(LinkedList::new); }

    public List<ServerConfig> getServers() { return Optional.ofNullable(servers).orElseGet(LinkedList::new); }

    public FeatureConfig getShareville() { return Optional.ofNullable(shareville).orElseGet(()->new FeatureConfig("false")); }
}
