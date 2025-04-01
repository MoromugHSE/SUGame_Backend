package ru.tech.smarttest.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.tech.smarttest.dto.*;
import ru.tech.smarttest.models.Scenario;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final ScenarioService scenarioService;
    private final SimpMessagingTemplate messagingTemplate;
    private long decoderStartTime; // момент запуска дешифратора
    private final ScoreService scoreService;



    private final Map<String, Long> decoderAnswerTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> playerScores = new ConcurrentHashMap<>();
    private final Map<String, String> playerAnswers = new ConcurrentHashMap<>();
    private List<Scenario.Question> questionsRound1;
    private int currentQuestionIndex = -1;


    private List<String> playerOrderRound2; // Порядок игроков, определяется по дешифратору
    private int currentPlayerIndexRound2 = -1;
    private List<Scenario.SimpleQuestion> questionsRound2;
    private int currentQuestionIndexRound2 = -1;



    private List<Scenario.SimpleQuestion> blitzQuestionsRound2;
    private int currentBlitzQuestionIndexRound2 = 0;
    private String selectedRound2Player;

    private Map<String, List<Scenario.SimpleQuestion>> playerRound3Questions = new HashMap<>();
    private Map<String, Integer> countRoundCheck = new HashMap<>();
    private List<String> playersRound3Order = new ArrayList<>();
    private int currentRound3PlayerIndex = -1;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private long decoder2StartTime;
    private final Map<String, Long> decoder2AnswerTimes = new ConcurrentHashMap<>();


    @Autowired
    private GameService gameService;

    private volatile boolean isRound1Active = false;


    private ScheduledFuture<?> round2TimerTask;


    // Поле для глобального таймера блitz второго раунда
    private ScheduledFuture<?> round2BlitzTimerTask;


    private Map<String, String> playerColorsRound3 = new HashMap<>(); // имя игрока -> цвет
    private List<String> finalPlayersRound3; // топ-3 игрока
    private Map<String, String> playerThemesRound3 = new HashMap<>(); // имя игрока -> тема


    private final Map<String, Integer> round1Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round1ADDScores = new ConcurrentHashMap<>();
    private final Map<String, Long> decoder1Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round2Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round2ADDScores = new ConcurrentHashMap<>();
    private final Map<String, Long> decoder2Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round3Scores = new ConcurrentHashMap<>();


    private Map<String, Integer> finalRoundScores = new ConcurrentHashMap<>();
    private Map<String, List<Scenario.SimpleQuestion>> themesQuestionsRound3 = new HashMap<>();
    private List<Scenario.SimpleQuestion> neutralQuestionsRound3 = new ArrayList<>();
    private int currentQuestionIndexRound3 = 0;


    private Map<Integer, String> questionColors = new HashMap<>(); // Номер вопроса (1-36) -> цвет
    private Map<Integer, String> questionThemes = new HashMap<>(); // Номер вопроса (1-36) -> тема
    private Map<Integer, String> questionTexts = new HashMap<>(); // Номер вопроса (1-36) -> текст вопроса
    private Map<Integer, String> questionAnswers = new HashMap<>(); // Номер вопроса (1-36) -> ответ
    private Set<Integer> availableQuestions = new HashSet<>();
    private int currentQuestionNumber;
    private String currentAnsweringPlayer;;
    private final Map<String, Integer> totalScores = new ConcurrentHashMap<>();
    private Integer statusBlitz = 0;



    // Храним задачу таймера для возможности отмены
    private ScheduledFuture<?> round3TimerTask;




    public void startRound1() {
        isRound1Active = true;
        Scenario scenario = scenarioService.getCurrentScenario();
        questionsRound1 = scenario.getRound1();
        currentQuestionIndex = 0;

        playerAnswers.clear();
        sendCurrentQuestionToPlayers();
    }


    public void stopRound1() {
        isRound1Active = false;
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND1_STOPPED"
        ));
    }


    public void nextQuestion() {
        if (questionsRound1 == null || currentQuestionIndex + 1 >= questionsRound1.size()) {
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "ROUND_FINISHED",
                    "payload", "Вопросы первого раунда закончились!"
            ));
            return;
        }

        currentQuestionIndex++;
        playerAnswers.clear();
        sendCurrentQuestionToPlayers();
    }

    String vopr = null;

    private void sendCurrentQuestionToPlayers() {
        if (!isRound1Active) return; // если раунд остановлен, больше вопросы не отправляем
        Scenario.Question currentQuestion = questionsRound1.get(currentQuestionIndex);
        vopr = currentQuestion.getAnswer();
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "QUESTION",
                "question", currentQuestion.getText(),
                "choices", currentQuestion.getChoices(),
                "answer", currentQuestion.getAnswer(),
                "comment", currentQuestion.getComment()
        ));

        int roundTime = scenarioService.getCurrentScenario().getRules().getTimeRound1();
        scheduler.schedule(this::finishCurrentQuestion, roundTime, TimeUnit.SECONDS);
    }

    private void finishCurrentQuestion() {
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "QUESTION_ENDED",
                "questionIndex", currentQuestionIndex
        ));
    }

    public void processPlayerAnswer(PlayerAnswer playerAnswer) {
        String playerName = playerAnswer.getPlayerName();


        // Проверяем, ответил ли уже игрок на текущий вопрос
        if (playerAnswers.containsKey(playerName)) {
            // Сообщаем игроку, что повторный ответ не принимается
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors",
                    Map.of("message", "Вы уже ответили на этот вопрос."));
            return;
        }



        String answer = playerAnswer.getAnswer();
        Scenario.Question currentQuestion = questionsRound1.get(currentQuestionIndex);


        System.out.println("LOOOOOOOOOOOOOOGGGGGGGG :" + vopr+ " TVETUSER : "+ answer+" USSSSSSSSSSSSSSSSSSSSSSSSEEER: "+ playerName);
        System.out.println("LEEEEEEEEEEEEEEEEEEEEEEEEED :  " + currentQuestion.getAnswer());

        playerAnswers.put(playerName, answer);


        if (currentQuestion.getAnswer().equals(answer)) {
            scoreService.addScore("round1", playerName, 1); // Заменяем playerScores.put
        }


        if (currentQuestion.getAnswer().equals(answer)) {
            countRoundCheck.put(playerName, countRoundCheck.getOrDefault(playerName, 0) + 1);
        } else {
            // Добавляем игрока с 0 очков, если его ещё нет
            countRoundCheck.putIfAbsent(playerName, 0);
        }

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "type", "PLAYER_ANSWER",
                "payload", playerAnswers
        ));
    }

    public void revealCorrectAnswerToPlayers() {
        Scenario.Question currentQuestion = questionsRound1.get(currentQuestionIndex);
        Map<String, String> payload = Map.of(
                "correctAnswer", currentQuestion.getAnswer(),
                "comment", currentQuestion.getComment() != null ? currentQuestion.getComment() : ""
        );

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "SHOW_CORRECT_ANSWER",
                "payload", payload
        ));




    }


    public void startAdditionalRoundOne() {
        Scenario scenario = scenarioService.getCurrentScenario();
        Scenario.Matching additionalRound = scenario.getRound1Add();

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ADDITIONAL_ROUND_STARTED",
                "text", additionalRound.getText(),
                "leftParts", additionalRound.getLeftParts(),
                "rightParts", additionalRound.getRightParts()
        ));
    }

    public void processAdditionalRoundAnswer(AdditionalRoundAnswer answer) {
        String playerName = answer.getPlayerName();

        // Проверка: если игрок уже ответил — не принимаем второй ответ
        if (playerScores.containsKey(playerName)) {
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", Map.of(
                    "message", "❗Вы уже отправили свой ответ в дополнительном раунде."
            ));
            return;
        }

        Scenario.Matching additionalRound = scenarioService.getCurrentScenario().getRound1Add();
        List<List<String>> correctPairs = additionalRound.getAnswer();

        int points = calculateAdditionalRoundPoints(answer.getMatchedPairs(), correctPairs);
        scoreService.addScore("round1_add", playerName, points);

        playerScores.put(playerName, points); // фиксируем, что он уже отвечал

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "type", "ADDITIONAL_ROUND_ANSWER",
                "player", playerName,
                "points", points
        ));
    }
    // Подсчет очков за дополнительные вопросы
    private int calculateAdditionalRoundPoints(List<List<String>> playerPairs, List<List<String>> correctPairs) {
        int points = 0;
        for (List<String> pair : playerPairs) {
            if (correctPairs.contains(pair)) {
                points++;
            }
        }
        return points;
    }


    // запуск дешифратора с фиксацией старта времени
    public void startDecoderOne() {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder1();
        decoderStartTime = System.currentTimeMillis();
        decoderAnswerTimes.clear();

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "DECODER_STARTED",
                "hint", decoder.getHint(),
                "code", decoder.getCode()
        ));
    }


    // обработка ответов с временем
    public void processDecoderAnswer(DecoderAnswer decoderAnswer) {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder1();

        String correctWord = decoder.getWord();

        String playerName = decoderAnswer.getPlayerName();
        String answer = decoderAnswer.getAnswer().trim();





        if (decoderAnswerTimes.containsKey(playerName)) {
            // Игрок уже ответил
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER_ALREADY_ANSWERED",
                    "player", playerName
            ));
            return;
        }


        long answerTime = System.currentTimeMillis() - decoderStartTime;
        decoderAnswerTimes.put(playerName, answerTime);
        decoder1Scores.merge(playerName, answerTime, Long::sum);



        if (answer.equalsIgnoreCase(correctWord)) {
            scoreService.addScore("round1", playerName, 1); // Заменяем playerScores.put
            scoreService.setDecoderTime("decoder1", playerName, answerTime); // Добавляем время
        }




        if (answer.equalsIgnoreCase(correctWord)) {
            playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + 1);
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER_ANSWER_CORRECT",
                    "player", playerName,
                    "answer", answer,
                    "time", answerTime
            ));
        }else {
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER_ANSWER_WRONG",
                    "player", playerName,
                    "answer", answer,
                    "time", answerTime
            ));
        }

    }



    public void trackDecoderTypingProgress(DecoderTypingProgress progress) {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder1();
        String correctWord = decoder.getWord().toUpperCase();
        String playerInput = progress.getCurrentInput().trim().toUpperCase();

        int matchPercent = calculateMatchPercent(correctWord, playerInput);

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "type", "DECODER_TYPING_PROGRESS",
                "player", progress.getPlayerName(),
                "input", progress.getCurrentInput(),
                "percent", matchPercent
        ));
    }

    private int calculateMatchPercent(String correct, String input) {
        int correctLength = correct.length();
        int matchLength = 0;

        for (int i = 0; i < Math.min(input.length(), correctLength); i++) {
            if (input.charAt(i) == correct.charAt(i)) {
                matchLength++;
            }
        }
        return (int) ((matchLength / (double) correctLength) * 100);
    }


    // второй раунд
    private List<String> availableThemesRound2;
    private String currentThemeRound2;
    private String currentSelectedPlayerRound2;

    public void startRound2() {

        initTopicMapping();


        Scenario scenario = scenarioService.getCurrentScenario();
        availableThemesRound2 = new ArrayList<>(scenario.getRound2().getThemes());

        // Отправляем список доступных тем для выбора
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND2_STARTED",
                "availableThemes", availableThemesRound2
        ));

        // Отправляем список игроков для выбора

        List<String> activePlayers = gameService.getAvailablePlayers().stream()
                .filter(this::isPlayerActive)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND2_PLAYERS_LIST",
                "players", activePlayers
        ));
    }

    public void processRound2Selection(Round2Selection selection) {


        // Проверяем валидность выбора
        if (!availableThemesRound2.contains(selection.getSelectedTheme()) ||
                !isPlayerActive(selection.getSelectedPlayer())) {
            return;
        }

        currentThemeRound2 = selection.getSelectedTheme();
        currentSelectedPlayerRound2 = selection.getSelectedPlayer();



        // Удаляем выбранную тему, чтобы она не показывалась следующему игроку
        availableThemesRound2.remove(currentThemeRound2);



        // Получаем вопросы для выбранной темы
        List<Scenario.SimpleQuestion> questions = getQuestionsForTheme(currentThemeRound2);

        // Запускаем глобальный таймер блitz для выбранного игрока
        startRound2BlitzTimer(currentSelectedPlayerRound2);


        // Отправляем первый вопрос выбранному игроку
        sendRound2Question(currentSelectedPlayerRound2, questions, 0);
    }

    private void startRound2BlitzTimer(String player) {
        setStatusBlitz(1);
        int blitzTime = scenarioService.getCurrentScenario().getRules().getTimeRound2();
        System.out.println("Starting blitz timer for " + blitzTime + " seconds for player: " + player);
        round2BlitzTimerTask = scheduler.schedule(() -> {
            System.out.println("Blitz timer ended for player: " + player);
            messagingTemplate.convertAndSend("/topic/game", Map.of(
                    "event", "ROUND2_BLITZ_ENDED",
                    "player", player
            ));
            setStatusBlitz(0);
        }, blitzTime, TimeUnit.SECONDS);
    }

    private Map<String, List<Scenario.SimpleQuestion>> topicToQuestions = new HashMap<>();

    @PostConstruct
    public void initTopicMapping() {
        // Получаем текущий сценарий и раунд 2
        Scenario.Round2 round2 = scenarioService.getCurrentScenario().getRound2();
        List<String> themes = round2.getThemes();

        // Собираем списки вопросов в массив (порядок вопросов соответствует порядку тем)
        List<List<Scenario.SimpleQuestion>> questionLists = new ArrayList<>();
        questionLists.add(round2.getQ1());
        questionLists.add(round2.getQ2());
        questionLists.add(round2.getQ3());
        questionLists.add(round2.getQ4());
        questionLists.add(round2.getQ5());
        questionLists.add(round2.getQ6());
        questionLists.add(round2.getQ7());
        questionLists.add(round2.getQ8());
        questionLists.add(round2.getQ9());
        questionLists.add(round2.getQ10());
        questionLists.add(round2.getQ11());
        questionLists.add(round2.getQ12());

        // Формируем маппинг: нормализуем название темы (удаляем лишние пробелы, приводим к верхнему регистру)
        for (int i = 0; i < themes.size() && i < questionLists.size(); i++) {
            String normalizedTopic = themes.get(i).trim().toUpperCase();
            topicToQuestions.put(normalizedTopic, questionLists.get(i));
        }
    }


    // Обновлённый метод для получения вопросов по выбранной теме (ROUND2)
    private List<Scenario.SimpleQuestion> getQuestionsForTheme(String theme) {
        if (theme == null) {
            return Collections.emptyList();
        }
        String normalizedTheme = theme.trim().toUpperCase();
        return topicToQuestions.getOrDefault(normalizedTheme, Collections.emptyList());
    }




    // Отправляет вопрос выбранному игроку, без запуска отдельного таймера на ответ
    private void sendRound2Question(String player, List<Scenario.SimpleQuestion> questions, int questionIndex) {
        // Если вопросов больше нет — заканчиваем раунд
        if (questionIndex >= questions.size()) {
            setStatusBlitz(0);
            messagingTemplate.convertAndSend("/topic/game", Map.of(
                    "event", "ROUND2_ENDED"
            ));
            return;
        }

        // Проверяем, не истёк ли уже глобальный таймер блitz
        if (round2BlitzTimerTask.isDone()) {
            setStatusBlitz(0);
            messagingTemplate.convertAndSend("/topic/game", Map.of(
                    "event", "ROUND2_BLITZ_ENDED",
                    "player", player
            ));
            return;
        }

        Scenario.SimpleQuestion question = questions.get(questionIndex);

        // Отправляем вопрос игроку
        messagingTemplate.convertAndSendToUser(player, "/topic/game", Map.of(
                "event", "ROUND2_QUESTION",
                "question", question.getQuestion(),
                "questionIndex", questionIndex
        ));

        // Отправляем вопрос режиссёру и ведущему
        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "ROUND2_QUESTION",
                "player", player,
                "question", question.getQuestion(),
                "answer", question.getAnswer(),
                "questionIndex", questionIndex
        ));
        messagingTemplate.convertAndSend("/topic/host", Map.of(
                "event", "ROUND2_QUESTION",
                "player", player,
                "question", question.getQuestion(),
                "answer", question.getAnswer(),
                "questionIndex", questionIndex
        ));
    }


    public Integer getStatusBlitz() {
        return statusBlitz;
    }

    public void setStatusBlitz(Integer statusBlitz) {
        this.statusBlitz = statusBlitz;
    }

    // Метод, вызываемый после решения режиссёра (нажатия "правильно/неправильно")
    public void directorDecisionRound2(boolean isCorrect, int questionIndex) {
        // Если время блitz уже истекло, не отправляем следующий вопрос
        if (round2BlitzTimerTask != null && round2BlitzTimerTask.isDone()) {
            setStatusBlitz(0);
            messagingTemplate.convertAndSend("/topic/game", Map.of(
                    "event", "ROUND2_BLITZ_ENDED",
                    "player", currentSelectedPlayerRound2
            ));
            return;
        }

        if (isCorrect) {
            playerScores.merge(currentSelectedPlayerRound2, 1, Integer::sum);
            scoreService.addScore("round2", currentSelectedPlayerRound2, 1);
        }

        // Отправляем результат решения режиссёра
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND2_ANSWER_RESULT",
                "player", currentSelectedPlayerRound2,
                "isCorrect", isCorrect,
                "questionIndex", questionIndex
        ));

        // Получаем список вопросов для текущей темы
        List<Scenario.SimpleQuestion> questions = getQuestionsForTheme(currentThemeRound2);
        // Переходим к следующему вопросу
        sendRound2Question(currentSelectedPlayerRound2, questions, questionIndex + 1);
    }



