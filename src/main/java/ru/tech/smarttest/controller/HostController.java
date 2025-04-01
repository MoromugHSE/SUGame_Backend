package ru.tech.smarttest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tech.smarttest.service.GameService;

import java.util.Optional;

@RestController
@RequestMapping("/host")
public class HostController {

    private final GameService gameService;

    @Autowired
    public HostController(GameService gameService) {
        this.gameService = gameService;
    }

    // Ведущий присоединяется к игре
    @PostMapping("/join")
    public ResponseEntity<String> joinHost() {
        boolean joined = gameService.addHost();
        if (joined) {
            return ResponseEntity.ok("✅ Ведущий успешно присоединился к игре.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("❌ Ведущий уже присоединён.");
        }
    }

    // Проверка текущего состояния подключения ведущего
    @GetMapping("/status")
    public ResponseEntity<String> hostStatus() {
        boolean isJoined = gameService.isHostPresent();
        return isJoined
                ? ResponseEntity.ok("🎙️ Ведущий присоединён к игре.")
                : ResponseEntity.ok("⚠️ Ведущий ещё не присоединился.");
    }

    // Ведущий отключается от игры
    @PostMapping("/leave")
    public ResponseEntity<String> leaveHost() {
        boolean left = gameService.removeHost();
        if (left) {
            return ResponseEntity.ok("👋 Ведущий успешно отключён.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("⚠️ Ведущий не был подключён.");
        }
    }
}
