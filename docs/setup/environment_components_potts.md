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
The `potts` implementation defines the component classes `oxygen_source`, `vessels`, `vessel_growth`, `vessel_cells` and `release` (`release` and `vessel_cells` below).
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


## Vessel cells component

The `vessel_cells` class turns the plane section of a vessel network into space-occupying cells of a passive population (class `vessel`).
It needs a `vessels` component (`VESSELS`), the population named by `POPULATION`, and the `vessel` Hamiltonian term (`<potts.term id="VESSEL" />`).
It registers to no layer.

- **First step.** The network is cut into pieces of at most `L_PIECE` in-plane length; a piece below `N_MIN` voxels merges with its neighbour, or gets no cell if it is isolated.
  Each piece becomes one cell on the voxels of the piece, which are taken from the cells that own them (a cell left with no voxels is removed); a piece that is not connected starts on its largest connected part (at least `N_MIN` voxels), as later.
  The run fails if cells of `POPULATION` or `TIP_POPULATION` already exist (for example, in a loaded state).
- **Each cell** has the piece size as its target volume and is anchored to its piece through the `vessel` term with strength `LAMBDA_ANCHOR_MIN` + (`LAMBDA_ANCHOR_MAX` &minus; `LAMBDA_ANCHOR_MIN`) &times; I, where I is the mean integrity of its segments.
  The outer radius of a segment is `R_MATURE` + (`R_SPROUT` &minus; `R_MATURE`) &times; (1 &minus; I).
- **Each tick**, after the Potts step: if the network changed, the pieces are updated without recutting. A cell keeps its piece while its segments exist and their integrities are unchanged; a cell whose segments changed keeps the segments still there; segments with no cell are cut into new pieces among themselves.
  A new cell never takes voxels from another vessel or tip cell: it starts on the largest connected part of its piece not held by one (at least `N_MIN` voxels).
  A cell with no piece left gets target volume 0 and no anchor, and is removed once it has shrunk to one voxel or has not shrunk for 4 ticks.
- **Tips** (only if the `GROWTH` component exists; it is then coupled to the tip cells). A sprout tip whose proposed segment crosses the plane gets an aim instead of a segment, and a tip cell (`TIP_POPULATION`):
  it buds `N_TIP0` voxels from the nearest vessel cell within `D_BUD_FOLD` &times; `R_OUT` of the tip node, on the side facing the aim, or is seeded on the aim's footprint if there is none.
  The tip cell is anchored from the tip node to the aim's end at the anchor strength of a new sprout, drawn into the aim's footprint with strength `LAMBDA_GUIDE`, and its target area grows by `G_TIP` per hour since its last append (at most one hour's growth).
  When the centre of its leading end has moved the aim's reach forward along the aim in the plane (at most `V_TIP` per hour since the last append; a front behind the tip node, or more than the sprout's outer radius to the side of the aim, counts for nothing), a segment is appended along the aim by that much; the tip cell's trailing voxels become a vessel cell for that segment, and the tip cell carries on.
  Through Potts copies, a tip cell cannot take voxels from a vessel cell (the vessel term forbids it): tips migrate into tissue, not through the vessel wall.
  A tip that joins another vessel becomes a vessel cell; a tip that regresses, is capped or leaves the plane retires. Without the growth component there are no tip cells.
  Then cells whose centroid is farther than `D_DRIFT_FOLD` &times; `R_OUT` from all their segments (drift), and cells that are not connected (fragments), are counted; non-zero counts are logged.
  With the growth component, the in-plane tip updates (one per tip cell per tick), the stalled ones (no append for over 1 h) and the appends are counted and logged daily.

| Parameter | Units | Default | Description |
| --- | --- | --- | --- |
| `R_MATURE` | &micro;m | 5 | outer radius of a mature segment (I = 1) |
| `R_SPROUT` | &micro;m | 8 | outer radius of a sprout (I = 0) |
| `L_PIECE` | &micro;m | 30 | largest in-plane length of a piece (one cell) |
| `N_MIN` | voxels | 6 | smallest piece, and smallest vessel cell after voxels are taken from it |
| `LAMBDA_ANCHOR_MIN` | energy | 10 | anchor strength at I = 0 (placeholder) |
| `LAMBDA_ANCHOR_MAX` | energy | 50 | anchor strength at I = 1 (anchor screen) |
| `D_DRIFT_FOLD` | | 2 | drift distance as a multiple of `R_OUT` |
| `VESSELS` | | `VESSELS` | id of the vessel component |
| `POPULATION` | | `VESSEL` | name of the vessel cell population |
| `TIP_POPULATION` | | `VTIP` | name of the tip cell population (may be absent) |
| `GROWTH` | | `GROWTH` | id of the `vessel_growth` component (tips only if it exists) |
| `LAMBDA_GUIDE` | energy | 50 | guide strength drawing tip cells into their aim (placeholder) |
| `G_TIP` | &micro;m&sup2;/hour | 160 | growth of the tip cell target area |
| `N_TIP0` | voxels | 6 | voxels a tip cell buds with |
| `D_BUD_FOLD` | | 2 | bud distance as a multiple of `R_OUT` |

```xml
<component id="VESSEL_CELLS" class="vessel_cells" />
```
