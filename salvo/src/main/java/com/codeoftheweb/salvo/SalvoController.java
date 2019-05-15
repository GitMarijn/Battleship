package com.codeoftheweb.salvo;


import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.management.PlatformLoggingMXBean;
import java.util.*;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @RequestMapping(path = "/games", method = RequestMethod.GET)
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> currentUser = new LinkedHashMap<>();
        if (authentication != null) {
            currentUser.put("player", getPlayerInfo(playerRepository.findByUserName(authentication.getName())));
            currentUser.put("games", gameRepository.findAll()
                    .stream()
                    .map(this::getGameInfo)
                    .collect(toList()));
        } else {
            currentUser.put("games", gameRepository.findAll()
                    .stream()
                    .map(this::getGameInfo)
                    .collect(toList()));
        }
        return currentUser;
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (authentication == null)
            return new ResponseEntity<>(makeMap("error", "Log in"), HttpStatus.UNAUTHORIZED);
        Player currentUser = playerRepository.findByUserName(authentication.getName());
        if (currentUser == null) {
            return new ResponseEntity<>(makeMap("error", "Player unknown"), HttpStatus.UNAUTHORIZED);
        }
        Game newGame = new Game(new Date());
        gameRepository.save(newGame);
        GamePlayer newGamePlayer = new GamePlayer(newGame, currentUser, new Date());
        gamePlayerRepository.save(newGamePlayer);
        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    private Map<String, Object> getGameInfo(Game game) {
        Map<String, Object> gameInfo = new LinkedHashMap<>();
        gameInfo.put("id", game.getId());
        gameInfo.put("created", game.getCreationDate());
        gameInfo.put("gamePlayers", game.getGamePlayers().stream().map(this::getGamePlayerInfo).collect(toList()));
        return gameInfo;
    }

    private Map<String, Object> getGamePlayerInfo(GamePlayer gamePlayer) {
        Map<String, Object> gamePlayerInfo = new LinkedHashMap<>();
        gamePlayerInfo.put("id", gamePlayer.getId());
        gamePlayerInfo.put("player", getPlayerInfo(gamePlayer.getPlayer()));
        return gamePlayerInfo;
    }

    private Map<String, Object> getPlayerInfo(Player player) {
        Map<String, Object> playerInfo = new LinkedHashMap<>();
        playerInfo.put("id", player.getId());
        playerInfo.put("email", player.getUserName());
        return playerInfo;
    }

    private Map<String, Object> getShipInfo(Ship ship) {
        Map<String, Object> shipInfo = new LinkedHashMap<>();
        shipInfo.put("type", ship.getType());
        shipInfo.put("locations", ship.getLocations());
        return shipInfo;
    }

    private Map<String, Object> getSalvoInfo(Salvo salvo) {
        Map<String, Object> salvoInfo = new LinkedHashMap<>();
        salvoInfo.put("turn", salvo.getTurn());
        salvoInfo.put("salvoLocations", salvo.getSalvoLocations());
        return salvoInfo;
    }

    private Map<String, Object> getScoreInfo(Player player) {
        Map<String, Object> scoreInfo = new LinkedHashMap<>();
        scoreInfo.put("player", player.getUserName());
        scoreInfo.put("total", player.getScores().stream().mapToDouble(score -> score.getScore()).sum());
        scoreInfo.put("wins", player.getScores().stream().filter(score -> score.getScore() == 1.0).count());
        scoreInfo.put("losses", player.getScores().stream().filter(score -> score.getScore() == 0.0).count());
        scoreInfo.put("ties", player.getScores().stream().filter(score -> score.getScore() == 0.5).count());
        return scoreInfo;
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGamePlayers(@PathVariable Long id, Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.getOne(id);
        if (gamePlayer.getPlayer() == playerRepository.findByUserName(authentication.getName())) {
            Map<String, Object> gameView = new LinkedHashMap<>();
            gameView.put("games", getGameInfo(gamePlayer.getGame()));
            gameView.put("ships", gamePlayer.getShips().stream().map(this::getShipInfo).collect(toList()));
            gameView.put("salvoes", gamePlayer.getSalvoes().stream().map(this::getSalvoInfo).collect(toList()));
            GamePlayer enemy = getEnemy(gamePlayer);
            if (enemy != null) {
                sunkShips(gamePlayer);
                sunkShips(enemy);
                updateScoreBoard(gamePlayer);
                gameView.put("enemy_salvoes", enemy.getSalvoes().stream().map(this::getSalvoInfo).collect(toList()));
                gameView.put("hit_on_enemy", allHitLocations(gamePlayer));
                gameView.put("hit_on_user", allHitLocations(enemy));
                gameView.put("enemy_sunk_ships", allSinkLocations(gamePlayer));
                gameView.put("user_sunk_ships", allSinkLocations(enemy));
                gameView.put("game_state", gameState(gamePlayer));
            }
            return new ResponseEntity<>(makeMap("gameview", gameView), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(makeMap("error", "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }
    }

    private GamePlayer getEnemy(GamePlayer gamePlayer) {
        GamePlayer enemy = null;
        for (GamePlayer gp : gamePlayer.getGame().getGamePlayers()) {
            if (gamePlayer.getId() != gp.getId()) {
                enemy = gp;
            }
        }
        return enemy;
    }

    @RequestMapping("/scoreboard")
    public List<Object> getPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(player -> getScoreInfo(player))
                .collect(toList());
    }

//    @RequestMapping(path = "/scoreboard", method = RequestMethod.GET)
//    public Map<String, Object> getPlayers(GamePlayer gamePlayer) {
//        Map<String, Object> scoreboard = new LinkedHashMap<>();
//        scoreboard.put("gamestate", gameState(gamePlayer));
//        scoreboard.put("scores", playerRepository.findAll().stream().map(player -> getScoreInfo(player)).collect(toList()));
//        return scoreboard;
//    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String userName, String password) {
        if (userName.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No name given"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByUserName(userName);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.CONFLICT);
        }
        Player newPlayer = playerRepository.save(new Player(userName, password));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    @RequestMapping(path = "/game/{id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long id, Authentication authentication) {
        if (authentication == null)
            return new ResponseEntity<>(makeMap("error", "Log in"), HttpStatus.UNAUTHORIZED);
        Player currentUser = playerRepository.findByUserName(authentication.getName());
        if (currentUser == null) {
            return new ResponseEntity<>(makeMap("error", "Player unknown"), HttpStatus.UNAUTHORIZED);
        }
        Game joinGame = gameRepository.findOne(id);
        if (gameRepository.findOne(id) == null) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (joinGame.getGamePlayers().size() == 2) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }
        GamePlayer newGamePlayer = new GamePlayer(joinGame, currentUser, new Date());
        gamePlayerRepository.save(newGamePlayer);
        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{id}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShips(@PathVariable long id, @RequestBody List<Ship> ships, Authentication authentication) {
        if (authentication == null)
            return new ResponseEntity<>(makeMap("error", "Log in"), HttpStatus.UNAUTHORIZED);
        Player currentUser = playerRepository.findByUserName(authentication.getName());
        if (currentUser == null) {
            return new ResponseEntity<>(makeMap("error", "Player unknown"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findOne(id);
        if (gamePlayerRepository.findOne(id) == null) {
            return new ResponseEntity<>(makeMap("error", "No such game player"), HttpStatus.UNAUTHORIZED);
        }
        if (currentUser.getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Not your game"), HttpStatus.UNAUTHORIZED);
        }

        if (!gamePlayer.getShips().isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Ships already placed"), HttpStatus.FORBIDDEN);
        } else {
            for (Ship ship : ships) {
                ship.setGamePlayer(gamePlayer);
                shipRepository.save(ship);
            }
            return new ResponseEntity<>(makeMap("success", "Ships placed"), HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games/players/{id}/ships", method = RequestMethod.GET)
    public List<Object> getShips(@PathVariable long id) {
        GamePlayer gamePlayer = gamePlayerRepository.findOne(id);
        return gamePlayer.getShips().stream().map(ship -> getShipInfo(ship)).collect(toList());
    }

    @RequestMapping(path = "/games/players/{id}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> fireSalvoes(@PathVariable long id, @RequestBody Salvo salvo, Authentication authentication) {
        if (authentication == null)
            return new ResponseEntity<>(makeMap("error", "Log in"), HttpStatus.UNAUTHORIZED);
        Player currentUser = playerRepository.findByUserName(authentication.getName());
        if (currentUser == null) {
            return new ResponseEntity<>(makeMap("error", "Player unknown"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findOne(id);
        if (gamePlayerRepository.findOne(id) == null) {
            return new ResponseEntity<>(makeMap("error", "No such game player"), HttpStatus.UNAUTHORIZED);
        }
        if (currentUser.getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Not your game"), HttpStatus.UNAUTHORIZED);
        } else {
            salvo.setGamePlayer(gamePlayer);
            salvo.setTurn(getLastTurn(gamePlayer) + 1);
            salvoRepository.save(salvo);
        }
        return new ResponseEntity<>(makeMap("success", "Salvoes fired"), HttpStatus.CREATED);
    }

    @RequestMapping(path = "api/games/players/{id}/salvoes", method = RequestMethod.GET)
    public List<Object> getSalvoes(@PathVariable long id) {
        GamePlayer gamePlayer = gamePlayerRepository.findOne(id);
        return gamePlayer.getSalvoes().stream().map(salvo -> getSalvoInfo(salvo)).collect(toList());
    }

    private int getLastTurn(GamePlayer gamePlayer) {
        return gamePlayer.getSalvoes().size();
    }

    private List<String> allShipLocations(GamePlayer gamePlayer) {
        List<String> allLocations = new ArrayList<>();
        Set<Ship> ships = gamePlayer.getShips();
        for (Ship ship : ships) {
            List<String> eachLocation = ship.getLocations();
            String type = ship.getType();
            allLocations.add(type);
            for (String location : eachLocation) {
                allLocations.add(location);
            }
        }
        return allLocations;
    }

    private List<String> allSalvoLocations(GamePlayer gamePlayer) {
        List<String> allSalvoes = new ArrayList<>();
        Set<Salvo> salvoes = gamePlayer.getSalvoes();
        for (Salvo salvo : salvoes) {
            List<String> eachLocation = salvo.getSalvoLocations();
            for (String location : eachLocation) {
                allSalvoes.add(location);
            }
        }
        return allSalvoes;
    }

    private List<String> allHitLocations(GamePlayer gamePlayer) {
        List<String> allHits = new ArrayList<>();
        GamePlayer enemy = getEnemy(gamePlayer);
        List<String> salvoes = allSalvoLocations(gamePlayer);
        List<String> ships = allShipLocations(enemy);
        for (String salvo : salvoes) {
            if (ships.contains(salvo))
                allHits.add(salvo);
        }
        return allHits;
    }

    private List<Object> allSinkLocations(GamePlayer gamePlayer) {
        List<Object> allSinks = new ArrayList<>();
        for (Ship ship : gamePlayer.getShips()) {
            if (ship.isSunk() == true)
                allSinks.add(getShipInfo(ship));
        }
        return allSinks;
    }

    private void sunkShips(GamePlayer gamePlayer) {
        Set<Ship> ships = gamePlayer.getShips();
        List<String> hits = allHitLocations(getEnemy(gamePlayer));
        for (Ship ship : ships) {
            List<String> locations = ship.getLocations();
            List<String> hitLocations = new ArrayList<>();
            for (String location : locations) {
                if (hits.contains(location))
                    hitLocations.add(location);
            }
            if (locations.stream().allMatch(location -> hits.contains(location))) {
                ship.setSunk(true);
                shipRepository.save(ship);
            }
        }
    }

    private boolean gameOver(GamePlayer gamePlayer) {
        boolean gameover = false;
        GamePlayer enemy = getEnemy(gamePlayer);
        if (getLastTurn(gamePlayer) == getLastTurn(enemy) && allSinkLocations(gamePlayer).size() == 5) {
            gameover = true;
        }
        return gameover;
    }

    private boolean gameTied(GamePlayer gamePlayer) {
        boolean tie = false;
        GamePlayer enemy = getEnemy(gamePlayer);
        if (getLastTurn(gamePlayer) == getLastTurn(enemy) && allSinkLocations(gamePlayer).size() == 5 && allSinkLocations(enemy).size() == 5) {
            tie = true;
        }
        return tie;
    }

    private String gameState(GamePlayer gamePlayer) {
        String state = new String();
        GamePlayer enemy = getEnemy(gamePlayer);

        if (gamePlayer.getShips().isEmpty()){
            return "Place your ships.";
        }
        else if (enemy.getShips().isEmpty()) {
            return "Please wait for opponent to place ships.";
        }
        else if (gameTied(gamePlayer) == true) {
            return "It is a tie!";
        }
        else if (gameOver(gamePlayer) == true) {
            return "Game over!";
        }
        else if (gameOver(enemy) == true) {
            return "You have won the game!";
        }
        else if (gamePlayer.getShips().size() == 5 && getLastTurn(gamePlayer) == (getLastTurn(enemy) + 1)) {
            return "Please wait for opponent to fire salvoes.";
        }
        else if (gamePlayer.getShips().size() == 5) {
            return "Fire your salvoes!";
        }
        return state;
    }

    private void updateScoreBoard(GamePlayer gamePlayer) {
        GamePlayer enemy = getEnemy(gamePlayer);
        if (gameTied(gamePlayer) == true && gamePlayer.getScore() == null) {
            Score score = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), 0.5, new Date());
            scoreRepository.save(score);
        }
        else if (gameOver(gamePlayer) == true && gamePlayer.getScore() == null) {
            Score score = new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), 0.0, new Date());
            Score score1 = new Score(enemy.getGame(), enemy.getPlayer(), 1.0, new Date());
            scoreRepository.save(score);
            scoreRepository.save(score1);
        }
    }
}
