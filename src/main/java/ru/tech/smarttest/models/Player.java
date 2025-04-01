package ru.tech.smarttest.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {

    private final String id;
    private final String name;
    private final String theme;
    private final List<Scenario.Question> themeQuestions;
    private int currentScore = 0;
    private String decoderAnswer;
    private Map<Integer, List<Boolean>> answersHistory = new HashMap<>();

    public Player(String id, String name, String theme, List<Scenario.Question> themeQuestions) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.themeQuestions = themeQuestions;
    }

//    // Ответы
//    public boolean answerQuestion(int roundNumber, String answer) { /*...*/ }
    public void submitDecoderAnswer(String decoderAnswer) { this.decoderAnswer = decoderAnswer; }

    // Состояние игрока
    public int getCurrentScore() { return currentScore; }
    public String getTheme() { return theme; }
    public List<Scenario.Question> getThemeQuestions() { return themeQuestions; }
    public List<Boolean> getAnswersHistory(int roundNumber) { return answersHistory.get(roundNumber); }

    // Управление состоянием (из Director/GameSession)
    public void incrementScore() { currentScore++; }
    public void resetScore() { currentScore = 0; }
    public void addAnswerResult(int round, boolean result) {
        answersHistory.computeIfAbsent(round, k -> new ArrayList<>()).add(result);
    }
}
