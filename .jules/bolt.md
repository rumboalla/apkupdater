# Jules Safety & Learnings
This directory tracks general project learnings, safety patterns, and critical insights to help avoid regressions and improve code quality.

## 2024-05-23 - [Safety] Safe List Access in Extensions
**Learning:** Using `get(0)` on a list inside `runCatching` is less efficient and less safe than using `firstOrNull()`. Exception handling for control flow (handling `IndexOutOfBoundsException`) is expensive and should be avoided when a simple null check suffices.
**Action:** Prefer safe accessors like `firstOrNull()`, `getOrNull()`, etc., over index access followed by exception catching.

## 2024-05-23 - [Performance] Order of Filtering Operations
**Learning:** In `FdroidRepository`, checking app signatures (expensive IPC) was done *before* checking version codes (cheap integer comparison). This meant expensive checks were run for every app in the repo, even if no update was available.
**Action:** Always order filter operations from cheapest/most restrictive to most expensive.

## 2024-05-23 - [Performance] O(N) Lookups in Loops
**Learning:** `FdroidRepository` was performing linear searches (`contains`, `find`) on the installed apps list for every app in the F-Droid repository. This resulted in O(M*N) complexity.
**Action:** Convert reference lists to Maps (O(1) lookup) before iterating over large datasets.
