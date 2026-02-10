package com.saulpos.server.ops;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class P5DocumentationHandoverReadinessTest {

    @Test
    void shouldProvideP5HandoverDocuments() {
        Path root = repositoryRoot();

        assertTrue(Files.exists(root.resolve("docs/handover/P5-user-guide.md")));
        assertTrue(Files.exists(root.resolve("docs/handover/P5-api-reference-and-integration-notes.md")));
        assertTrue(Files.exists(root.resolve("docs/handover/P5-architecture-and-maintenance-guide.md")));
    }

    @Test
    void shouldContainRequiredP5Sections() throws IOException {
        Path root = repositoryRoot();
        String userGuide = Files.readString(
                root.resolve("docs/handover/P5-user-guide.md"),
                StandardCharsets.UTF_8);
        String apiGuide = Files.readString(
                root.resolve("docs/handover/P5-api-reference-and-integration-notes.md"),
                StandardCharsets.UTF_8);
        String architectureGuide = Files.readString(
                root.resolve("docs/handover/P5-architecture-and-maintenance-guide.md"),
                StandardCharsets.UTF_8);

        assertTrue(userGuide.contains("## 1. Cashier Daily Workflow"));
        assertTrue(userGuide.contains("## 2. Manager Daily Workflow"));
        assertTrue(userGuide.contains("## 3. Admin Workflow"));

        assertTrue(apiGuide.contains("## 1. API Foundation"));
        assertTrue(apiGuide.contains("## 2. Core Endpoint Groups"));
        assertTrue(apiGuide.contains("## 3. Integration Notes"));

        assertTrue(architectureGuide.contains("## 1. Architecture Overview"));
        assertTrue(architectureGuide.contains("## 2. Data and Migrations"));
        assertTrue(architectureGuide.contains("## 4. Maintenance Workflow"));
    }

    @Test
    void shouldReferenceP5DocumentsFromReadme() throws IOException {
        Path root = repositoryRoot();
        String readme = Files.readString(root.resolve("README.md"), StandardCharsets.UTF_8);

        assertTrue(readme.contains("docs/handover/P5-user-guide.md"));
        assertTrue(readme.contains("docs/handover/P5-api-reference-and-integration-notes.md"));
        assertTrue(readme.contains("docs/handover/P5-architecture-and-maintenance-guide.md"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.exists(current.resolve("ROADMAP.md"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Repository root with ROADMAP.md not found");
        }
        return current;
    }
}
