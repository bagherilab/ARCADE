---
title: Patch environment components
nav_order: 1
parent: Environment components
---

# Patch environment components
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `component` tag defines a single component.
Nested tags include `component.parameter` for parameters and `component.register` for registration to a layer.

```xml
<components>
    <component id="[ID]" class="[CLASS]">
        <component.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <component.register id="[ID]" />
        ...
    </component>
    ...
</components>
```

{% assign implementation = "patch" %}
{% include_relative components/creation.md implementation=implementation %}
{% include_relative components/parameter.md implementation=implementation %}
{% include_relative components/register.md %}
