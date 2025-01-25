---
title: Patch cell populations
nav_order: 1
parent: Cell populations
---

# Patch cell populations
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `population` tag defines a single population.
Nested tags include `population.parameter` for parameters, `population.process` for processes, and `population.link` for links.

```xml
<populations>
    <population id="[ID]" class="[CLASS]" init="[INIT]">
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" module="[MODULE]" />
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" process="[PROCESS]" />
        <population.process id="[ID]" version="[VERSION]" />
        <population.process id="[ID]" version="[VERSION]" />
        <population.link id="[ID]" weight="[WEIGHT]" />
        ...
    </population>
    ...
</populations>
```

{% assign implementation = "patch" %}
{% include_relative populations/initialization.md implementation=implementation %}
{% include_relative populations/parameter.md implementation=implementation process=true %}
{% include_relative populations/process.md %}
{% include_relative populations/link.md %}
