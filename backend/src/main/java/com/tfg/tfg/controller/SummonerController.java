public class SummonerController {
    @RestController
    @RequestMapping("/api/summoners")
    public class SummonerController {
        @GetMapping("/{name}")
        public Summoner getSummoner(@PathVariable String name) {
            return new Summoner(name, 142, "Gold II", 1247);
        }
    }
}
