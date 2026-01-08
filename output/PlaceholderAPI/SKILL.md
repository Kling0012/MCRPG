---
name: PlaceholderAPI
description: PlaceholderAPI - Minecraft plugin expansion system for placeholder text replacement
---

# Placeholderapi Skill

Placeholderapi - minecraft plugin expansion system for placeholder text replacement, generated from official documentation.

## When to Use This Skill

This skill should be triggered when:
- Working with PlaceholderAPI
- Asking about PlaceholderAPI features or APIs
- Implementing PlaceholderAPI solutions
- Debugging PlaceholderAPI code
- Learning PlaceholderAPI best practices

## Quick Reference

### Common Patterns

**Pattern 1:** Example: %unicode_1000% would show က

```
%unicode_1000%
```

**Pattern 2:** Example: %unixtime_1750277249389_dd.MM.yyyy-HH:mm:ss% would show 18.06.2025 20:07:29

```
%unixtime_1750277249389_dd.MM.yyyy-HH:mm:ss%
```

**Pattern 3:** Global achievement placeholders: %aach_achievements% - Return the total unlocked achievements (number) %aach_achievements_percentage% - Return the total unlocked achievements (%) %aach_total_achievements% - Return the total achievements (number) Normal achievement placeholders for individual player statistics: %aach_connections% %aach_deaths% %aach_arrows% %aach_snowballs% %aach_eggs% %aach_fish% %aach_treasures% %aach_itembreaks% %aach_eatenitems% %aach_shear% %aach_milk% %aach_lavabuckets% %aach_waterbuckets% %aach_trades% %aach_anvilsused% %aach_enchantments% %aach_beds% %aach_maxlevel% %aach_consumedpotions% %aach_playedtime% %aach_itemdrops% %aach_itempickups% %aach_hoeplowings% %aach_fertilising% %aach_taming% %aach_brewing% %aach_fireworks% %aach_musicdiscs% %aach_enderpearls% %aach_smelting% %aach_petmastergive% %aach_petmasterreceive% %aach_distancefoot% %aach_distancepig% %aach_distancehorse% %aach_distanceminecart% %aach_distanceboat% %aach_distancegliding% %aach_distancellama% %aach_distancesneaking% %aach_raidswon% %aach_riptides% %aach_advancementscompleted% Multiple Achievement Placeholders for individual player statistics: %aach_places_[blockname]% - example: %aach_places_dirt% %aach_breaks_[blockname]% - example: %aach_breaks_stone% %aach_kills_[entityname]% - example: %aach_kills_zombie% %aach_targetsshot_[targetname]% - example: %aach_targetsshot_zombie% %aach_crafts_[itemname]% - example: %aach_crafts_bread% %aach_breeding_[entityname]% - example: %aach_breeding_pig% %aach_playercommands_[command]% - example: %aach_playercommands_aach list% %aach_custom_[customname]% - example: %aach_custom_votes% %aach_jobsreborn_[job]% - example: %aach_jobsreborn_hunter% Placeholders for total category achievements: %aach_total_connections% %aach_total_deaths% %aach_total_arrows% %aach_total_snowballs% %aach_total_eggs% %aach_total_fish% %aach_total_treasures% %aach_total_itembreaks% %aach_total_eatenitems% %aach_total_shear% %aach_total_milk% %aach_total_lavabuckets% %aach_total_waterbuckets% %aach_total_trades% %aach_total_anvilsused% %aach_total_enchantments% %aach_total_beds% %aach_total_maxlevel% %aach_total_consumedpotions% %aach_total_playedtime% %aach_total_itemdrops% %aach_total_itempickups% %aach_total_hoeplowings% %aach_total_fertilising% %aach_total_taming% %aach_total_brewing% %aach_total_fireworks% %aach_total_musicdiscs% %aach_total_enderpearls% %aach_total_smelting% %aach_total_petmastergive% %aach_total_petmasterreceive% %aach_total_distancefoot% %aach_total_distancepig% %aach_total_distancehorse% %aach_total_distanceminecart% %aach_total_distanceboat% %aach_total_distancegliding% %aach_total_distancellama% %aach_total_distancesneaking% %aach_total_raidswon% %aach_total_riptides% %aach_total_advancementscompleted% %aach_total_places% %aach_total_breaks% %aach_total_kills% %aach_total_targetsshot% %aach_total_crafts% %aach_total_breeding% %aach_total_playercommands% %aach_total_custom% %aach_total_commands% %aach_total_jobsreborn%

