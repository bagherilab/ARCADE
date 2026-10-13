---
title: Potts lattice layers
nav_order: 2
parent: Lattice layers
---

# Potts lattice layers
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `layer` tag defines a single layer.
Nested tags include `layer.parameter` for parameters.

```xml
<layers>
    <layer id="[ID]">
        <layer.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        ...
    </layer>
    ...
</layers>
```

Each potts layer is a field with one value per voxel, the same size as the potts grid (`length` × `width` × `height`).
Values are concentrations (amount/µm³) and are shared by cells and empty space.

{: .warning }
Operations are not yet available for potts layers.
`layer.operation` tags and operation-specific parameters are ignored, so layer values stay constant over the simulation.

{% assign implementation = "potts" %}
{% include_relative layers/initialization.md implementation=implementation %}
{% include_relative layers/parameter.md implementation=implementation %}

## Saving and loading layers

Use `--savelayers` to write a `.LAYERS` snapshot at every snapshot interval.
Use `--loadpath [PREFIX] --loadlayers` to start a simulation from a saved `.LAYERS` snapshot.
Layers in the setup file but not in the snapshot start at their initial values.
Loading a snapshot that contains a layer not in the setup file, or a layer with a different size, causes an error.

Set `offset="[TICKS]"` on the `series` tag of the restarted simulation so output files are labeled with the tick of the original simulation.
