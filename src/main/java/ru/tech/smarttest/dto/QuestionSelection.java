package ru.tech.smarttest.dto;

import lombok.Data;

@Data
public class QuestionSelection {
    private String playerName;
    private int questionNumber; // Число от 1 до 36
}