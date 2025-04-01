package ru.tech.smarttest.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tech.smarttest.models.Scenario;

import java.io.IOException;

@Service
public class ScenarioService {
    private Scenario scenario;

    private final ObjectMapper mapper = new ObjectMapper();


    @PostConstruct
    public void init() {
        loadScenario();
    }

    public void reloadScenario() {
        loadScenario();
    }

    private void loadScenario() {
        try {
            scenario = mapper.readValue(
                    new ClassPathResource("scenario.json").getInputStream(),
                    Scenario.class
            );
            System.out.println("✅ Scenario loaded successfully!");
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load scenario!", e);
        }
    }

    public Scenario getScenario() {
        return scenario;
    }


    public void reloadScenarioFromFile(MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        scenario = objectMapper.readValue(file.getInputStream(), Scenario.class);
    }

    // Добавь вот этот метод:
    public Scenario getCurrentScenario() {
        return scenario;
    }
}
