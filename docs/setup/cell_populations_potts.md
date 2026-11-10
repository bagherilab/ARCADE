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

_Specifies a population that secretes GF at 1000 per hour into the voxels around each cell, and ECM at 50 per hour into the voxels of each cell._

```xml
<population.process id="SECRETION" />
<population.parameter process="secretion" id="RATE_GF" value="1000" />
<population.parameter process="secretion" id="RATE_ECM" value="50" />
<population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
```

### Sensing process

The `SENSING` process adds molecules to the environment based on the conditions the cell senses.
The `HYPOXIC` version secretes VEGF when the cell is hypoxic.
Each tick, a cell adds `VEGF_SECRETION_RATE` × dt × h(O2) to the `VEGF` layer, where O2 is the mean of the `O2` layer over the cell and h(O2) = K<sup>n</sup> / (K<sup>n</sup> + O2<sup>n</sup>) with K = `O2_HYPOXIA` and n = `HYPOXIA_HILL`.
The amount is spread evenly over the voxels of the cell, and is added to the layer at the next lattice step.
A positive rate requires both the `O2` and `VEGF` layers in the setup.
Secretion that does not depend on O2 (for example, basal or SASP secretion of VEGF) uses the secretion process.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `sensing/VEGF_SECRETION_RATE` | 0 | amount/hour | maximum rate of VEGF secretion |
| `sensing/O2_HYPOXIA` | 12 | mmHg | O2 at which secretion is half maximal |
| `sensing/HYPOXIA_HILL` | 4 | | Hill exponent (steepness) of the switch |

### Deposition process

The `DEPOSITION` process deposits matrix into the layer named by its version.
The `ECM` version deposits into the `ECM` layer when the cell is activated by growth factor (the `GF` layer; PDGF in Chandrasegaran et al. 2025).
Each tick, a quiescent or proliferative cell whose mean `GF` is above `GF_THRESHOLD` adds `RATE` × dt × f(O2) to the `ECM` layer, where f(O2) = O2 / (`K_O2` + O2) is the O2 limit on collagen maturation, using the mean of the `O2` layer over the cell.
If `K_O2` is 0, deposition is not limited by O2 and no `O2` layer is needed.
The amount is spread evenly over the voxels of the cell, and is added to the layer at the next lattice step.
Senescent, apoptotic, necrotic, and autotic cells do not deposit through this process; ECM in the SASP uses the senescence secretion rates.
The `GF` and `ECM` layers are required when `RATE` is above 0.
Do not also set `secretion/RATE_ECM` for the same population unless both are intended, since both deposit.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `deposition/RATE` | 0 | amount/hour | rate of ECM deposition |
| `deposition/GF_THRESHOLD` | 0 | concentration | threshold on mean GF for deposition |
| `deposition/K_O2` | 0 | mmHg | O2 at which deposition is half maximal; 0 disables the O2 limit |

### Example: Depositing ECM when activated

_Specifies a population that deposits 50 ECM per hour when GF is above 0.1, half maximal at 25 mmHg O2._

```xml
<population.process id="DEPOSITION" version="ECM" />
<population.parameter process="deposition" id="RATE" value="50" />
<population.parameter process="deposition" id="GF_THRESHOLD" value="0.1" />
<population.parameter process="deposition" id="K_O2" value="25" />
```

### Example: Secreting VEGF when hypoxic

_Specifies a population that secretes up to 100 VEGF per hour, half maximal at 12 mmHg O2._

```xml
<population.process id="SENSING" version="HYPOXIC" />
<population.parameter process="sensing" id="VEGF_SECRETION_RATE" value="100" />
```

## Wound healing cells

The `fibroblast`, `myofibroblast`, `macrophage`, and `tissue` classes model wound healing.
Each tick, a wound healing cell applies the rules for its class (quiescent and proliferative cells only), steps its processes, and then steps the module for its state.
The rules read the mean `GF` (growth factor), `ECM`, `INF`, `INJURY_SIGNAL`, and `WOUND_MATRIX` layer values over the cell; `GF` stands for the pro-repair growth factors (PDGF, TGF-β) lumped into one field; a layer that is not in the setup reads as 0.

`INJURY_SIGNAL` stands for the signals from injured tissue (the wound and its cut edge), set as an initial pattern and cleared by decay. Its decay is clearance of the signal, not repair of the tissue; tissue recovery is read from ECM, cells and closure. `WOUND_MATRIX` is the provisional matrix (fibrin clot and fibronectin) that fills a wound before collagen (`ECM`) replaces it; it can be removed by MMP through a `DEGRADER` operation.

