package ru.tech.smarttest.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlayerThemeChoice {
    private String playerName;
    private List<String> chosenThemes;
}