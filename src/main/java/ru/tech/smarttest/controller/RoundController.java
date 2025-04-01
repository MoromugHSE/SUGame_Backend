package ru.tech.smarttest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import ru.tech.smarttest.dto.QuestionSelection;
import ru.tech.smarttest.dto.Round2Selection;
import ru.tech.smarttest.dto.Round3Answer;
import ru.tech.smarttest.service.RoundService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/round")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;

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

    @PostMapping("/next-question")
    public ResponseEntity<String> nextQuestion() {
        roundService.nextQuestion();
        return ResponseEntity.ok("✅ Следующий вопрос отправлен!");
    }

    @PostMapping("/show-answer")
    public ResponseEntity<String> showCorrectAnswer() {
        roundService.revealCorrectAnswerToPlayers();
        return ResponseEntity.ok("✅ Правильный ответ показан игрокам!");
    }


    @PostMapping("/start/1/additional")
    public ResponseEntity<String> startAdditionalRoundOne() {
        roundService.startAdditionalRoundOne();
        return ResponseEntity.ok("✅ Дополнительный раунд 1 начался!");
    }


    @PostMapping("/decoder/start/1")
    public ResponseEntity<String> startDecoderOne() {
        roundService.startDecoderOne();
        return ResponseEntity.ok("✅ Дешифратор первого раунда запущен!");
    }



//    @PostMapping("/round2/directorDecision")
//    public ResponseEntity<String> directorDecision(@RequestParam boolean isCorrect) {
//        roundService.directorDecisionRound2(isCorrect);
//        return ResponseEntity.ok("Решение режиссёра принято!");
//    }


    @PostMapping("/start/2/additional")
    public ResponseEntity<String> startAdditionalRoundTwo() {
        roundService.startAdditionalRoundTwo();
        return ResponseEntity.ok("✅ Дополнительный раунд 2 начался!");
    }


    @PostMapping("/round2/start")
    public ResponseEntity<String> startSecondRound() {
        roundService.startRound2();
        return ResponseEntity.ok("✅ Второй раунд начался!");
    }



    @PostMapping("/round2/select")
    public ResponseEntity<String> handleRound2Selection(@RequestParam String playerName, @RequestParam String theme) {
        Round2Selection selection = new Round2Selection();
        selection.setSelectedPlayer(playerName);
        selection.setSelectedTheme(theme);
        roundService.processRound2Selection(selection);
        return ResponseEntity.ok("Выбор для второго раунда принят!");
    }


    @GetMapping("/round2/status")
    public ResponseEntity<Integer> statusBlitz(){
        return ResponseEntity.ok(roundService.getStatusBlitz());
    }

    @PostMapping("/round2/directorDecision")
    public ResponseEntity<String> directorDecisionRound2(
            @RequestParam boolean isCorrect,
            @RequestParam int questionIndex) {
        roundService.directorDecisionRound2(isCorrect, questionIndex);
        return ResponseEntity.ok("Решение режиссёра принято!");
    }


    @PostMapping("/decoder/start/2")
    public ResponseEntity<String> startDecoderTwo() {
        roundService.startDecoderTwo();
        return ResponseEntity.ok("✅ Дешифратор второго раунда запущен!");
    }

    @PostMapping("/round3/start")
    public ResponseEntity<String> startThirdRound() {
        roundService.startRound3();
        return ResponseEntity.ok("✅ Третий раунд начался!");
    }


    @PostMapping("/round3/directorSelectQuestion")
    public ResponseEntity<String> directorSelectQuestion(
            @RequestParam String playerName,
            @RequestParam int questionNumber
    ) {
        roundService.directorSelectQuestionForRound3(playerName, questionNumber);
        return ResponseEntity.ok("✅ Вопрос отправлен игроку.");
    }


    @GetMapping("/round3/availableQuestions")
    public ResponseEntity<List<Map<String, Object>>> getAvailableRound3Questions() {
        return ResponseEntity.ok(roundService.getAvailableRound3Questions());
    }


    @PostMapping("/round3/directorDecision")
    public ResponseEntity<String> directorDecisionRound3(@RequestParam String playerName,
                                                         @RequestParam int questionNumber,
                                                         @RequestParam boolean isCorrect) {
        roundService.directorDecisionRound3(playerName, questionNumber, isCorrect);
        return ResponseEntity.ok("Режиссёр принял решение по Раунду 3.");
    }



    @GetMapping("/round3/participants")
    public ResponseEntity<List<String>> getRound3Participants() {
        List<String> participants = roundService.getFinalPlayersRound3();
        return ResponseEntity.ok(participants);
    }

    @PostMapping("/round3/questions/by-theme")
    public ResponseEntity<Void> getRound3QuestionsByTheme(@RequestParam String theme) {
        roundService.sendRound3QuestionsByTheme(theme);
        return ResponseEntity.ok().build(); // ничего не возвращаем, только статус
    }


    @GetMapping("/round3/players/themes")
    public ResponseEntity<List<String>> getRound3PlayerThemes() {
        List<String> themes = roundService.getRound3PlayerThemes();
        return ResponseEntity.ok(themes);
    }




}

