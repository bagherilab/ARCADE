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

## Potts implementation: Hamiltonian terms

The `potts.term` tag is used to specify the terms used in the CPM Hamiltonian.
Valid options include:

`volume`
: Target area (2D) or volume (3D) constraint

`adhesion`
: Adhesion contact energy (2D and 3D)

`substrate`
: Differential substrate adhesion contact energy (3D only)

### Example: Including volume and adhesion Hamiltonian terms

```xml
<potts.term id="volume" />
<potts.term id="adhesion" />
```

## Potts implementation: Parameters

The `potts.parameter` tag defines CPM implementation parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.potts.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/potts/parameter.potts.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`
- Parameter changes for terms can be applied to specific populations using the `target` attribute

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | Parameter name                            |
| `value`   | New parameter value                       |
| `scale`   | Scaling factor applied to parameter value |
| `term`    | Hamiltonian term the parameter applies to |
| `target`  | Target populations ids for the parameter  |

### Example: Modifying global potts parameters

```xml
<potts.parameter id="MCS" value="[VALUE]"  />
```

### Example: Modifying term-specific potts parameters

```xml
<potts.parameter term="volume" id="LAMBDA" value="[SCALE]" />
```

### Example: Modifying term-specific potts parameters with target

```xml
<!-- Modifies LAMBDA parameter in volume term for population A -->
<potts.parameter term="volume" id="LAMBDA" scale="[SCALE]" target="A" />
<!-- Modifies ADHESION parameter in adhesion term between population A and population B -->
<potts.parameter term="adhesion" id="ADHESION" value="[VALUE]" target="A:B" />
<!-- Modifies ADHESION parameter in adhesion term between population A and media -->
<potts.parameter term="adhesion" id="ADHESION" value="[VALUE]" target="A:*" />
```
