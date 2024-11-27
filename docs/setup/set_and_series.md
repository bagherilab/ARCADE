---
title: Simulation set and series
nav_order: 0
parent: Setup files
---

# Simulation set and series

Each setup file corresponds to a single simulation set.
Each simulation set can have one or more simulation series, which differ only in random seed.
{: .fs-6 .fw-300 }

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
```
{: .fw-700 }

```xml
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
        <populations> <!-- Setup for agent populations --> </populations>
        <actions> <!-- Setup for agent actions --> </actions>
        <layers> <!-- Setup for environment layers --> </layers>
        <components> <!-- Setup for environment components --> </components>
```

```xml
    </series>
</set>
```
{: .fw-700 }
