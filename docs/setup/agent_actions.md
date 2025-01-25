---
title: Agent actions
nav_order: 4
parent: Setup files
---

# Agent actions

Actions can be specified to interact with cells at different points of the
simulation.
{: .fs-6 .fw-300 }

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
        <populations> <!-- Setup for agent populations --> </populations>
```

```xml
        <actions> <!-- Setup for agent actions --> </actions>
```
{: .fw-700 }

```xml
        <layers> <!-- Setup for environment layers --> </layers>
        <components> <!-- Setup for environment components --> </components>
    </series>
</set>
```