//    public void startRound2() {
//        Scenario scenario = scenarioService.getCurrentScenario();
//        questionsRound2 = scenario.getRound2().getQ1(); // Или другая логика выбора вопросов
//        currentQuestionIndexRound2 = 0;
//
//        // Определяем порядок игроков по скорости дешифратора
//        playerOrderRound2 = decoderAnswerTimes.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .toList();
//
//        currentPlayerIndexRound2 = 0;
//
//        sendCurrentQuestionToActivePlayer();
//    }
//

//
//    private void sendCurrentQuestionToActivePlayer() {
//        if (currentPlayerIndexRound2 >= playerOrderRound2.size()) {
//            messagingTemplate.convertAndSend("/topic/game", Map.of(
//                    "event", "ROUND2_ENDED"
//            ));
//            return;
//        }
//
//        String currentPlayer = playerOrderRound2.get(currentPlayerIndexRound2);
//        Scenario.SimpleQuestion currentQuestion = questionsRound2.get(currentQuestionIndexRound2);
//
//        // Игроку
//        messagingTemplate.convertAndSendToUser(currentPlayer, "/topic/game", Map.of(
//                "event", "YOUR_TURN",
//                "question", currentQuestion.getQ()
//        ));
//
//        // Тут же запускаем таймер:
//        startRound2Timer(currentPlayer);
//
//        // Ведущему и режиссёру
//        messagingTemplate.convertAndSend("/topic/director", Map.of(
//                "event", "ROUND2_QUESTION",
//                "player", currentPlayer,
//                "question", currentQuestion.getQ(),
//                "answer", currentQuestion.getR()
//        ));
//
//        messagingTemplate.convertAndSend("/topic/host", Map.of(
//                "event", "ROUND2_QUESTION",
//                "player", currentPlayer,
//                "question", currentQuestion.getQ(),
//                "answer", currentQuestion.getR()
//        ));
//    }



