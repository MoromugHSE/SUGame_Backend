package ru.tech.smarttest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tech.smarttest.service.RoundService;

import java.util.Map;

@RestController
@RequestMapping("/director")
public class DirectorController {

    @Autowired
    private RoundService roundService;


    @PostMapping("/round1/start")
    public ResponseEntity<String> startRoundOne() {
        roundService.startRound1();
        return ResponseEntity.ok("✅ Первый раунд начался!");
    }


    @PostMapping("/round1/stop")
    public ResponseEntity<String> stopRound1() {
        roundService.stopRound1();
        return ResponseEntity.ok("✅ Первый раунд остановлен режиссёром!");
    }

    @PostMapping("/round1/nextQuestion")
    public ResponseEntity<String> nextQuestion() {
        roundService.nextQuestion();
        return ResponseEntity.ok("✅ Следующий вопрос отправлен!");
    }

    @PostMapping("/round1/showAnswer")
    public ResponseEntity<String> showCorrectAnswer() {
        roundService.revealCorrectAnswerToPlayers();
        return ResponseEntity.ok("✅ Правильный ответ показан игрокам!");
    }



    // Пример эндпоинта для получения результатов первого раунда
    @PostMapping("/round1/results")
    public ResponseEntity<Map<String, Integer>> round1Results() {
        return ResponseEntity.ok(roundService.getRound1Results());
    }

    // Эндпоинт для доп. раунда 1
    @PostMapping("/round1/additional/results")
    public ResponseEntity<Map<String, Object>> additionalRound1Results() {
        return ResponseEntity.ok(roundService.getAdditionalRound1Results());
    }

    // Эндпоинт для дешифратора 1
    @PostMapping("/decoder1/results")
    public ResponseEntity<Map<String, Object>> decoder1Results() {
        return ResponseEntity.ok(roundService.getDecoder1Results());
    }

    // Эндпоинт для второго раунда
    @PostMapping("/round2/results")
    public ResponseEntity<Map<String, Object>> round2Results() {
        return ResponseEntity.ok(roundService.getRound2Results());
    }

    // Эндпоинт для доп. раунда 2
    @PostMapping("/round2/additional/results")
    public ResponseEntity<Map<String, Object>> additionalRound2Results() {
        return ResponseEntity.ok(roundService.getAdditionalRound2Results());
    }

    // Эндпоинт для дешифратора 2
    @PostMapping("/decoder2/results")
    public ResponseEntity<Map<String, Object>> decoder2Results() {
        return ResponseEntity.ok(roundService.getDecoder2Results());
    }

    // Эндпоинт для третьего раунда
    @PostMapping("/round3/results")
    public ResponseEntity<Map<String, Object>> round3Results() {
        return ResponseEntity.ok(roundService.getRound3Results());
    }

    // Эндпоинт для общего результата
    @PostMapping("/overall/results")
    public ResponseEntity<Map<String, Object>> overallResults() {
        return ResponseEntity.ok(roundService.getOverallResults());
    }
}
