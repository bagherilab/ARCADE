---
title: 4D hybrid model interrogates agent-level rules and parameters driving hiPS cell colony dynamics
authors: JS Yu, B Lyons, SM Rafelski, JA Theriot, N Bagheri, GT Johnson
journal: bioRxiv
doi: 10.1101/2024.08.12.607546
---

Iterating between data-driven research and generative computational models is a powerful approach for emulating biological systems, testing hypotheses, and gaining a deeper understanding of these systems. We developed a hybrid agent-based model (ABM) that integrates a Cellular Potts Model (CPM) designed to investigate cell shape and colony dynamics in human induced pluripotent stem cell (hiPS cell) colonies. This model aimed to first mimic and then explore the dynamics observed in real-world hiPS cell cultures. Initial outputs showed great potential, seeming to mimic small colony behaviors relatively well. However, longer simulations and quantitative comparisons revealed limitations, particularly with the CPM component, which lacked long-range interactions that might be necessary for accurate simulations. This challenge led us to thoroughly examine the hybrid model's potential and limitations, providing insights and recommendations for systems where cell-wide mechanics play significant roles. The CPM supports 2D and 3D cell shapes using a Monte Carlo algorithm to prevent cell fragmentation. Basic "out of the box" CPM Hamiltonian terms of volume and adhesion were insufficient to match live cell imaging of hiPS cell cultures. Adding substrate adhesion resulted in flatter colonies, highlighting the need to consider environmental context in modeling. High-throughput parameter sweeps identified regimes that produced consistent simulated shapes and demonstrated the impact of specific model decisions on emergent dynamics. Full-scale simulations showed that while certain agent rules could form a hiPS cell monolayer in 3D, they could not maintain it over time. Our study underscores that "out of the box" 3D CPMs, which do not natively incorporate long-range cell mechanics like elasticity, may be insufficient for accurately simulating hiPS cell and colony dynamics. To address this limitation, future work could add mechanical constraints to the CPM Hamiltonian or integrate global agent rules. Alternatively, replacing the CPM with a methodology that directly represents cell mechanics might be necessary. Documenting and sharing our model development process fosters open team science and supports the broader research community in developing computational models of complex biological systems.
{: .fs-2 }

- **Model release**: [https://github.com/bagherilab/ARCADE/releases/tag/v3.1.4](https://github.com/bagherilab/ARCADE/releases/tag/v3.1.4)
- **Supporting code**: [https://github.com/allen-cell-animated/cell-abm-pipeline](https://github.com/allen-cell-animated/cell-abm-pipeline)
{: .fs-2 }