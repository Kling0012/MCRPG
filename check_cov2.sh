#!/bin/bash
for pkg in skill.component skill skill.component.condition skill.component.cost skill.component.filter skill.component.mechanic skill.component.placement skill.component.target skill.executor; do
  f="/workspaces/java/Minecraft-Java-Plaugin-forRPG/target/site/jacoco/com.example.rpgplugin.$pkg/index.html"
  if [ -f "$f" ]; then
    cov=$(grep -oP 'Total.*?<td class="ctr2">\K\d+(?=%)' "$f" | head -1)
    echo "$pkg: $cov%"
  fi
done
