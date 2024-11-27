---
title: Changelog
nav_order: 2
---

# Changelog

{% assign entries = site.changelog | reverse %}

{% for entry in entries %}
  <p>{{ entry.content | markdownify }}</p>
{% endfor %}
