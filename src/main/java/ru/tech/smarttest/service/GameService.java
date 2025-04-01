package ru.tech.smarttest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tech.smarttest.models.PlayerJoinStatus;
import ru.tech.smarttest.models.Scenario;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final ScenarioService scenarioService;

    private final AtomicBoolean hostConnected = new AtomicBoolean(false);

    // Используем Set для уникальности
    private final Set<String> connectedPlayers = ConcurrentHashMap.newKeySet();

    @Autowired
    public GameService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    public boolean addHost() {
        return hostConnected.compareAndSet(false, true);
    }

    public boolean isHostPresent() {
        return hostConnected.get();
    }

    public boolean removeHost() {
        return hostConnected.compareAndSet(true, false);
    }



    // Получить список доступных игроков
    public List<String> getAvailablePlayers() {
        return scenarioService.getScenario()
                .getPlayers()
                .stream()
                .map(Scenario.PlayerData::getName)
                .collect(Collectors.toList());
    }




    // Подключение игрока
    public PlayerJoinStatus joinPlayer(String playerName) {
        List<String> availablePlayers = getAvailablePlayers();

        if (!availablePlayers.contains(playerName))
            return PlayerJoinStatus.NOT_FOUND;

        if (connectedPlayers.contains(playerName))
            return PlayerJoinStatus.ALREADY_JOINED;

        if (connectedPlayers.size() >= availablePlayers.size())
            return PlayerJoinStatus.LIMIT_REACHED;

        connectedPlayers.add(playerName);
        return PlayerJoinStatus.SUCCESS;
    }

    // Отключение игрока (опционально)
    public boolean leavePlayer(String playerName) {
        return connectedPlayers.remove(playerName);
    }


    public List<String> getTheme2Round() {
        return scenarioService.getScenario().getRound2().getThemes();
    }
}
