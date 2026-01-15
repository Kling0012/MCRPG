#!/bin/bash
for f in /workspaces/java/Minecraft-Java-Plaugin-forRPG/target/site/jacoco/com.example.rpgplugin.*/index.html; do
  pkg=$(echo "$f" | grep -oP 'com\.example\.rpgplugin\.[^/]+' | sed 's/com.example.rpgplugin.//')
  cov=$(grep -A1 '<tfoot>' "$f" | grep 'ctr2' | tail -1 | grep -oP '\d+(?=%)')
  if [ -n "$cov" ] && [ "$cov" -lt 90 ]; then
    echo "$pkg: $cov%"
  fi
done | sort -t'%' -k2 -n
