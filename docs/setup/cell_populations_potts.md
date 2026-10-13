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
Nested tags include `population.parameter` for parameters, `population.process` for processes, `population.region` for regions, and `population.link` for links.

```xml
<populations>
    <population id="[ID]" class="[CLASS]" init="[INIT]">
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" module="[MODULE]" />
        <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" process="[PROCESS]" />
        <population.process id="[ID]" />
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

## Intracellular processes

The `population.process` tag lists processes for the population.
Processes are stepped every tick, before the module for the cell state, regardless of the cell state.
Valid options include:

`SECRETION`
: Secretes into lattice layers

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | process option id                         |
| `version` | process version (optional)                |

To modify process parameters, use the [`population.parameter`](#population-parameters) tag with the corresponding `process` attribute (for example, `process="secretion"`).

### Secretion process

Each tick, a cell secretes `RATE_[LAYER]` × dt into each [layer]({{site.baseurl}}/setup/lattice_layers_potts) in the setup.
The amount is spread evenly over the voxels set by `REGION_[LAYER]`, and is added to the layer at the next lattice step.
Parameters for a layer that is not in the setup cause an error.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `secretion/RATE_[LAYER]` | 0 | amount/hour | rate of secretion into the layer |
| `secretion/REGION_[LAYER]` | `BOUNDARY` | | voxels secreted into: `BOUNDARY` (voxels adjacent to the cell that the cell does not own) or `INSIDE` (voxels of the cell) |

### Example: Secreting into layers

_Specifies a population that secretes PDGF at 1000 per hour into the voxels around each cell, and ECM at 50 per hour into the voxels of each cell._

```xml
<population.process id="SECRETION" />
<population.parameter process="secretion" id="RATE_PDGF" value="1000" />
<population.parameter process="secretion" id="RATE_ECM" value="50" />
<population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
```

{% include_relative populations/region.md %}
{% include_relative populations/link.md %}
