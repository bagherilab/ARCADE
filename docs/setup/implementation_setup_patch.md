---
title: Patch implementation
nav_order: 1
parent: Implementation setup
---

# Patch implementation setup
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `patch` tag describes patch framework-specific setup.
Nested tags include `patch.parameter` for parameters.

```xml
<patch>
    <patch.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
    ...
</patch>
```

## Implementation parameters

The `patch.parameter` tag defines patch implementation parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.patch.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/patch/parameter.patch.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | Parameter name                            |
| `value`   | New parameter value                       |
| `scale`   | Scaling factor applied to parameter value |

### Example: Changing model geometry

_Specifies a hexagonal model geometry._

```xml
<patch.parameter id="GEOMETRY" value="hex" />
```

### Example: Changing model initialization strategy

_Specifies the random initialization strategy._

```xml
<patch.parameter id="INITIALIZATION" value="random" />
```
