---
name: PlaceholderAPI
description: PlaceholderAPI - Minecraft plugin expansion system for placeholder text replacement
---

# PlaceholderAPI Skill

PlaceholderAPI is a powerful placeholder system for Minecraft servers that allows dynamic text replacement through expansions. It enables plugins to provide and consume placeholder values like `%player_name%` or `%server_online%`.

## When to Use This Skill

Trigger this skill when:
- **Creating a PlaceholderExpansion** - Implementing internal or external placeholder expansions for your plugin
- **Integrating PlaceholderAPI** - Adding placeholder parsing capability to an existing plugin
- **Configuring build dependencies** - Setting up Maven/Gradle dependencies for PlaceholderAPI
- **Troubleshooting expansions** - Debugging `NoClassDefFoundError`, expansion loading failures, or registration issues
- **Implementing relational placeholders** - Creating placeholders that compare values between two players
- **Using PlaceholderAPI programmatically** - Parsing placeholders dynamically in Java code
- **Learning eCloud commands** - Downloading, listing, or managing expansions from the cloud repository

## Quick Reference

### 1. Maven Dependency Setup

```xml
<repositories>
    <repository>
        <id>placeholderapi</id>
        <url>https://repo.extendedclip.com/releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.clip</groupId>
        <artifactId>placeholderapi</artifactId>
        <version>2.11.6</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2. Gradle Dependency Setup

```groovy
repositories {
    maven { url 'https://repo.extendedclip.com/releases/' }
}

dependencies {
    compileOnly 'me.clip:placeholderapi:2.11.6'
}
```

### 3. Basic PlaceholderExpansion Structure

```java
package at.helpch.placeholderapi.example.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SomeExpansion extends PlaceholderExpansion {

    @Override
    @NotNull
    public String getAuthor() {
        return "Author"; // Required: Expansion author name
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "example"; // Required: Used as %example_param%
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0"; // Required: Expansion version
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("placeholder1")) {
            return "text1";
        }
        return null; // Return null for invalid placeholders
    }
}
```

### 4. Internal Expansion with Plugin Integration (Recommended)

```java
package at.helpch.placeholderapi.example.expansion;

import at.helpch.placeholderapi.example.SomePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SomeExpansion extends PlaceholderExpansion {

    private final SomePlugin plugin;

    public SomeExpansion(SomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "example";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Keep expansion loaded on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("config_value")) {
            return plugin.getConfig().getString("path.value", "default");
        }
        return null;
    }
}
```

### 5. Registering Internal Expansion in Plugin

```java
package at.helpch.placeholderapi.example;

import at.helpch.placeholderapi.example.expansion.SomeExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SomePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SomeExpansion(this).register();
        }
    }
}
```

### 6. Parsing Placeholders in Code

```java
import me.clip.placeholderapi.PlaceholderAPI;

// Parse with player context
String parsed = PlaceholderAPI.setPlaceholders(player, "Hello %player_name%!");
player.sendMessage(parsed);

// Parse without player (server-level placeholders only)
String serverInfo = PlaceholderAPI.setPlaceholders(null, "Online: %server_online%");
```

### 7. plugin.yml Configuration

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.MyPlugin
author: Author
softdepend:
  - PlaceholderAPI
```

### 8. Handling Multiple Parameters

```java
@Override
public String onRequest(OfflinePlayer player, @NotNull String params) {
    // %example_param1% -> params = "param1"
    // %example_section_value% -> params = "section_value"

    if (params.startsWith("info_")) {
        String type = params.substring(5); // Remove "info_" prefix
        return getInfo(type);
    }

    switch (params.toLowerCase()) {
        case "name":
            return player != null ? player.getName() : "None";
        case "uuid":
            return player != null ? player.getUniqueId().toString() : "None";
        default:
            return null;
    }
}
```

### 9. Relational Placeholder Example

```java
@Override
public String onRequest(OfflinePlayer player, @NotNull String params) {
    // For relational placeholders, override this method
    return null;
}

@Override
public String onPlaceholderRequest(Player player, @NotNull String params) {
    // This method is called when onRequest is not overridden
    return null;
}
```

## Reference Files

This skill includes comprehensive documentation in `references/`:

| File | Description |
|------|-------------|
| **api.md** | API documentation including common issues (NoClassDefFoundError, expansion loading failures) and troubleshooting |
| **developers.md** | Complete developer guide: using PlaceholderAPI in plugins, creating expansions (internal/external/relational), eCloud submission |
| **users.md** | User-facing documentation with complete placeholder list (300+ expansions), command reference, and usage examples |
| **index.md** | Main wiki index with navigation to all sections |

## Key Concepts

### Placeholder Syntax

| Format | Usage | Example |
|--------|-------|---------|
| `%identifier_param%` | Standard format | `%player_name%`, `%server_online%` |
| `{identifier_param}` | Bracket format (used within certain expansions like Animations) | `{player_name}` inside animation tags |

**Important**: When using placeholders within the Animations expansion text, you must use the bracket variant `{player_name}` instead of `%player_name%`.

### Expansion Types

| Type | Description | Registration |
|------|-------------|--------------|
| **Internal** | Built into your plugin's JAR | Manual registration required (recommended) |
| **External** | Separate JAR in `plugins/PlaceholderAPI/expansions/` | Auto-loaded by PlaceholderAPI |
| **Relational** | Compares values between two players | Prefix: `%rel_<identifier>_%` |

### The onRequest Method

