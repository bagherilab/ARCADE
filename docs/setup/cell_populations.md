---
title: Cell populations
nav_order: 3
parent: Setup files
---

# Cell populations

All cell agents are defined in populations.
Cells in the same population share the same cell class and parameters.
{: .fs-6 .fw-300 }

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
```

```xml
        <populations> <!-- Setup for agent populations --> </populations>
```
{: .fw-700 }

```xml
        <actions> <!-- Setup for agent actions --> </actions>
        <layers> <!-- Setup for environment layers --> </layers>
        <components> <!-- Setup for environment components --> </components>
    </series>
</set>
```
