package ru.tech.smarttest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.tech.smarttest.models.Scenario;
import ru.tech.smarttest.service.ScenarioService;

import java.io.IOException;

@RestController
public class ScenarioController {

    @Autowired
    private ScenarioService scenarioService;

    @GetMapping("/scenario")
    public Scenario getScenario() {
        return scenarioService.getScenario();
    }



    @PostMapping("/scenario/download")
    public ResponseEntity<String> reloadScenario(@RequestParam("file") MultipartFile file) {
        try {
            scenarioService.reloadScenarioFromFile(file);
            return ResponseEntity.ok("✅ Сценарий успешно загружен и обновлен.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("❌ Ошибка при загрузке файла сценария: " + e.getMessage());
        }
    }
}