| CLASS | RULES |
| ----- | ----- |
| `fibroblast` | Proliferates when GF is above `GF_PROLIFERATION`. Differentiates into the `DIFFERENTIATION_TARGET` population when GF and ECM are above their thresholds (and `WOUND_MATRIX` above `WOUND_MATRIX_DIFFERENTIATION`, if set). Becomes senescent spontaneously or through the bystander routes. |
| `myofibroblast` | Proliferates under the same conditions as fibroblasts. Becomes apoptotic when INF is above `INF_APOPTOSIS`. Matures after a mean of `MATURATION`; mature cells below `ECM_TENSION` become apoptotic with probability `TENSION_APOPTOSIS` (0 = at once) and cannot become senescent; mature cells at or above it become senescent with probability `SENESCENCE_RATE` once the time (including the series `offset`) reaches `SENESCENCE_START`. Becomes senescent through the bystander routes. |
| `macrophage` | Does not proliferate. Senescent cells that touch a macrophage may be cleared. If `INF_POLARIZATION` is above 0, switches each tick between M1 (INF above the threshold) and M2 secretion rates. With `INJURY_ACTIVATION` above 0, a macrophage that is not M1 is M2 only while `INJURY_SIGNAL` is above it, and otherwise rests and secretes nothing. |
| `tissue` | Proliferates into free space next to the cell when the mean ECM over that space is at least `ECM_PROLIFERATION`. Becomes senescent spontaneously or through the bystander routes. |

Wound healing cells are quiescent, proliferative, senescent, or apoptotic; any other state (for example, in a loaded cells file) causes an error.
A quiescent cell becomes apoptotic with probability `BASAL_DEATH` each tick, before the class rules apply. Proliferative cells die through `proliferation/BASAL_APOPTOSIS_RATE`, and senescent cells only through clearance.
A quiescent cell becomes proliferative when it can proliferate.
A proliferative cell returns to quiescent if it can no longer proliferate during G1; after G1, it is committed to dividing.
Fibroblasts and myofibroblasts need GF above `GF_PROLIFERATION` to start proliferating, and at least half of it to continue.
All classes except macrophages are blocked from proliferating when the free fraction of voxels around the cell is below `CONTACT_FREE_FRACTION`.
An empty voxel counts as free only if its matrix is in range: ECM below `CONTACT_ECM_MAX` (dense collagen leaves no room to grow) and ECM + `WOUND_MATRIX` at least `CONTACT_MATRIX_MIN` (the cell needs matrix to attach to). Each limit is off at 0. Missing layers read 0.
If `O2_PROLIF_MIN` is above 0, they are also blocked when the mean of the `O2` layer over the cell is below it; the `O2` layer is then required.

