package ru.tech.smarttest.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private String playerName;   // кто ответил
    private String answer;       // вариант ответа
}
