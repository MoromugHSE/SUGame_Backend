package ru.tech.smarttest.dto;

import lombok.Data;

@Data
public class Round3Answer {
    private String playerName;
    private boolean isCorrect;
    private int questionNumber;
}