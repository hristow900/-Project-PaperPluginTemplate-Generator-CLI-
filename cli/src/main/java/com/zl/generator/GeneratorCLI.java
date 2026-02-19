package com.zl.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GeneratorCLI {
    private static final String COMMAND_NEW = "new";

    private GeneratorCLI() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        if (!COMMAND_NEW.equals(args[0])) {
            System.err.println("Unknown command: " + args[0]);
            printUsage();
            System.exit(1);
        }

        if (args.length != 4) {
            System.err.println("Invalid number of arguments for 'new'.");
            printUsage();
            System.exit(1);
        }

        String pluginName = args[1].trim();
        String groupId = args[2].trim();
        String artifactId = args[3].trim();

        validateArguments(pluginName, groupId, artifactId);

        try {
            Path templateRoot = resolveTemplateRoot();
            Path outputDir = Path.of(artifactId).toAbsolutePath().normalize();
            if (Files.exists(outputDir)) {
                throw new IllegalStateException("Output directory already exists: " + outputDir);
            }

            copyDirectory(templateRoot, outputDir);
            applyPlaceholders(outputDir, placeholderValues(pluginName, groupId, artifactId));

            System.out.println("Plugin scaffold created at: " + outputDir);
        } catch (Exception exception) {
            System.err.println("Failed to create plugin scaffold: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void validateArguments(String pluginName, String groupId, String artifactId) {
        if (pluginName.isBlank() || groupId.isBlank() || artifactId.isBlank()) {
            throw new IllegalArgumentException("PluginName, GroupId, and ArtifactId must not be blank.");
        }
    }

    private static Map<String, String> placeholderValues(String pluginName, String groupId, String artifactId) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("__PLUGIN_NAME__", pluginName);
        placeholders.put("__GROUP_ID__", groupId);
        placeholders.put("__ARTIFACT_ID__", artifactId);
        return placeholders;
    }

    private static Path resolveTemplateRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();

        for (Path probe = current; probe != null; probe = probe.getParent()) {
            Path candidate = probe.resolve(Path.of("template", "plugin-template"));
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Template directory not found from working directory: " + current);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(dir));
                Files.createDirectories(destination);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path destination = target.resolve(source.relativize(file));
                Files.copy(file, destination, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void applyPlaceholders(Path root, Map<String, String> placeholders) throws IOException {
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> replacePlaceholders(path, placeholders));
        }
    }

    private static void replacePlaceholders(Path file, Map<String, String> placeholders) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String updated = content;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                updated = updated.replace(entry.getKey(), entry.getValue());
            }
            if (!content.equals(updated)) {
                Files.writeString(file, updated, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            // Non-text or unreadable files are ignored to keep generation resilient.
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  ppg new <PluginName> <GroupId> <ArtifactId>");
    }
}
