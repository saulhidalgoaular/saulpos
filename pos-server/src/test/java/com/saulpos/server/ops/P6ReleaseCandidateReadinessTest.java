package com.saulpos.server.ops;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class P6ReleaseCandidateReadinessTest {

    @Test
    void shouldProvideP6ReleaseCandidateDocument() {
        Path root = repositoryRoot();

        assertTrue(Files.exists(root.resolve("docs/release/P6-release-candidate-and-go-live.md")));
    }

    @Test
    void shouldContainRequiredReleaseCandidateSectionsAndRules() throws IOException {
        Path root = repositoryRoot();
        String content = Files.readString(
                root.resolve("docs/release/P6-release-candidate-and-go-live.md"),
                StandardCharsets.UTF_8);

        assertTrue(content.contains("## Release Candidate Policy"));
        assertTrue(content.contains("Scope freeze"));
        assertTrue(content.contains("Only blocker fixes"));
        assertTrue(content.contains("## RC Input Gates (P1-P5 Evidence)"));
        assertTrue(content.contains("## Go-Live Checklist"));
        assertTrue(content.contains("## Release Sign-Off"));
        assertTrue(content.contains("## Post-Release Validation"));
    }

    @Test
    void shouldReferenceP1ToP5EvidenceDocuments() throws IOException {
        Path root = repositoryRoot();
        String content = Files.readString(
                root.resolve("docs/release/P6-release-candidate-and-go-live.md"),
                StandardCharsets.UTF_8);

        assertTrue(content.contains("docs/uat/P1-end-to-end-uat-scenarios.md"));
        assertTrue(content.contains("docs/uat/P2-performance-and-reliability-hardening.md"));
        assertTrue(content.contains("docs/uat/P3-security-and-compliance-verification.md"));
        assertTrue(content.contains("docs/ops/P4-packaging-deployment-and-operations.md"));
        assertTrue(content.contains("docs/handover/P5-user-guide.md"));
    }

    @Test
    void shouldReferenceP6ReleaseDocumentFromReadme() throws IOException {
        Path root = repositoryRoot();
        String readme = Files.readString(root.resolve("README.md"), StandardCharsets.UTF_8);

        assertTrue(readme.contains("docs/release/P6-release-candidate-and-go-live.md"));
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
