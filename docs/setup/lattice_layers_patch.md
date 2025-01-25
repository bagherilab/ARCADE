---
title: Patch lattice layers
nav_order: 1
parent: Lattice layers
---

# Patch lattice layers
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `layer` tag defines a single layer.
Nested tags include `layer.parameter` for parameters and `layer.operation` for operations.

```xml
<layers>
    <layer id="[ID]">
        <layer.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <layer.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" operation="[OPERATION]" />
        <layer.operation id="[ID]" />
        ...
    </layer>
    ...
</layers>
```

{% assign implementation = "patch" %}
{% include_relative layers/initialization.md implementation=implementation %}
{% include_relative layers/parameter.md implementation=implementation %}
{% include_relative layers/operation.md %}
