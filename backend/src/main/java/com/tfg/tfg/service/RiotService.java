@Service
public class RiotService {
    @Value("${riot.api.key}")
    private String apiKey;
    
    public SummonerDto getSummonerByName(String name) {
        // Implementar llamada real a Riot API
    }
}