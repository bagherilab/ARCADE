---
title: Potts cell populations
nav_order: 2
parent: Cell populations
---

# Potts cell populations
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `population` tag defines a single population.
Nested tags include `population.parameter` for parameters, `population.region` for regions, and `population.link` for links.

```xml
<populations>
    <population id="[ID]" class="[CLASS]" init="[INIT]">
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" module="[MODULE]" />
        <population.region id="[ID]" />
        <population.link id="[ID]" weight="[WEIGHT]" />
        ...
    </population>
    ...
</populations>
```

{% assign implementation = "potts" %}
{% include_relative populations/initialization.md implementation=implementation %}
{% include_relative populations/parameter.md implementation=implementation process=false %}
{% include_relative populations/region.md %}
{% include_relative populations/link.md %}
