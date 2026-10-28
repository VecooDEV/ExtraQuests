* Update Gradle, build plugins, loaders, FTB dependencies and ExtraLib.
* Adapt key-value rewards to the current FTB Quests claim API on 1.21.1.
* Respect repeat cooldowns when ignoring quest requirements and prevent key-value progress overflow.
* Preserve the target player context when updating key-value progress and claiming rewards.
* Match blocked console commands by name, including aliases, namespaces and nested execute commands.
* Keep both active configurations unchanged when reload fails and log the full error cause.
