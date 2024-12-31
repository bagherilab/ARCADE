---
title: Implementation setup
nav_order: 2
parent: Setup files
---

# Implementation setup

Each implementation uses a dedicated tag to define implementation-specific setup.
{: .fs-6 .fw-300 }

ARCADE currently supports two implementations: `patch` and `potts`.
Only one implementation tag should be used in the setup file; additional implementation tags will be ignored.

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
```

```xml
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
```
{: .fw-700 }

```xml
        <populations> <!-- Setup for agent populations --> </populations>
        <actions> <!-- Setup for agent actions --> </actions>
        <layers> <!-- Setup for environment layers --> </layers>
        <components> <!-- Setup for environment components --> </components>
    </series>
</set>
```
