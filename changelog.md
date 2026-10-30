* Fix JSON5 packet encoding when creating or editing quests, tasks and rewards on the 26.1 branch.
* Port the full feature set from NeoForge 1.21.1 to Fabric 1.20.1, Forge 1.20.1 and Fabric 1.21.1.
* Add NeoForge and Fabric builds for the 26.1 branch, built and tested on Minecraft 26.1.2.
* Add a Player NBT task with a fixed 60-tick check interval, partial field matching and optional exact full NBT matching.
* Add a searchable NBT field selector and manual SNBT input for nested fields and mod data saved with the player.
* Show configured NBT fields with checkmarks, required values and distinct colors, while preserving numeric tag types and other filter conditions.
* Add server-wide progress as an option on existing FTB tasks, shared across all teams with Auto, Sum and Best aggregation modes.
* Add minimum personal contribution requirements for claiming rewards from server-wide goals.
* Handle inventory snapshots and statistics without repeatedly counting the same progress.
* Add timed challenges as an option on existing tasks, with a time limit and countdown starting on first progress or when the task becomes available.
* Add optional challenge resets on participant death, damage or logout, and count statistics from the current attempt's baseline.
* Add daily and weekly schedules to quest settings, with configurable reset time, time zone and weekday.
* Add quest rotation pools with a configurable number of active quests and stable selection for each team and calendar period.
* Add calendar streak rewards with regular items and bonus items every configured number of consecutive completed periods.
* Add attribute bonus rewards with selectable attributes and operations, stacking policies, bonus caps and permanent or timed effects.
* Preserve progress, personal contributions, challenge deadlines, calendar streaks and attribute bonuses across server restarts; timed effects continue to expire while offline.
* Add enable switches that preserve configured values and immediately update the availability of dependent settings.
* Add tooltips for personal contribution, challenge status, remaining time, calendar availability, streaks and the next reset.
* Add `/extraquests shared reset <task>` and its `/equests` alias to reset shared progress, contributions and related quest progress and reward claims.
* Count task actions regardless of the player's game mode.
* Add English and Russian translations for the new tasks, rewards, settings, tooltips and NBT editor.
* Synchronize extended quest state to clients and clear cached state when disconnecting.
* Fix reward eligibility, calendar progress checks and dependent settings on 1.20.1, and use native FTB networking for team-data synchronization.
* Adapt 26.1 builds to FTB JSON5 storage, current command permissions, networking and Minecraft NBT APIs.
* Split version-specific shared logic into common20, common21 and common261 modules without introducing a core module.
* Add client and dedicated-server development runs and CurseForge publishing tasks for all implemented loaders.
* Collect current release JARs and remove outdated builds of the same Minecraft version and loader.
* Add automated tests for calendar scheduling, quest rotation and progress arithmetic.
* Update Gradle, build plugins, loaders, FTB dependencies and ExtraLib.
* Adapt key-value rewards to the current FTB Quests claim API on 1.21.1.
* Respect repeat cooldowns when ignoring quest requirements and prevent key-value progress overflow.
* Preserve the target player context when updating key-value progress and claiming rewards.
* Match blocked console commands by name, including aliases, namespaces and nested execute commands.
* Keep both active configurations unchanged when reload fails and log the full error cause.