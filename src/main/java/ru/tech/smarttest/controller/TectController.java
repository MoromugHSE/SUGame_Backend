package ru.tech.smarttest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class TectController {

    @GetMapping("/stendboard")
    public ResponseEntity<String> debugStatic() throws IOException {
        String html = new String(Files.readAllBytes(Paths.get("src/main/resources/static/test.html")));
        return ResponseEntity.ok(html);
    }
}

