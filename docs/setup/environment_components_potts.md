---
title: Potts environment components
nav_order: 2
parent: Environment components
---

# Potts environment components
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

{: .note }
No component classes are defined for the `potts` implementation yet.
Component entries are parsed and checked, but a component with an unknown `class` is skipped without an error.
Components act on lattice layers (for example, a source of a layer), and are stepped after cells and before layers update.

{% assign implementation = "potts" %}
{% include_relative components/creation.md implementation=implementation %}
{% include_relative components/parameter.md implementation=implementation %}
{% include_relative components/register.md %}
