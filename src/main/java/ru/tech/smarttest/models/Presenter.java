package ru.tech.smarttest.models;

import io.jsonwebtoken.io.Decoder;

import java.util.List;
import java.util.Map;

public class Presenter {
    private final String id;
    private final GameSession currentSession;

    public Presenter(String id, GameSession session) {
        this.id = id;
        this.currentSession = session;
    }

//    // Текущая информация о вопросах и ответах
//    public Scenario.Question getCurrentQuestion() { /*...*/ }
//    public List<String> getCurrentChoices() { /*...*/ }
//    public String getCorrectAnswer() { /*...*/ }
//    public String getCurrentComment() { /*...*/ }
//    public int getCurrentRound() { /*...*/ }
//    public Decoder getDecoderDetails() { /*...*/ }
//
//    // Информация об игроках
//    public List<String> getPlayerNames() { /*...*/ }
//    public Map<String, String> getPlayerThemes() { /*...*/ }
//
//    // Состояние игры
//    public GameState getGameState() { /*...*/ }
}