- **Purpose**: Called by PlaceholderAPI when a matching placeholder is parsed
- **params parameter**: Contains text after the underscore (e.g., `"param"` in `%example_param%`)
- **Return values**:
  - `null` → Placeholder is invalid
  - `""` (empty string) → Valid placeholder with no value
  - Any other string → The replacement value

### persist() Method

- **Purpose**: Controls whether expansion stays loaded during `/papi reload`
- **Return `true`**: Required for internal expansions to prevent unregistration
- **Return `false`**: Expansion will be unregistered on reload (default for external)

## Working with This Skill

### For Beginners (Getting Started)

1. **Add dependency** to your `pom.xml` or `build.gradle` (see Quick Reference #1 or #2)
2. **Create expansion class** extending `PlaceholderExpansion` (see Quick Reference #3)
3. **Implement required methods**: `getAuthor()`, `getIdentifier()`, `getVersion()`
4. **Override `onRequest()`** to handle placeholder parsing
5. **Register expansion** in your plugin's `onEnable()` (see Quick Reference #5)
6. **Test with command**: `/papi parse me %youridentifier_param%`

### For Plugin Developers (Best Practices)

- **Use internal expansions** for plugin-specific features (better integration, no separate JAR)
- **Always set `persist() = true`** for internal expansions
- **Handle `null` players gracefully** in `onRequest()` (use `OfflinePlayer` when possible)
- **Use `compileOnly`/`provided` scope** - don't shade PlaceholderAPI into your plugin
- **Add soft dependency** in `plugin.yml` to ensure proper load order
- **Consider caching** expensive operations in `onRequest()` for performance

### For Advanced Users (Relational & Complex Expansions)

- **Relational placeholders**: Override appropriate methods, use `%rel_<identifier>_%` syntax
- **Multi-parameter handling**: Parse `params` string with `split("_")` or custom logic
- **Database integration**: Query external data sources in `onRequest()`
- **Conditional placeholders**: Return different values based on player state/world/time

### For Troubleshooting

| Issue | Solution |
|-------|----------|
| Expansion not loading | Check console for `NoClassDefFoundError` - add missing dependencies |
| Placeholder shows as literal text | Verify expansion is registered: `/papi list` |
| `/papi reload` breaks expansion | Ensure `persist()` returns `true` for internal expansions |
| Class loading failure | Ensure all dependencies use `provided`/`compileOnly` scope, don't shade PAPI |

## Common Commands

### Parse Commands

| Command | Description |
|---------|-------------|
| `/papi parse me <text>` | Parse placeholders for yourself |
| `/papi parse <player> <text>` | Parse placeholders for specific player |
| `/papi parse --null <text>` | Parse server-level placeholders only |
| `/papi bcparse <player> <text>` | Parse and broadcast to all players |
| `/papi cmdparse <player> <command>` | Parse and execute as command |
| `/papi parserel <p1> <p2> <text>` | Parse relational placeholders |

### eCloud Commands

| Command | Description |
|---------|-------------|
| `/papi ecloud download <expansion>` | Download expansion from eCloud |
| `/papi ecloud download <expansion> <version>` | Download specific version |
| `/papi ecloud list all` | List all available expansions |
| `/papi ecloud list installed` | List installed expansions |
| `/papi ecloud info <expansion>` | View expansion information |
| `/papi ecloud placeholders <expansion>` | Show expansion's placeholders |
| `/papi ecloud status` | Check eCloud connection status |

### Expansion Management

| Command | Description |
|---------|-------------|
| `/papi list` | List all installed expansions |
| `/papi info <expansion>` | View expansion details |
| `/papi reload` | Reload PlaceholderAPI configuration |
| `/papi dump` | Generate debug dump for pastebin |
| `/papi version` | Show PlaceholderAPI version |

## Resources

### Official Links

- **Wiki**: https://wiki.placeholderapi.com/
- **Spigot Resource**: https://www.spigotmc.org/resources/6245/
- **GitHub Repository**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **Discord Community**: https://discord.gg/placeholderapi
- **eCloud API**: https://extendedclip.com/

### Popular Expansions

Download from eCloud using `/papi ecloud download <name>`:

| Expansion | Purpose |
|-----------|---------|
| **Player** | Basic player information (name, UUID, location, health) |
| **Server** | Server information (TPS, online count, version) |
| **Bungee** | Network-wide player counts for BungeeCord |
| **Vault** | Economy, permission, and chat integration |
| **LuckPerms** | Permission and prefix/suffix support |

## Common Errors and Solutions

### NoClassDefFoundError: com/google/gson/Gson

```
[PlaceholderAPI] Failed to load Expansion class <expansion> (Is a dependency missing?)
[PlaceholderAPI] Cause: NoClassDefFoundError <path>
```

**Cause**: Server running Minecraft 1.8 or older without Gson included.

**Solution**: Use at least Minecraft 1.8.8 which includes the required Gson dependency, or manually add Gson to your classpath.

### Expansions Won't Work After Reload

**Cause**: Internal expansion missing `persist() = true` override.

**Solution**: Add the following to your expansion class:

```java
@Override
public boolean persist() {
    return true;
}
```

## Best Practices

1. **Always use `provided`/`compileOnly` scope** - Never shade PlaceholderAPI into your plugin JAR
2. **Prefer internal expansions** - Better integration, no separate files to manage
3. **Return `null` for invalid placeholders** - Let PlaceholderAPI handle unknown identifiers
4. **Use `OfflinePlayer` instead of `Player`** - Works for offline players and avoids NPEs
5. **Add soft dependency in plugin.yml** - Ensures proper load order
6. **Check if PAPI is enabled** before registering: `Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")`
7. **Keep expansions lightweight** - Focus on specific functionality, avoid heavy operations in `onRequest()`
