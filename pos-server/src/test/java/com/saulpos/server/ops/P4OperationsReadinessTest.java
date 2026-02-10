package com.saulpos.server.ops;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class P4OperationsReadinessTest {

    @Test
    void shouldProvideEnvironmentProfilesForDeploymentTargets() {
        Path root = repositoryRoot();

        assertTrue(Files.exists(root.resolve("pos-server/src/main/resources/application-dev.properties")));
        assertTrue(Files.exists(root.resolve("pos-server/src/main/resources/application-staging.properties")));
        assertTrue(Files.exists(root.resolve("pos-server/src/main/resources/application-prod.properties")));
    }

    @Test
    void shouldProvideOperationalScriptsForPackagingDeployBackupAndRestore() {
        Path root = repositoryRoot();

        assertTrue(Files.isExecutable(root.resolve("ops/scripts/package-release.sh")));
        assertTrue(Files.isExecutable(root.resolve("ops/scripts/deploy-server.sh")));
        assertTrue(Files.isExecutable(root.resolve("ops/scripts/backup-postgres.sh")));
        assertTrue(Files.isExecutable(root.resolve("ops/scripts/restore-postgres.sh")));
    }

    @Test
    void shouldDocumentRunbooksWithRequiredSections() throws IOException {
        Path root = repositoryRoot();
        Path runbook = root.resolve("docs/ops/P4-packaging-deployment-and-operations.md");
        Path validation = root.resolve("docs/ops/P4-backup-restore-validation.md");

        assertTrue(Files.exists(runbook));
        assertTrue(Files.exists(validation));

        String runbookContent = Files.readString(runbook, StandardCharsets.UTF_8);
        assertTrue(runbookContent.contains("## Deployment and Startup Runbook"));
        assertTrue(runbookContent.contains("## Backup Runbook"));
        assertTrue(runbookContent.contains("## Restore Runbook"));
        assertTrue(runbookContent.contains("## Rollback Runbook"));
        assertTrue(runbookContent.contains("## Incident Response Runbook"));
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
