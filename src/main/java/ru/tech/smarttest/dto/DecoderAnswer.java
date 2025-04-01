package ru.tech.smarttest.dto;


import lombok.Data;

@Data
public class DecoderAnswer {

    private String playerName;
    private String answer;
    private long answerTime; // Время ответа игрока (в миллисекундах)
}
