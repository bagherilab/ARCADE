---
title: Changelog
nav_order: 3
---

# Changelog

{% assign entries = site.changelog | reverse %}

{% for entry in entries %}
  <p>{{ entry.content | markdownify }}</p>
{% endfor %}
