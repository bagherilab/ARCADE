---
title: Potts implementation
nav_order: 2
parent: Implementation setup
---

# Potts implementation setup
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `potts` tag describes Cellular Potts Model (CPM) framework-specific setup.
Nested tags include `potts.term` for Hamiltonian terms and `potts.parameter` for parameters.

```xml
<potts>
    <potts.term id="[ID]" />
    <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
    <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" term="[TERM]" />
    <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" term="[TERM]" target="[TARGET]" />
    ...
</potts>
```

## Hamiltonian terms

The `potts.term` tag is used to specify the terms used in the CPM Hamiltonian.
Valid options include:

`volume`
: Target area (2D) or volume (3D) constraint

`surface`
: Target perimeter (2D) or surface area (3D) constraint

`height`
: Target height constraint (3D only)

`adhesion`
: Adhesion contact energy (2D and 3D)

`substrate`
: Differential substrate adhesion contact energy (3D only)

`persistence`
: Persistence energy (2D and 3D)

`chemotaxis`
: Chemotaxis energy up or down the gradients of [lattice layers]({{site.baseurl}}/setup/lattice_layers_potts) (2D and 3D)

`matrix`
: Resistance to movement through the ECM layer (2D and 3D)

To modify Hamiltonian term parameters, use the [`potts.parameter`](#implementation-parameters) tag with the corresponding `term` attribute.

### Example: Including volume and adhesion Hamiltonian terms

_Specifies a Hamiltonian with the volume and adhesion terms._

```xml
<potts.term id="volume" />
<potts.term id="adhesion" />
```

### Chemotaxis term

The chemotaxis term moves cells up (positive lambda) or down (negative lambda) the gradient of each layer.
When a cell gains a voxel, the change in energy is -`LAMBDA_[LAYER]` × (c - c'), where c is the layer value at the voxel and c' is the mean layer value over the voxels of the cell adjacent to it.
When a cell loses a voxel, the change in energy is -`LAMBDA_[LAYER]` × (c' - c).

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `chemotaxis/LAMBDA_[LAYER]` | 0 | energy/concentration | strength of chemotaxis up the layer gradient |

Parameters for a layer that is not in the setup cause an error.

### Example: Including chemotaxis toward a layer

_Specifies chemotaxis of population A up the GF gradient and population B down the GF gradient._

```xml
<potts.term id="chemotaxis" />
<potts.parameter term="chemotaxis" id="LAMBDA_GF" value="20" target="A" />
<potts.parameter term="chemotaxis" id="LAMBDA_GF" value="-20" target="B" />
```

### Matrix term

The matrix term slows cells where the `ECM` layer is dense, without making ECM take up space.
Any change of a voxel costs `LAMBDA` × ECM for each cell involved (the cell losing and the cell gaining the voxel), where ECM is the layer value at the voxel.
The cost is the same for gaining and losing, so it does not push cells in any direction; fewer changes succeed in dense ECM, so cells move more slowly there.
Medium pays nothing, and the term is zero if there is no `ECM` layer.
If `WOUND_MATRIX_WEIGHT` w is above 0, the provisional matrix (`WOUND_MATRIX`, the fibrin clot) also resists, and the cost is `LAMBDA` × (ECM + w × WOUND_MATRIX).
Cells must break the clot down to advance, so invasion slows where it is dense and speeds up as MMP clears it.
A missing layer contributes 0.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `matrix/LAMBDA` | 0 | energy/concentration | cost per unit ECM of changing a voxel, paid by each cell involved |
| `matrix/WOUND_MATRIX_WEIGHT` | 0 | unitless | weight of `WOUND_MATRIX` relative to ECM; 0 for ECM only |

### Example: Slowing fibroblasts more than macrophages in ECM

```xml
<potts.term id="matrix" />
<potts.parameter term="matrix" id="LAMBDA" value="20" target="FIB" />
<potts.parameter term="matrix" id="LAMBDA" value="5" target="MAC" />
```

## Implementation parameters

The `potts.parameter` tag defines CPM implementation parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.potts.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/potts/parameter.potts.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`
- Parameter changes for terms can be applied to specific populations using the `target` attribute

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | parameter name                            |
| `value`   | new parameter value                       |
| `scale`   | scaling factor applied to parameter value |
| `term`    | Hamiltonian term the parameter applies to |
| `target`  | target populations ids for the parameter  |

### Example: Modifying global potts parameters

_The MCS parameter is set to the new value of 3._

```xml
<potts.parameter id="MCS" value="3" />
```

### Example: Modifying term-specific potts parameters

_The lambda parameter in the volume term is set to the new value of 20._

```xml
<potts.parameter term="volume" id="LAMBDA" value="20" />
```

### Example: Modifying term-specific potts parameters with target

_The lambda parameter in the volume term is scaled by 2 for population A._

```xml
<potts.parameter term="volume" id="LAMBDA" scale="2" target="A" />
```

_The adhesion between population A and population B is set to the new value of 20._

```xml
<potts.parameter term="adhesion" id="ADHESION" value="20" target="A:B" />
```

_The adhesion between population A and the media (*) is set to the new value of 30._

```xml
<potts.parameter term="adhesion" id="ADHESION" value="30" target="A:*" />
```
