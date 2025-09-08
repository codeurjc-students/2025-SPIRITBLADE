import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RiotService {
    @Value("${riot.api.key}")
    private String apiKey;
    
    public SummonerDto getSummonerByName(String name) {
        // Implementar llamada real a Riot API
    }
}