Differentiation replaces the fibroblast with a cell of the target population that has the same id, age, divisions, state, and location.
The target must be a `myofibroblast` population, or the simulation fails at setup.
Population links are used for daughter cells on division, not for differentiation.
A target set for a population that is not a `fibroblast` population is ignored with a warning.
Wound healing populations do not support regions; a `population.region` tag causes an error.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `CONTACT_FREE_FRACTION` | 0 | | minimum free fraction of voxels around the cell to proliferate; 0 disables contact inhibition |
| `CONTACT_ECM_MAX` | 0 | concentration | ECM at or above which an empty voxel is not free; 0 for no limit |
| `CONTACT_MATRIX_MIN` | 0 | concentration | ECM + WOUND_MATRIX below which an empty voxel is not free; 0 for no limit; must be below `CONTACT_ECM_MAX` when both are set |
| `SPONTANEOUS_SENESCENCE` | 0 | 1/hour | probability of spontaneous senescence |
| `BYSTANDER_CONTACT` | 0 | 1/hour | probability of senescence on contact with an early-phase senescent cell (late-phase cells induce only through the paracrine route) |
| `BYSTANDER_PARACRINE` | 0 | 1/hour | probability of senescence when INF is above `INF_BYSTANDER` |
| `INF_BYSTANDER` | 1000 | concentration | threshold on INF for paracrine senescence |
| `O2_PROLIF_MIN` | 0 | mmHg | minimum mean O2 over the cell to proliferate; 0 disables the O2 gate |
| `BASAL_DEATH` | 0 | 1/hour | probability that a quiescent cell becomes apoptotic (fibroblast and macrophage turnover) |
| `INF_POLARIZATION` | 0 | concentration | macrophages only: threshold on mean INF for the M1 phenotype; 0 disables the switch |
| `INJURY_ACTIVATION` | 0 | concentration | threshold on mean `INJURY_SIGNAL` for the M2 phenotype; below it a macrophage that is not M1 rests and secretes nothing; 0 means no gate; needs `INF_POLARIZATION` above 0 |
| `M1_RATE_[LAYER]` | 0 | amount/hour | macrophages only: secretion rate into the layer in the M1 phenotype |
| `M2_RATE_[LAYER]` | 0 | amount/hour | macrophages only: secretion rate into the layer in the M2 phenotype |
| `GF_PROLIFERATION` | 0.1 | concentration | threshold on GF for proliferation |
| `GF_DIFFERENTIATION` | 0.1 | concentration | threshold on GF for differentiation |
| `ECM_DIFFERENTIATION` | 0.5 | concentration | threshold on ECM for differentiation |
| `WOUND_MATRIX_DIFFERENTIATION` | 0 | concentration | threshold on mean `WOUND_MATRIX` for differentiation, so myofibroblasts form only where there is wound matrix; 0 means no gate |
| `DIFFERENTIATION_RATE` | 0.2 | 1/hour | probability of differentiation above thresholds |
| `DIFFERENTIATION_TARGET` | `NONE` | | population id fibroblasts differentiate into |
| `INF_APOPTOSIS` | 5 | concentration | threshold on INF for myofibroblast apoptosis |
| `MATURATION` | 72 | hours | mean time to myofibroblast maturation |
| `ECM_TENSION` | 0.2 | concentration | threshold on ECM below which mature myofibroblasts become apoptotic |
| `TENSION_APOPTOSIS` | 0 | 1/hour | probability of apoptosis for mature myofibroblasts below `ECM_TENSION`; 0 means at once (no random draw) |
| `SENESCENCE_RATE` | 0.02 | 1/hour | probability of senescence for mature myofibroblasts |
| `SENESCENCE_START` | 0 | hours | time (including the series `offset`) from which mature myofibroblasts can become senescent; 0 means no gate. Only this primary route is gated; maturation, tension apoptosis and the bystander routes are not |
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

### Macrophage phenotype

If `INF_POLARIZATION` is above 0, a macrophage switches phenotype each tick, following Chandrasegaran et al. (2025).
When the mean INF over the cell is above `INF_POLARIZATION`, it is M1 and its secretion rate into each layer is set to `M1_RATE_[LAYER]`; otherwise it is M2 and the rates are set to `M2_RATE_[LAYER]`.
The switch has no memory, and a layer missing from the setup reads as 0 INF (M2).
The switch needs the `SECRETION` process, or the simulation fails when the cell is created.
If `INF_POLARIZATION` is 0, the macrophage uses its `secretion/RATE_[LAYER]` rates.
The phenotype is not saved; it can be recomputed from the saved INF layer at the macrophage locations.

### Example: Macrophages that switch between MMP and GF

_Specifies macrophages that secrete MMP at 100 per hour when mean INF is above 1 (M1), and GF at 100 per hour otherwise (M2)._

```xml
<population id="MAC" class="macrophage" init="20">
    <population.process id="SECRETION" />
    <population.parameter id="INF_POLARIZATION" value="1" />
    <population.parameter id="M1_RATE_MMP" value="100" />
    <population.parameter id="M2_RATE_GF" value="100" />
</population>
```

### Example: Fibroblasts that differentiate into myofibroblasts

_Specifies fibroblasts that deposit ECM under themselves and differentiate into a myofibroblast population that also secretes GF. Senescent fibroblasts secrete GF early and MMP late._

```xml
<population id="FIB" class="fibroblast" init="100">
    <population.process id="SECRETION" />
    <population.parameter id="DIFFERENTIATION_TARGET" value="MYO" />
    <population.parameter process="secretion" id="RATE_ECM" value="5" />
    <population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
    <population.parameter module="senescence" id="EARLY_RATE_GF" value="1000" />
    <population.parameter module="senescence" id="LATE_RATE_MMP" value="20000" />
</population>
<population id="MYO" class="myofibroblast" init="0">
    <population.process id="SECRETION" />
    <population.parameter process="secretion" id="RATE_ECM" value="30" />
    <population.parameter process="secretion" id="REGION_ECM" value="INSIDE" />
    <population.parameter process="secretion" id="RATE_GF" value="200" />
</population>
```

{% include_relative populations/region.md %}
{% include_relative populations/link.md %}