//    public void directorDecisionRound2(boolean isCorrect) {
//        String currentPlayer = playerOrderRound2.get(currentPlayerIndexRound2);
//
//        if (isCorrect) {
//            playerScores.put(currentPlayer, playerScores.getOrDefault(currentPlayer, 0) + 1);
//        }
//
//        // Оповещение всех участников о решении режиссёра
//        messagingTemplate.convertAndSend("/topic/game", Map.of(
//                "event", "DIRECTOR_DECISION",
//                "player", currentPlayer,
//                "isCorrect", isCorrect
//        ));
//
//        // переходим к следующему вопросу и игроку
//        currentPlayerIndexRound2++;
//        currentQuestionIndexRound2++;
//
//        sendCurrentQuestionToActivePlayer();
//    }


    // старт дополнительного второго раунда
    public void startAdditionalRoundTwo() {
        Scenario scenario = scenarioService.getCurrentScenario();
        Scenario.Matching additionalRound2 = scenario.getRound2Add();

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ADDITIONAL_ROUND2_STARTED",
                "text", additionalRound2.getText(),
                "leftParts", additionalRound2.getLeftParts(),
                "rightParts", additionalRound2.getRightParts()
        ));
    }

    public void processAdditionalRoundTwoAnswer(AdditionalRoundAnswer answer) {
        String playerName = answer.getPlayerName();

        // Проверка: уже есть ответ от игрока в round2ADDScores — повтор запрещён
        if (round2ADDScores.containsKey(playerName)) {
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", Map.of(
                    "message", "❗Вы уже отправили ответ на дополнительный второй раунд."
            ));
            return;
        }

        Scenario.Matching additionalRound2 = scenarioService.getCurrentScenario().getRound2Add();
        List<List<String>> correctPairs = additionalRound2.getAnswer();

        int points = calculateAdditionalRoundPoints(answer.getMatchedPairs(), correctPairs);
        scoreService.addScore("round2_add", playerName, points);

        playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + points);
        round2ADDScores.put(playerName, points); // фиксируем ответ

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "type", "ADDITIONAL_ROUND2_ANSWER",
                "player", playerName,
                "points", points
        ));
    }


    // Запуск дешифратора второго раунда
    public void startDecoderTwo() {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder2();
        decoder2StartTime = System.currentTimeMillis();
        decoder2AnswerTimes.clear();

        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "DECODER2_STARTED",
                "hint", decoder.getHint(),
                "code", decoder.getCode()
        ));
    }

    // Обработка ответов дешифратора второго раунда
    public void processDecoderTwoAnswer(DecoderAnswer decoderAnswer) {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder2();
        String correctWord = decoder.getWord();

        String playerName = decoderAnswer.getPlayerName();
        String answer = decoderAnswer.getAnswer().trim();



        if (decoder2AnswerTimes.containsKey(playerName)) {
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER2_ALREADY_ANSWERED",
                    "player", playerName
            ));
            return;
        }

        long answerTime = System.currentTimeMillis() - decoder2StartTime;
        decoder2AnswerTimes.put(playerName, answerTime);
        decoder2Scores.merge(playerName, answerTime, Long::sum);

        if (answer.equalsIgnoreCase(correctWord)) {
            scoreService.addScore("round2", playerName, 1); // Заменяем playerScores.put
            scoreService.setDecoderTime("decoder2", playerName, answerTime); // Добавляем время
            playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + 1);
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER2_ANSWER_CORRECT",
                    "player", playerName,
                    "answer", answer,
                    "time", answerTime
            ));
        } else {
            messagingTemplate.convertAndSend("/topic/director", Map.of(
                    "type", "DECODER2_ANSWER_WRONG",
                    "player", playerName,
                    "answer", answer,
                    "time", answerTime
            ));
        }
    }

    // Отслеживание прогресса ввода дешифратора второго раунда
    public void trackDecoderTwoTypingProgress(DecoderTypingProgress progress) {
        Scenario.Decoder decoder = scenarioService.getCurrentScenario().getDecoder2();
        String correctWord = decoder.getWord().toUpperCase();
        String playerInput = progress.getCurrentInput().trim().toUpperCase();

        int matchPercent = calculateMatchPercent(correctWord, playerInput);

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "type", "DECODER2_TYPING_PROGRESS",
                "player", progress.getPlayerName(),
                "input", progress.getCurrentInput(),
                "percent", matchPercent
        ));
    }



    public void startRound3() {
        Scenario scenario = scenarioService.getCurrentScenario();

        // 1. Выбираем топ-3 игроков по накопленным очкам
        finalPlayersRound3 = getTopPlayers(3);

        // 2. Заполняем темы для игроков, вышедших в 3-й раунд
        for (String player : finalPlayersRound3) {
            for (Scenario.PlayerData pData : scenario.getPlayers()) {
                if (pData.getName().equals(player)) {
                    playerThemesRound3.put(player, pData.getTheme());
                    break;
                }
            }
        }

        // 3. Назначаем цвета игрокам (например, базовыми цветами)
        assignPlayerColors();

        // 4. Загружаем вопросы третьего раунда
        loadRound3Questions(scenario);

        // 5. Инициализируем счет для третьего раунда
        initRound3Scores();

        // 6. Показываем таблицу с назначенными цветами
        showColorTable();

        // 7. Через 5 секунд скрываем цвета и разрешаем выбор вопросов
        scheduler.schedule(this::hideColorsAndStartSelection, 5, TimeUnit.SECONDS);

        // 8. Оповещаем режиссёра о доступных вопросах
        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "ROUND3_SELECTION_AVAILABLE",
                "availableQuestions", availableQuestions,
                "players", finalPlayersRound3
        ));
    }

    private void initRound3Scores() {
        round3Scores.clear();
        finalPlayersRound3.forEach(player -> round3Scores.put(player, 0));
    }




    private List<String> getTopPlayers(int count) {
        return playerScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // сортируем по убыванию очков
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void assignPlayerColors() {
        String[] colors = {"GREEN", "BLUE", "RED"}; // базовые цвета для 3 игроков

        for (int i = 0; i < finalPlayersRound3.size(); i++) {
            playerColorsRound3.put(finalPlayersRound3.get(i), colors[i]);
        }

        // Сообщаем фронтенду о назначенных цветах
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "PLAYER_COLORS_ASSIGNED",
                "colors", playerColorsRound3
        ));
    }


    private void showColorTable() {
        // Отправляем информацию о цветах игроков и вопросах
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND3_COLOR_TABLE_SHOW",
                "playerColors", playerColorsRound3,
                "questionColors", questionColors
        ));
    }


    private void hideColorsAndStartSelection() {
        // Скрываем цвета и разрешаем выбор вопросов
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND3_COLOR_TABLE_HIDE",
                "availableQuestions", availableQuestions
        ));



    }


    public List<Map<String, Object>> getAvailableRound3Questions() {

        List<Map<String, Object>> result = new ArrayList<>();

        for (Integer questionNumber : availableQuestions.stream().sorted().toList()) {
            Map<String, Object> info = new HashMap<>();
            info.put("questionNumber", questionNumber);
            info.put("question", questionTexts.get(questionNumber));
            info.put("theme", questionThemes.get(questionNumber));
            info.put("color", questionColors.get(questionNumber));
            result.add(info);
        }

        return result;
    }


    /**
     * Выбор вопроса для третьего раунда.
     * Отправляет сообщение с вопросом выбранному игроку, режиссёру и ведущему,
     * затем запускает таймер для автоматической проверки ответа.
     */
    public void processQuestionSelection(QuestionSelection selection) {
        if (currentQuestionNumber != 0) {
            // Уже есть активный вопрос – игнорируем повторный выбор
            return;
        }
        if (!availableQuestions.contains(selection.getQuestionNumber())) {
            // Такой вопрос недоступен
            return;
        }
        currentQuestionNumber = selection.getQuestionNumber();
        currentAnsweringPlayer = selection.getPlayerName();

        // Отправляем вопрос выбранному игроку
        messagingTemplate.convertAndSendToUser(selection.getPlayerName(), "/topic/game", Map.of(
                "event", "ROUND3_QUESTION",
                "questionNumber", selection.getQuestionNumber(),
                "question", questionTexts.get(selection.getQuestionNumber()),
                "timeLimit", scenarioService.getCurrentScenario().getRules().getTimeRound3()
        ));

        // Отправляем вопрос режиссёру
        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "ROUND3_QUESTION",
                "player", selection.getPlayerName(),
                "questionNumber", selection.getQuestionNumber(),
                "question", questionTexts.get(selection.getQuestionNumber()),
                "answer", questionAnswers.get(selection.getQuestionNumber()),
                "theme", questionThemes.get(selection.getQuestionNumber())
        ));

        // Отправляем вопрос ведущему
        messagingTemplate.convertAndSend("/topic/host", Map.of(
                "event", "ROUND3_QUESTION",
                "player", selection.getPlayerName(),
                "questionNumber", selection.getQuestionNumber(),
                "question", questionTexts.get(selection.getQuestionNumber()),
                "answer", questionAnswers.get(selection.getQuestionNumber()),
                "theme", questionThemes.get(selection.getQuestionNumber())
        ));

        // Запускаем таймер для ожидания ответа
        startRound3Timer(selection.getQuestionNumber());
    }



    public void processRound3Answer(Round3Answer answer) {
        handleRound3Answer(answer);
    }

    /**
     * Запуск таймера для вопроса третьего раунда.
     * По истечении timeLimit секунд, если вопрос ещё активен, автоматически
     * засчитывается неверный ответ.
     */
    private void startRound3Timer(int questionNumber) {
        int timeLimit = scenarioService.getCurrentScenario().getRules().getTimeRound3();
        round3TimerTask = scheduler.schedule(() -> {
            if (currentQuestionNumber == questionNumber) {
                // Время истекло – автоматически неверный ответ
                Round3Answer answer = new Round3Answer();
                answer.setPlayerName(currentAnsweringPlayer);
                answer.setQuestionNumber(questionNumber);
                answer.setCorrect(false);
                handleRound3Answer(answer);
            }
        }, timeLimit, TimeUnit.SECONDS);
    }



    private void handleRound3Answer(Round3Answer answer) {
        // Если вопрос уже обработан или номер не совпадает – выходим
        if (currentQuestionNumber == 0 || currentQuestionNumber != answer.getQuestionNumber()) {
            return;
        }

        // Останавливаем таймер, если он ещё активен
        if (round3TimerTask != null && !round3TimerTask.isDone()) {
            round3TimerTask.cancel(true);
        }

        int points = 0;
        if (answer.isCorrect()) {
            String theme = questionThemes.get(answer.getQuestionNumber());
            if (theme == null) {
                theme = "NEUTRAL";
            }
            String playerTheme = playerThemesRound3.getOrDefault(answer.getPlayerName(), "NEUTRAL");
            if ("NEUTRAL".equals(theme)) {
                points = 1;
            } else if (playerTheme.equals(theme)) {
                points = 2;
            } else {
                points = 3;
            }
            round3Scores.merge(answer.getPlayerName(), points, Integer::sum);
            playerScores.merge(answer.getPlayerName(), points, Integer::sum);
            scoreService.addScore("round3", answer.getPlayerName(), points);
        }

        // Удаляем вопрос из списка доступных
        availableQuestions.remove(answer.getQuestionNumber());
        questionColors.remove(answer.getQuestionNumber());
        questionThemes.remove(answer.getQuestionNumber());
        questionTexts.remove(answer.getQuestionNumber());
        questionAnswers.remove(answer.getQuestionNumber());

        // Отправляем результат всем участникам
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND3_ANSWER_RESULT",
                "player", answer.getPlayerName(),
                "questionNumber", answer.getQuestionNumber(),
                "isCorrect", answer.isCorrect(),
                "points", points,
                "totalScore", round3Scores.getOrDefault(answer.getPlayerName(), 0)
        ));

        hideColorsAndStartSelection();

        // Сбрасываем текущий вопрос
        currentQuestionNumber = 0;
        currentAnsweringPlayer = null;

        // Если вопросов больше нет, завершаем раунд
        if (availableQuestions.isEmpty()) {
            endRound3();
        }
    }





    public void directorSelectQuestionForRound3(String playerName, int questionNumber) {
        if (currentQuestionNumber != 0 || !availableQuestions.contains(questionNumber)) {
            return;
        }
        currentQuestionNumber = questionNumber;
        currentAnsweringPlayer = playerName;

        messagingTemplate.convertAndSendToUser(playerName, "/topic/game", Map.of(
                "event", "ROUND3_QUESTION",
                "questionNumber", questionNumber,
                "question", questionTexts.get(questionNumber),
                "timeLimit", scenarioService.getCurrentScenario().getRules().getTimeRound3()
        ));
        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "ROUND3_QUESTION",
                "player", playerName,
                "questionNumber", questionNumber,
                "question", questionTexts.get(questionNumber),
                "answer", questionAnswers.get(questionNumber),
                "theme", questionThemes.get(questionNumber)
        ));
        messagingTemplate.convertAndSend("/topic/host", Map.of(
                "event", "ROUND3_QUESTION",
                "player", playerName,
                "questionNumber", questionNumber,
                "question", questionTexts.get(questionNumber),
                "answer", questionAnswers.get(questionNumber),
                "theme", questionThemes.get(questionNumber)
        ));

        startRound3Timer(questionNumber);


    }


    private void endRound3() {
        round3Scores.forEach((player, score) -> {
            playerScores.merge(player, score, Integer::sum);
        });
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "ROUND3_ENDED",
                "scores", round3Scores
        ));
    }

    private void loadRound3Questions(Scenario scenario) {
        questionColors.clear();
        questionThemes.clear();
        questionTexts.clear();
        questionAnswers.clear();
        availableQuestions.clear();

        // 1. Собираем все вопросы из тем игроков
        List<Scenario.SimpleQuestion> playerQuestions = new ArrayList<>();
        Map<Scenario.SimpleQuestion, String> questionToTheme = new HashMap<>();
        Map<Scenario.SimpleQuestion, String> questionToPlayer = new HashMap<>();

        for (Scenario.PlayerData player : scenario.getPlayers()) {
            if (finalPlayersRound3.contains(player.getName())) {
                for (Scenario.SimpleQuestion question : player.getRound3()) {
                    playerQuestions.add(question);
                    questionToTheme.put(question, player.getTheme());
                    questionToPlayer.put(question, player.getName());
                }
            }
        }
        // 2. Добавляем нейтральные вопросы
        List<Scenario.SimpleQuestion> neutralQuestions = scenario.getRound3().getNetral();

        // 3. Объединяем и перемешиваем вопросы
        List<Scenario.SimpleQuestion> allQuestions = new ArrayList<>();
        allQuestions.addAll(playerQuestions);
        allQuestions.addAll(neutralQuestions);
        Collections.shuffle(allQuestions);

        // 4. Назначаем вопросы номерам от 1 до 36 (или меньше, если вопросов меньше)
        int questionCount = Math.min(allQuestions.size(), 36);
        for (int i = 1; i <= questionCount; i++) {
            Scenario.SimpleQuestion question = allQuestions.get(i-1);
            String theme = questionToTheme.getOrDefault(question, "NEUTRAL");
            String player = questionToPlayer.get(question);

            availableQuestions.add(i);
            questionTexts.put(i, question.getQuestion());
            questionAnswers.put(i, question.getAnswer());
            questionThemes.put(i, theme);

            // Назначаем цвет
            if ("NEUTRAL".equals(theme)) {
                questionColors.put(i, "GRAY");
            } else {
                questionColors.put(i, playerColorsRound3.get(player));
            }
        }
    }


    public void sendRound3QuestionsByTheme(String theme) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Integer questionNumber : availableQuestions) {
            if (theme.equals(questionThemes.get(questionNumber))) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("questionNumber", questionNumber);
                entry.put("color", questionColors.get(questionNumber));
                result.add(entry);
            }
        }

        messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "ROUND3_QUESTIONS_BY_THEME",
                "theme", theme,
                "questions", result
        ));
    }




    // отправка общего рейтинга и рейтинга по раундам режиссёру
    public void sendRatingsToDirector() {
        Map<String, Integer> totalRatings = new HashMap<>(playerScores);

        // сортировка рейтинга с учетом алфавита при равных очках
        LinkedHashMap<String, Integer> sortedRatings = totalRatings.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

             messagingTemplate.convertAndSend("/topic/director", Map.of(
                "event", "RATINGS_UPDATE",
                "totalRatings", scoreService.getOverallRanking(),
                "round1Ranking", scoreService.getRoundRanking("round1"),
                "round2Ranking", scoreService.getRoundRanking("round2"),
                "round3Ranking", scoreService.getRoundRanking("round3"),
                "decoder1Results", scoreService.getDecoderRanking("decoder1"),
                "decoder2Results", scoreService.getDecoderRanking("decoder2")
        ));
    }


    // метод для сортировки результатов дешифраторов
    private LinkedHashMap<String, Long> getSortedDecoderResults(Map<String, Long> decoderResults) {
        return decoderResults.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }



    private final Set<String> eliminatedPlayers = new HashSet<>();

    public void eliminatePlayer(String playerName) {
        eliminatedPlayers.add(playerName);
        messagingTemplate.convertAndSend("/topic/game", Map.of(
                "event", "PLAYER_ELIMINATED",
                "player", playerName
        ));
    }

    // Проверка, не выбыл ли игрок
    private boolean isPlayerActive(String playerName) {
        return !eliminatedPlayers.contains(playerName);
    }




    // Запуск таймера второго раунда (блица)
    public void startRound2Timer(String currentPlayer) {
        int round2Time = scenarioService.getCurrentScenario().getRules().getTimeRound2();
        long endTime = System.currentTimeMillis() + round2Time * 1000L;

        round2TimerTask = scheduler.scheduleAtFixedRate(() -> {
            long remainingSeconds = (endTime - System.currentTimeMillis()) / 1000;

            if (remainingSeconds <= 0) {
                messagingTemplate.convertAndSendToUser(currentPlayer, "/queue/round2", Map.of(
                        "event", "TIMER_ENDED"
                ));
                round2TimerTask.cancel(true);
                // Здесь можно автоматически переходить к следующему игроку или принимать другие решения
            } else {
                messagingTemplate.convertAndSendToUser(currentPlayer, "/queue/round2", Map.of(
                        "event", "TIMER_TICK",
                        "remainingSeconds", remainingSeconds
                ));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    // Добавьте этот метод в RoundService для третьего раунда
    public void directorDecisionRound3(String playerName, int questionNumber, boolean isCorrect) {
        Round3Answer answer = new Round3Answer();
        answer.setPlayerName(playerName);
        answer.setQuestionNumber(questionNumber);
        answer.setCorrect(isCorrect);
        handleRound3Answer(answer);
    }

    // Вспомогательный метод для инкрементации очков
    private void incrementScore(Map<String, Integer> scoreMap, String player, int increment) {
        scoreMap.merge(player, increment, Integer::sum);
    }

    public Map<String, Integer> getRound1Results() {
        Map<String, Integer> results = new HashMap<>();

        for (Map.Entry<String, Integer> entry : countRoundCheck.entrySet()) {

            results.put(entry.getKey(), entry.getValue());
        }

        return results;
    }

    public Map<String, Object> getAdditionalRound1Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerScore> addScores = scoreService.getRoundRanking("round1_add");
        Map<String, Integer> scoresMap = addScores.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerScore::getPlayerName,
                        ScoreService.PlayerScore::getScore
                ));

        results.put("additionalRound1Scores", scoresMap);
        return results;
    }

    public Map<String, Object> getDecoder1Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerDecoderScore> decoderResults = scoreService.getDecoderRanking("decoder1");
        Map<String, Long> decoderMap = decoderResults.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerDecoderScore::getPlayerName,
                        ScoreService.PlayerDecoderScore::getTimeMs
                ));

        results.put("decoder1Results", decoderMap);
        return results;
    }

    public Map<String, Object> getRound2Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerScore> round2Scores = scoreService.getRoundRanking("round2");
        Map<String, Integer> scoresMap = round2Scores.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerScore::getPlayerName,
                        ScoreService.PlayerScore::getScore
                ));

        results.put("round2Scores", scoresMap);
        return results;
    }

    public Map<String, Object> getAdditionalRound2Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerScore> addScores = scoreService.getRoundRanking("round2_add");
        Map<String, Integer> scoresMap = addScores.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerScore::getPlayerName,
                        ScoreService.PlayerScore::getScore
                ));

        results.put("additionalRound2Scores", scoresMap);
        return results;
    }

    public Map<String, Object> getDecoder2Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerDecoderScore> decoderResults = scoreService.getDecoderRanking("decoder2");
        Map<String, Long> decoderMap = decoderResults.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerDecoderScore::getPlayerName,
                        ScoreService.PlayerDecoderScore::getTimeMs
                ));

        results.put("decoder2Results", decoderMap);
        return results;
    }

    public Map<String, Object> getRound3Results() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerScore> round3Scores = scoreService.getRoundRanking("round3");
        Map<String, Integer> scoresMap = round3Scores.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerScore::getPlayerName,
                        ScoreService.PlayerScore::getScore
                ));

        results.put("round3Scores", scoresMap);
        return results;
    }

    public Map<String, Object> getOverallResults() {
        Map<String, Object> results = new HashMap<>();
        List<ScoreService.PlayerScore> overallScores = scoreService.getOverallRanking();
        Map<String, Integer> scoresMap = overallScores.stream()
                .collect(Collectors.toMap(
                        ScoreService.PlayerScore::getPlayerName,
                        ScoreService.PlayerScore::getScore
                ));

        results.put("overallScores", scoresMap);
        return results;
    }

    public List<String> getFinalPlayersRound3() {
        return finalPlayersRound3;
    }



    public List<String> getRound3PlayerThemes() {
        // Получаем текущий сценарий
        List<String> resultTheme = new ArrayList<>();
        Scenario scenario = scenarioService.getScenario();
        Map<String, String> themes = new HashMap<>();
        if (finalPlayersRound3 != null) {
            for (Scenario.PlayerData player : scenario.getPlayers()) {
                if (finalPlayersRound3.contains(player.getName())) {
                    themes.put(player.getName(), player.getTheme());
                    resultTheme.add(player.getTheme());
                }
            }
        }

        return resultTheme;
    }




}
