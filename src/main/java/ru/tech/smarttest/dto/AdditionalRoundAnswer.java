package ru.tech.smarttest.dto;


import lombok.Data;

import java.util.List;

@Data
public class AdditionalRoundAnswer {
    private String playerName;
    private List<List<String>> matchedPairs; // [[leftPart, rightPart], ...]
}
