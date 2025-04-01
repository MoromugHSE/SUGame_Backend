package ru.tech.smarttest.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class DebugController {

    @GetMapping("/debug-static")
    public ResponseEntity<String> debugStatic() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }

}
