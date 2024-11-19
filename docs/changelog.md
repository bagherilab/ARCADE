---
title: Changelog
nav_order: 1
---

{% assign entries = site.changelog | reverse %}

{% for entry in entries %}
  <p>{{ entry.content | markdownify }}</p>
{% endfor %}
