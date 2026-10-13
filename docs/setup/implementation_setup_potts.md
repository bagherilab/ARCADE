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
: Chemotaxis energy up or down the gradients of [lattice layers]({{site.baseurl}}/setup/lattice_layers_potts) (2D only)

To modify Hamiltonian term parameters, use the [`potts.parameter`](#implementation-parameters) tag with the corresponding `term` attribute.

### Example: Including volume and adhesion Hamiltonian terms

_Specifies a Hamiltonian with the volume and adhesion terms._

```xml
<potts.term id="volume" />
<potts.term id="adhesion" />
```

### Chemotaxis term

The chemotaxis term moves cells up (positive lambda) or down (negative lambda) the gradient of each layer.
When a cell gains or loses a voxel, the change in energy is -`LAMBDA_[LAYER]` × ∇c · Δr, where ∇c is the layer gradient at the cell centroid (per voxel) and Δr is the shift in the cell centroid (in voxels).
The centroid shift from a single voxel is about the distance of the voxel from the centroid divided by the cell volume, so the same lambda moves larger cells more slowly.

| PARAMETER | DEFAULT | UNITS | DESCRIPTION |
| --------- | ------- | ----- | ----------- |
| `chemotaxis/LAMBDA_[LAYER]` | 0 | energy/concentration | strength of chemotaxis up the layer gradient |

Parameters for a layer that is not in the setup cause an error.

### Example: Including chemotaxis toward a layer

_Specifies chemotaxis of population A up the PDGF gradient and population B down the PDGF gradient._

```xml
<potts.term id="chemotaxis" />
<potts.parameter term="chemotaxis" id="LAMBDA_PDGF" value="2000" target="A" />
<potts.parameter term="chemotaxis" id="LAMBDA_PDGF" value="-2000" target="B" />
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
