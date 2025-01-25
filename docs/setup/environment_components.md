---
title: Environment components
nav_order: 6
parent: Setup files
---

# Environment components

Components can be specified to interact with environment layers at different points of the
simulation.
{: .fs-6 .fw-300 }

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
        <populations> <!-- Setup for agent populations --> </populations>
        <actions> <!-- Setup for agent actions --> </actions>
        <layers> <!-- Setup for environment layers --> </layers>
```

```xml
        <components> <!-- Setup for environment components --> </components>
```
{: .fw-700 }

```xml
    </series>
</set>
```
