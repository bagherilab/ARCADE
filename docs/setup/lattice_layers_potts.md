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

Each potts layer is a field with one value per voxel, the same size as the potts grid (`length` × `width` × `height`).
Values are concentrations (amount/µm³) and are shared by cells and empty space.

{% assign implementation = "potts" %}
{% include_relative layers/initialization.md implementation=implementation %}
{% include_relative layers/parameter.md implementation=implementation %}

## Layer operations

The `layer.operation` tag lists operations on the layer, stepped each tick after cells: diffuser, then decayer.
The degrader is stepped after all other lattice operations, so it uses the enzyme's end-of-tick value.
Valid options include:

`DIFFUSER`
: Diffuses the quantity with no-flux boundaries (2D only)

`DECAYER`
: Decays the quantity at a first-order rate

`DEGRADER`
: Degrades this layer at a rate proportional to the value of the `ENZYME` layer in the same voxel; the enzyme layer is not consumed

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `diffuser/DIFFUSIVITY` | 0 | µm²/s | diffusivity of the quantity |
| `diffuser/MAX_SUBSTEP` | 2 | s | maximum diffusion substep |
| `decayer/DECAY_RATE` | 0 | 1/hour | first-order decay rate |
| `degrader/ENZYME` | `NONE` | | layer id of the enzyme that degrades this layer |
| `degrader/RATE` | 0 | 1/(concentration·hour) | degradation rate per unit enzyme |

The degrader multiplies each voxel by exp(−rate × dt × enzyme).
The enzyme must be another layer in the setup, or the simulation fails at setup.

Unlike the patch operations, which are sized for the one-minute patch tick, potts operations support any tick length: diffusion uses an implicit scheme that is stable for any diffusivity, and decay uses the exact factor exp(-rate × dt).
Smaller `MAX_SUBSTEP` values are more accurate next to secreting cells but slower.

### Example: Diffusing and decaying layer

_Specifies a layer that diffuses at 1.33 µm²/s and decays at 1.33/hour._

```xml
<layer id="PDGF">
    <layer.operation id="DIFFUSER" />
    <layer.operation id="DECAYER" />
    <layer.parameter operation="diffuser" id="DIFFUSIVITY" value="1.33" />
    <layer.parameter operation="decayer" id="DECAY_RATE" value="1.33" />
</layer>
```

### Example: Enzyme-degraded layer

_Specifies a matrix layer (ECM) degraded by a diffusing enzyme layer (MMP) at 0.05 per unit enzyme per hour._

```xml
<layer id="MMP">
    <layer.operation id="DIFFUSER" />
    <layer.operation id="DECAYER" />
    <layer.parameter operation="diffuser" id="DIFFUSIVITY" value="0.6" />
    <layer.parameter operation="decayer" id="DECAY_RATE" value="6" />
</layer>
<layer id="ECM">
    <layer.operation id="DEGRADER" />
    <layer.parameter operation="degrader" id="ENZYME" value="MMP" />
    <layer.parameter operation="degrader" id="RATE" value="0.05" />
</layer>
```

## Saving and loading layers

Use `--savelayers` to write a `.LAYERS` snapshot at every snapshot interval.
Use `--loadpath [PREFIX] --loadlayers` to start a simulation from a saved `.LAYERS` snapshot.
Layers in the setup file but not in the snapshot start at their initial values.
Loading a snapshot that contains a layer not in the setup file, or a layer with a different size, causes an error.

Set `offset="[TICKS]"` on the `series` tag of the restarted simulation so output files are labeled with the tick of the original simulation.
