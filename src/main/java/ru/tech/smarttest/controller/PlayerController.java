package ru.tech.smarttest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tech.smarttest.models.PlayerJoinStatus;
import ru.tech.smarttest.service.GameService;
import ru.tech.smarttest.service.RoundService;

import java.util.List;


@RestController
@RequestMapping("/players")
public class PlayerController {


    @Autowired
    private RoundService roundService;

    private final GameService gameService;

    @Autowired
    public PlayerController(GameService gameService) {
        this.gameService = gameService;
    }


    // Список игроков из сценария
    @GetMapping("/available")
    public ResponseEntity<List<String>> getAvailablePlayers() {
        return ResponseEntity.ok(gameService.getAvailablePlayers());
    }

    @GetMapping("/round2theme")
    public ResponseEntity<List<String>> getTheme2Round(){
        return ResponseEntity.ok(gameService.getTheme2Round());
    }



    // Подключение игрока по имени
    @PostMapping("/join")
    public ResponseEntity<String> joinPlayer(@RequestParam String playerName) {
        PlayerJoinStatus status = gameService.joinPlayer(playerName);
        switch (status) {
            case SUCCESS:
                return ResponseEntity.ok("✅ Игрок успешно подключён.");
            case ALREADY_JOINED:
                return ResponseEntity.status(HttpStatus.CONFLICT).body("⚠️ Игрок уже подключён.");
            case NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Игрок не найден.");
            case LIMIT_REACHED:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ Превышено число игроков.");
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Неизвестная ошибка.");
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leavePlayer(@RequestParam String playerName){
        boolean left = gameService.leavePlayer(playerName);
        if (left) {
            return ResponseEntity.ok("👋 Игрок "+playerName+" успешно отключён.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("⚠️ Игрок "+playerName+" не был подключён.");
        }
    }

    @PostMapping("/eliminate")
    public ResponseEntity<String> eliminatePlayer(@RequestParam String playerName) {
        roundService.eliminatePlayer(playerName);
        return ResponseEntity.ok("Игрок " + playerName + " отключен от игры.");
    }


}
