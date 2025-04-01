package ru.tech.smarttest.models;

import java.util.List;
import java.util.Map;

public class Director {

    private final String id;
    private GameSession currentSession;

    public Director(String id) {
        this.id = id;
    }
//
//    // Управление сессией
//    public GameSession createSession(String scenarioJson) { /*...*/ }
//    public GameSession getCurrentSession() { /*...*/ }

    // Управление раундами
    public void startRound1() { /*...*/ }
    public void startAdditionalRound1() { /*...*/ }

    public void startRound2() { /*...*/ }
    public void startAdditionalRound2() { /*...*/ }
    public void startDecoder2() { /*...*/ }
    public void startRound3() { /*...*/ }

//    // Управление игровым процессом
//    public void acceptRound2Answer(String playerId, boolean correct) { /*...*/ }
//    public boolean isCategoryAvailable(String categoryName) { /*...*/ }
//
//    // Результаты
//    public Map<String, Integer> getRoundScores(int roundNumber) { /*...*/ }
//    public Map<String, Integer> getOverallScores() { /*...*/ }
//    public List<String> getDecoderOrder(int decoderNumber) { /*...*/ }

    // Управление состоянием
    public void pauseGame() { /*...*/ }
    public void resumeGame() { /*...*/ }
    public void finishGame() { /*...*/ }

//    // Просмотр текущей информации
//    public Scenario.Question getCurrentQuestion() { /*...*/ }
//    public GameState getSessionState() { /*...*/ }
}
