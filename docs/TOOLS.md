# Tools

## ai-context-proxy Multitool

`tools/ai-context-proxy/` is a local context multitool for coding agents. It gives compact repository maps, file summaries, exact ranges, symbol reads, compressed searches, compact git status/diff output, and short Gradle/Minecraft/test log summaries.

Use it when a normal command would dump too much context:

```bash
tools/ai-context-proxy/bin/ctx repo-map
tools/ai-context-proxy/bin/ctx file src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java
tools/ai-context-proxy/bin/ctx search "CombatStance" src/main/java
tools/ai-context-proxy/bin/ctx log -- ./gradlew compileJava
```

The tool is a guardrail, not a compiler or sandbox. Before editing code, read the exact symbol or exact line range you intend to change.

Full tool notes live in `../tools/ai-context-proxy/README.md`.
