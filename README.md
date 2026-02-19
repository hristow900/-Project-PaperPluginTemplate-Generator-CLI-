# paper-plugin-generator

A Java 21 CLI tool that scaffolds Paper plugin projects from a template.

## Requirements

- Java 21
- Gradle 8+

## Build

```bash
gradle build
```

## Run

```bash
gradle :cli:run --args="new TestPlugin com.test testplugin"
```

Or build an installable CLI:

```bash
gradle :cli:installDist
./cli/build/install/ppg/bin/ppg new TestPlugin com.test testplugin
```

## Command

```text
ppg new <PluginName> <GroupId> <ArtifactId>
```

Example:

```bash
ppg new TestPlugin com.test testplugin
```

This will:

1. Copy `template/plugin-template`
2. Replace placeholders:
   - `__PLUGIN_NAME__`
   - `__GROUP_ID__`
   - `__ARTIFACT_ID__`
3. Output a new plugin folder named after `<ArtifactId>`.

## Project layout

```text
paper-plugin-generator/
  template/
    plugin-template/
      build.gradle
      settings.gradle
      src/main/java/
      src/main/resources/plugin.yml
  cli/
    src/main/java/com/zl/generator/GeneratorCLI.java
```
