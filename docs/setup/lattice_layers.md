---
title: Lattice layers
nav_order: 5
parent: Setup files
---

# Lattice layers

All environmental quantities are defined in layers.
Layers are shared between all cell agents.
{: .fs-6 .fw-300 }

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
        <populations> <!-- Setup for agent populations --> </populations>
        <actions> <!-- Setup for agent actions --> </actions>
```

```xml
        <layers> <!-- Setup for environment layers --> </layers>
```
{: .fw-700 }

```xml
        <components> <!-- Setup for environment components --> </components>
    </series>
</set>
```
