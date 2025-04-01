package ru.tech.smarttest.controller;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sound")
public class SoundController {

    private final SimpMessagingTemplate messagingTemplate;

    // Категории звуков для удобного управления
    private static final Map<String, List<String>> SOUND_CATEGORIES = Map.of(
            "ROUND1", List.of("su_1_answers.mp3", "su_1_correct.mp3", "su_1_noqs.mp3", "su_1_number_of_right_answers.mp3", "su_1_question.mp3", "su_1_round.mp3", "su_1_rules.mp3", "su_1_timer.mp3", "su_1_timer_5secs.mp3"),
            "ROUND2", List.of("su_2_correct.mp3", "su_2_nextplayer.mp3", "su_2_round.mp3", "su_2_rules.mp3", "su_2_selected.mp3", "su_2_timer.mp3"),
            "ROUND3", List.of("su_3_correct.mp3", "su_3_incorrect.mp3", "su_3_level1.mp3", "su_3_level2.mp3", "su_3_level3.mp3", "su_3_opening_cells.mp3", "su_3_rules.mp3", "su_3_timer.mp3"),
            "GENERAL", List.of("su_intro.mp3", "su_end.mp3", "su_winner.mp3", "su_comm_in.mp3", "su_comm_out.mp3", "su_end_codebreak.mp3", "su_end_extended.mp3", "su_codebreaker.mp3", "su_closing.mp3", "su_leaderboard.mp3", "su_tiebreak.mp3", "su_tiebreak_rules.mp3"),
            "PLAYERS", List.of("su_players.mp3", "su_player1.mp3", "su_player2.mp3", "su_player3.mp3", "su_player4.mp3", "su_player5.mp3", "su_player6.mp3"),
            "SPECIAL", List.of("LocationSequenceEndSting.mp3", "LocationSequenceBed.mp3")
    );

    public SoundController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getSoundCategories() {
        return ResponseEntity.ok(SOUND_CATEGORIES);
    }

    @PostMapping("/play")
    public ResponseEntity<String> playSound(@RequestParam String filename) {
        if (!isValidSoundFile(filename)) {
            return ResponseEntity.badRequest().body("Invalid sound file");
        }

        messagingTemplate.convertAndSend("/topic/sound", Map.of(
                "action", "PLAY",
                "file", "/sounds/" + filename
        ));

        return ResponseEntity.ok("Playing: " + filename);
    }


    @PostMapping("/stop")
    public ResponseEntity<String> stopSound() {
        messagingTemplate.convertAndSend("/topic/sound", Map.of(
                "action", "STOP"
        ));

        return ResponseEntity.ok("Sound stopped");
    }


    private boolean isValidSoundFile(String filename) {
        // Проверяем, что файл есть в одной из категорий
        return SOUND_CATEGORIES.values().stream()
                .anyMatch(list -> list.contains(filename));
    }

}
