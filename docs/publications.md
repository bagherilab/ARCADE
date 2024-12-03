---
title: Publications
nav_order: 1
---

# Publications

{% assign entries = site.publications | reverse %}

{% for entry in entries %}
## {{ entry.title }}

{{ entry.authors }}. _{{ entry.journal }}_. DOI: [{{ entry.doi }}](https://doi.org/{{ entry.doi }})

{{ entry.content }}
{% endfor %}
