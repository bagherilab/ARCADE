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

## Wound healing cells

The `fibroblast`, `myofibroblast`, `macrophage`, and `tissue` classes model wound healing.
Each tick, a wound healing cell applies the rules for its class (quiescent and proliferative cells only), steps its processes, and then steps the module for its state.
The rules read the mean `PDGF`, `ECM`, and `INF` layer values over the cell; a layer that is not in the setup reads as 0.

| CLASS | RULES |
| ----- | ----- |
| `fibroblast` | Proliferates when PDGF is above `PDGF_PROLIFERATION`. Differentiates into the `DIFFERENTIATION_TARGET` population when PDGF and ECM are above their thresholds. Becomes senescent spontaneously or through the bystander routes. |
| `myofibroblast` | Proliferates under the same conditions as fibroblasts. Becomes apoptotic when INF is above `INF_APOPTOSIS`. Matures after a mean of `MATURATION`; mature cells become apoptotic when ECM is below `ECM_TENSION`, or senescent with probability `SENESCENCE_RATE`. Becomes senescent through the bystander routes. |
| `macrophage` | Does not proliferate. Senescent cells that touch a macrophage may be cleared. |
| `tissue` | Proliferates into free space next to the cell when the mean ECM over that space is at least `ECM_PROLIFERATION`. Becomes senescent spontaneously or through the bystander routes. |

A quiescent cell becomes proliferative when it can proliferate.
A proliferative cell returns to quiescent if it can no longer proliferate during G1; after G1, it is committed to dividing.
Fibroblasts and myofibroblasts need PDGF above `PDGF_PROLIFERATION` to start proliferating, and at least half of it to continue.
All classes except macrophages are blocked from proliferating when the free fraction of voxels around the cell is below `CONTACT_FREE_FRACTION`.

Differentiation replaces the fibroblast with a cell of the target population that has the same id, age, divisions, state, and location.
The target must be a `myofibroblast` population, or the simulation fails at setup.
Population links are used for daughter cells on division, not for differentiation.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `CONTACT_FREE_FRACTION` | 0 | | minimum free fraction of voxels around the cell to proliferate; 0 disables contact inhibition |
| `SPONTANEOUS_SENESCENCE` | 0 | 1/hour | probability of spontaneous senescence |
| `BYSTANDER_CONTACT` | 0 | 1/hour | probability of senescence on contact with a senescent cell |
| `BYSTANDER_PARACRINE` | 0 | 1/hour | probability of senescence when INF is above `INF_BYSTANDER` |
| `INF_BYSTANDER` | 1000 | concentration | threshold on INF for paracrine senescence |
| `PDGF_PROLIFERATION` | 0.1 | concentration | threshold on PDGF for proliferation |
| `PDGF_DIFFERENTIATION` | 0.1 | concentration | threshold on PDGF for differentiation |
| `ECM_DIFFERENTIATION` | 0.5 | concentration | threshold on ECM for differentiation |
| `DIFFERENTIATION_RATE` | 0.2 | 1/hour | probability of differentiation above thresholds |
| `DIFFERENTIATION_TARGET` | `NONE` | | population id fibroblasts differentiate into |
| `INF_APOPTOSIS` | 5 | concentration | threshold on INF for myofibroblast apoptosis |
| `MATURATION` | 72 | hours | mean time to myofibroblast maturation |
| `ECM_TENSION` | 0.2 | concentration | threshold on ECM below which mature myofibroblasts become apoptotic |
| `SENESCENCE_RATE` | 0.02 | 1/hour | probability of senescence for mature myofibroblasts |
| `ECM_PROLIFERATION` | 0 | concentration | minimum mean ECM over free voxels for tissue proliferation; 0 disables the gate |

### Senescence

Senescent cells do not divide.
They start in the `SENESCENT_EARLY` phase and move to `SENESCENT_LATE` after `STEPS_EARLY` steps at `RATE_EARLY` (a mean of `STEPS_EARLY` / `RATE_EARLY` hours).
On each phase change, the secretion rate for each layer is set to `EARLY_RATE_[LAYER]` or `LATE_RATE_[LAYER]`; the secretion region does not change.
Each tick, a senescent cell may be cleared (becomes apoptotic) at a basal rate, by a touching macrophage at a rate that depends on the phase, or by a senolytic drug during the drug window.

To start a cell in late senescence, load it from a `.CELLS` file with `"state": "SENESCENT"` and `"phase": "SENESCENT_LATE"`.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `senescence/RATE_EARLY` | 0.139 | steps/hour | rate of events in early senescence |
| `senescence/STEPS_EARLY` | 10 | steps | steps in early senescence |
| `senescence/EARLY_RATE_[LAYER]` | 0 | amount/hour | secretion rate into the layer in early senescence |
| `senescence/LATE_RATE_[LAYER]` | 0 | amount/hour | secretion rate into the layer in late senescence |
| `senescence/CLEARANCE_EARLY` | 0 | 1/hour | probability of clearance by a touching macrophage in early senescence |
| `senescence/CLEARANCE_LATE` | 0.1 | 1/hour | probability of clearance by a touching macrophage in late senescence |
| `senescence/BASAL_CLEARANCE` | 0 | 1/hour | probability of clearance without macrophage contact |
| `senescence/DRUG_START` | 0 | hours | start of the drug window |
| `senescence/DRUG_DURATION` | 0 | hours | duration of the drug window |
| `senescence/DRUG_KILL` | 0 | 1/hour | probability of clearance by the drug during the window |

### Example: Fibroblasts that differentiate into myofibroblasts

_Specifies fibroblasts that deposit ECM under themselves and differentiate into a myofibroblast population that also secretes PDGF. Senescent fibroblasts secrete PDGF early and MMP late._

```xml
<population id="FIB" class="fibroblast" init="100">
    <population.process id="SECRETION" />
    <population.parameter id="DIFFERENTIATION_TARGET" value="MYO" />
    <population.parameter process="secretion" id="RATE_ECM" value="5" />
    <population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
    <population.parameter module="senescence" id="EARLY_RATE_PDGF" value="1000" />
    <population.parameter module="senescence" id="LATE_RATE_MMP" value="20000" />
</population>
<population id="MYO" class="myofibroblast" init="0">
    <population.process id="SECRETION" />
    <population.parameter process="secretion" id="RATE_ECM" value="30" />
    <population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
    <population.parameter process="secretion" id="RATE_PDGF" value="200" />
</population>
```

{% include_relative populations/region.md %}
{% include_relative populations/link.md %}
