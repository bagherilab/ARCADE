---
title: Setup files
nav_order: 1
---

# Setup files

ARCADE uses a setup file that describes the setup of the simulations, agents, and environment.
{: .fs-6 .fw-300 }

The basic structure of the file is:

```xml
<set> <!-- Simulation set -->
    <series> <!-- Simulation series -->
        <patch> <!-- Patch implementation setup --> </patch>
        <potts> <!-- Potts implementation setup --> </potts>
        <populations> <!-- Setup for agent populations --> </populations>
        <actions> <!-- Setup for agent actions --> </actions>
        <layers> <!-- Setup for environment layers --> </layers>
        <components> <!-- Setup for environment components --> </components>
    </series>
</set>
```

Note that the `<patch>` and `<potts>` tags are only used when running the corresponding implementation.
Not all tags may be valid or available in all implementations; see specific docs for each tag for details.