```
%aach_achievements% - Return the total unlocked achievements (number)
%aach_achievements_percentage% - Return the total unlocked achievements (%)
%aach_total_achievements% - Return the total achievements (number)
```

**Pattern 4:** Multiverse-Core v5Multiverse-Core v4 Built into Plugin The below Placeholders are only for Multiverse-Core v5! All placeholders allow a _<world> to be added with <world> being the name of a Multiverse-loaded World. Example: %multiverse-core_alias_myworld% %multiverse-core_alias% %multiverse-core_animalspawn% %multiverse-core_autoheal% %multiverse-core_blacklist% %multiverse-core_currency% %multiverse-core_difficulty% %multiverse-core_entryfee% %multiverse-core_environment% %multiverse-core_flight% %multiverse-core_gamemode% %multiverse-core_generator% %multiverse-core_hunger% %multiverse-core_monstersspawn% %multiverse-core_name% %multiverse-core_playerlimit% %multiverse-core_price% %multiverse-core_pvp% %multiverse-core_seed% %multiverse-core_time% %multiverse-core_type% %multiverse-core_weather% papi ecloud download multiverse %multiverse_world_alias% %multiverse_world_all_property_names% %multiverse_world_generator% %multiverse_world_name% %multiverse_world_name_colored% %multiverse_world_permissible_name% %multiverse_world_time% %multiverse_world_animals_spawn_enabled% %multiverse_world_monsters_spawn_enabled% %multiverse_world_access_permission% %multiverse_world_adjust_spawn_enabled% %multiverse_world_allow_flight_enabled% %multiverse_world_auto_heal_enabled% %multiverse_world_auto_load_enabled% %multiverse_world_bed_respawn_enabled% %multiverse_world_color% %multiverse_world_currency% %multiverse_world_difficulty% %multiverse_world_environment% %multiverse_world_gamemode% %multiverse_world_hunger_enabled% %multiverse_world_player_limit% %multiverse_world_price% %multiverse_world_seed% %multiverse_world_style% %multiverse_world_type%

```
_<world>
```

**Pattern 5:** Description: Parses placeholders of a String and broadcasts the result to all players. Arguments: <player|me|--null> - The Player to parse values of the placeholder (Use me for yourself and --null to force a null player (Useful for consoles)). <text> - Text with placeholders to parse. Example: /papi bcparse funnycube My name is %player_name%!

```
<player|me|--null>
```

**Pattern 6:** Example: /papi bcparse funnycube My name is %player_name%!

```
/papi bcparse funnycube My name is %player_name%!
```

**Pattern 7:** Example: /papi cmdparse funnycube say My name is %player_name%!

```
/papi cmdparse funnycube say My name is %player_name%!
```

### Example Code Patterns

**Example 1** (jsx):
```jsx
%animations_<tag>Text</tag>%
%animations_<tag option>Text</tag>%
%animations_<tag option=:value>Text</tag>%
```

**Example 2** (json):
```json
repositories {
    maven {
        url = 'https://repo.extendedclip.com/releases/'
    }
}

dependencies {
    compileOnly 'me.clip:placeholderapi:{version}'
}
```

**Example 3** (json):
```json
[00:00:01 ERROR]: [PlaceholderAPI] Failed to load Expansion class <expansion> (Is a dependency missing?)
[00:00:01 ERROR]: [PlaceholderAPI] Cause: NoClassDefFoundError <path>
```

## Reference Files

This skill includes comprehensive documentation in `references/`:

- **api.md** - Api documentation
- **developers.md** - Developers documentation
- **users.md** - Users documentation

Use `view` to read specific reference files when detailed information is needed.

## Working with This Skill

### For Beginners
Start with the getting_started or tutorials reference files for foundational concepts.

### For Specific Features
Use the appropriate category reference file (api, guides, etc.) for detailed information.

### For Code Examples
The quick reference section above contains common patterns extracted from the official docs.

## Resources

### references/
Organized documentation extracted from official sources. These files contain:
- Detailed explanations
- Code examples with language annotations
- Links to original documentation
- Table of contents for quick navigation

### scripts/
Add helper scripts here for common automation tasks.

### assets/
Add templates, boilerplate, or example projects here.

## Notes

- This skill was automatically generated from official documentation
- Reference files preserve the structure and examples from source docs
- Code examples include language detection for better syntax highlighting
- Quick reference patterns are extracted from common usage examples in the docs

## Updating

To refresh this skill with updated documentation:
1. Re-run the scraper with the same configuration
2. The skill will be rebuilt with the latest information
