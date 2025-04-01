package ru.tech.smarttest.models;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Scenario {
    @JsonProperty("PLAYERS")
    private List<PlayerData> players;

    @JsonProperty("RULES")
    private Rules rules;

    @JsonProperty("ROUND1")
    private List<Question> round1;

    @JsonProperty("ROUND1_ADD")
    private Matching round1Add;

    @JsonProperty("DECODER1")
    private Decoder decoder1;

    @JsonProperty("ROUND2")
    private Round2 round2;

    @JsonProperty("ROUND2_ADD")
    private Matching round2Add;

    @JsonProperty("DECODER2")
    private Decoder decoder2;

    @JsonProperty("ROUND3")
    private Round3 round3;

    @Data
    public static class PlayerData {
        @JsonProperty("NAME")
        private String name;

        @JsonProperty("THEME")
        private String theme;

        @JsonProperty("ROUND3")
        private List<SimpleQuestion> round3;
    }

    @Data
    public static class Rules {
        @JsonProperty("KOLROUND2")
        private int kolRound2;

        @JsonProperty("KOLROUND3")
        private int kolRound3;

        @JsonProperty("TIMEROUND1")
        private int timeRound1;

        @JsonProperty("TIMEROUND2")
        private int timeRound2;

        @JsonProperty("TIMEROUND3")
        private int timeRound3;
    }

    @Data
    public static class Question {
        @JsonProperty("TEXT")
        private String text;

        @JsonProperty("CHOICES")
        private List<String> choices;

        @JsonProperty("ANSWER")
        private String answer;

        @JsonProperty("COMMENT")
        private String comment;
    }

    @Data
    public static class Matching {
        @JsonProperty("TEXT")
        private String text;

        @JsonProperty("LEFTS_PARTS")
        private List<String> leftParts;

        @JsonProperty("RIGHT_PARTS")
        private List<String> rightParts;

        @JsonProperty("ANSWER")
        private List<List<String>> answer;
    }

    @Data
    public static class Decoder {
        @JsonProperty("WORD")
        private String word;

        @JsonProperty("HINT")
        private String hint;

        @JsonProperty("CODE")
        private String code;
    }

    @Data
    public static class Round2 {
        @JsonProperty("THEMES")
        private List<String> themes;

        @JsonProperty("Q1")
        private List<SimpleQuestion> q1;

        @JsonProperty("Q2")
        private List<SimpleQuestion> q2;

        @JsonProperty("Q3")
        private List<SimpleQuestion> q3;

        @JsonProperty("Q4")
        private List<SimpleQuestion> q4;

        @JsonProperty("Q5")
        private List<SimpleQuestion> q5;

        @JsonProperty("Q6")
        private List<SimpleQuestion> q6;

        @JsonProperty("Q7")
        private List<SimpleQuestion> q7;

        @JsonProperty("Q8")
        private List<SimpleQuestion> q8;

        @JsonProperty("Q9")
        private List<SimpleQuestion> q9;

        @JsonProperty("Q10")
        private List<SimpleQuestion> q10;

        @JsonProperty("Q11")
        private List<SimpleQuestion> q11;

        @JsonProperty("Q12")
        private List<SimpleQuestion> q12;
    }

    @Data
    public static class Round3 {
        @JsonProperty("NETRAL")
        private List<SimpleQuestion> netral;
    }

    @Data
    public static class SimpleQuestion {
        private Map<String, String> qa = new HashMap<>();

        @JsonAnySetter
        public void setQa(String key, String value) {
            qa.put(key, value);
        }

        // Удобный метод для получения текста вопроса (берётся первый ключ)
        public String getQuestion() {
            return qa.isEmpty() ? null : qa.keySet().iterator().next();
        }

        // Удобный метод для получения ответа (берётся первое значение)
        public String getAnswer() {
            return qa.isEmpty() ? null : qa.values().iterator().next();
        }
    }
}