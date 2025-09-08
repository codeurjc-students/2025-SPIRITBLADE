import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summoners")
public class SummonerController {
    public Summoner getSummoner(@PathVariable String name) {
        return new Summoner(name, 142, "Gold II", 1247);
    }
}
