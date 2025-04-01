package ru.tech.smarttest.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScoreService {
    // Храним очки по раундам
    private final Map<String, Integer> round1Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round1ADDScores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round2ADDScores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round2Scores = new ConcurrentHashMap<>();
    private final Map<String, Integer> round3Scores = new ConcurrentHashMap<>();

    // Для дешифраторов храним время ответа (в мс)
    private final Map<String, Long> decoder1Times = new ConcurrentHashMap<>();
    private final Map<String, Long> decoder2Times = new ConcurrentHashMap<>();

    // Добавление очков в раунде
    public void addScore(String round, String playerName, int points) {
        switch (round) {
            case "round1" -> round1Scores.merge(playerName, points, Integer::sum);
            case "round2" -> round2Scores.merge(playerName, points, Integer::sum);
            case "round3" -> round3Scores.merge(playerName, points, Integer::sum);
            case "round1_add" -> round1ADDScores.merge(playerName, points, Integer::sum);
            case "round2_add" -> round2ADDScores.merge(playerName,points, Integer::sum);
            default -> throw new IllegalArgumentException("Unknown round: " + round);
        }
    }

    // Фиксация времени ответа на дешифратор
    public void setDecoderTime(String decoder, String playerName, long timeMs) {
        switch (decoder) {
            case "decoder1" -> decoder1Times.put(playerName, timeMs);
            case "decoder2" -> decoder2Times.put(playerName, timeMs);
            default -> throw new IllegalArgumentException("Unknown decoder: " + decoder);
        }
    }


    private List<String> getAllPlayers() {
        return List.of(
                        round1Scores.keySet(),
                        round2Scores.keySet(),
                        round3Scores.keySet(),
                        round1ADDScores.keySet(),
                        round2ADDScores.keySet(),
                        decoder1Times.keySet(),
                        decoder2Times.keySet()
                ).stream()
                .flatMap(Set::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }




    // Получение рейтинга для раунда (сортировка по очкам и алфавиту)
    public List<PlayerScore> getRoundRanking(String round) {
        Map<String, Integer> scores = switch (round) {
            case "round1" -> round1Scores;
            case "round2" -> round2Scores;
            case "round3" -> round3Scores;
            case "round1_add" -> round1ADDScores;
            case "round2_add" -> round2ADDScores;
            default -> throw new IllegalArgumentException("Unknown round: " + round);
        };

        // Теперь включаем всех игроков
        return getAllPlayers().stream()
                .map(player -> new PlayerScore(player, scores.getOrDefault(player, 0)))
                .sorted((e1, e2) -> {
                    int pointsCompare = Integer.compare(e2.getScore(), e1.getScore());
                    return pointsCompare != 0 ? pointsCompare : e1.getPlayerName().compareTo(e2.getPlayerName());
                })
                .collect(Collectors.toList());
    }

    // Рейтинг для дешифратора (сортировка по времени и алфавиту)
    public List<PlayerDecoderScore> getDecoderRanking(String decoder) {
        Map<String, Long> times = switch (decoder) {
            case "decoder1" -> decoder1Times;
            case "decoder2" -> decoder2Times;
            default -> throw new IllegalArgumentException("Unknown decoder: " + decoder);
        };

        return times.entrySet().stream()
                .sorted((e1, e2) -> {
                    // Сначала по времени (по возрастанию)
                    int timeCompare = e1.getValue().compareTo(e2.getValue());
                    if (timeCompare != 0) return timeCompare;
                    // При равном времени - по алфавиту
                    return e1.getKey().compareTo(e2.getKey());
                })
                .map(entry -> new PlayerDecoderScore(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // Общий рейтинг (сумма очков по всем раундам)
    public List<PlayerScore> getOverallRanking() {
        Map<String, Integer> totalScores = new HashMap<>();

        // Соберем очки
        round1Scores.forEach((player, score) -> totalScores.merge(player, score, Integer::sum));
        round2Scores.forEach((player, score) -> totalScores.merge(player, score, Integer::sum));
        round3Scores.forEach((player, score) -> totalScores.merge(player, score, Integer::sum));
        round1ADDScores.forEach((player, score) -> totalScores.merge(player, score, Integer::sum));
        round2ADDScores.forEach((player, score) -> totalScores.merge(player, score, Integer::sum));

        // Добавим 0 для остальных
        for (String player : getAllPlayers()) {
            totalScores.putIfAbsent(player, 0);
        }

        return totalScores.entrySet().stream()
                .sorted((e1, e2) -> {
                    int pointsCompare = Integer.compare(e2.getValue(), e1.getValue());
                    return pointsCompare != 0 ? pointsCompare : e1.getKey().compareTo(e2.getKey());
                })
                .map(entry -> new PlayerScore(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // DTO для возврата результатов
    @Data
    @AllArgsConstructor
    public static class PlayerScore {
        private String playerName;
        private int score;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerDecoderScore {
        private String playerName;
        private long timeMs;
    }



}
