## 2024-05-23 - [Safety] Safe List Access in Extensions
**Learning:** Using `get(0)` on a list inside `runCatching` is less efficient and less safe than using `firstOrNull()`. Exception handling for control flow (handling `IndexOutOfBoundsException`) is expensive and should be avoided when a simple null check suffices.
**Action:** Prefer safe accessors like `firstOrNull()`, `getOrNull()`, etc., over index access followed by exception catching.
