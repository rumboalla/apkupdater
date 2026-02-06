# Jules Safety & Learnings
This directory tracks general project learnings, safety patterns, and critical insights to help avoid regressions and improve code quality.

## 2024-05-23 - [Safety] Safe List Access in Extensions
**Learning:** Using `get(0)` on a list inside `runCatching` is less efficient and less safe than using `firstOrNull()`. Exception handling for control flow (handling `IndexOutOfBoundsException`) is expensive and should be avoided when a simple null check suffices.
**Action:** Prefer safe accessors like `firstOrNull()`, `getOrNull()`, etc., over index access followed by exception catching.
