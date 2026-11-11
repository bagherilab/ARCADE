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
The `potts` implementation defines the component classes `oxygen_source`, `vessels` and `release` (below).
A component with an unknown `class` is skipped without an error.
Components act on lattice layers (for example, a source of a layer), and are stepped after cells and before layers update.

{% assign implementation = "potts" %}
{% include_relative components/creation.md implementation=implementation %}
{% include_relative components/parameter.md implementation=implementation %}
{% include_relative components/register.md %}


## Release component

The `release` class adds an amount to each registered layer in proportion to a source layer.
Each tick, every voxel gains `RATE` &times; s &times; dt, where s is the source layer value at that voxel and dt is the tick length (hours).
The source layer is read every tick, so release follows the source as it changes.
The amount enters the field at the next layer step, before diffusion and decay.
Each registered layer must have a diffuser or a decayer.

| Parameter | Units | Default | Description |
| --- | --- | --- | --- |
| `RATE` | (layer units)/hour per source unit | 0 | release per unit of source; 0 disables release |
| `SOURCE` | | `WOUND_MATRIX` | id of the source layer |

With decay rate k and a source that is wide compared with the diffusion length, the layer settles near `RATE` &times; s / k.

```xml
<component id="INJURY_RELEASE" class="release">
    <component.parameter id="RATE" value="3.6" />
    <component.parameter id="SOURCE" value="WOUND_MATRIX" />
    <component.register id="INJURY_SIGNAL" />
</component>
```
