---
title: Incorporating temporal information during feature engineering bolsters emulation of spatio-temporal emergence
authors: JY Cain, JI Evarts, JS Yu, N Bagheri
journal: Bioinformatics
doi: 10.1093/bioinformatics/btae131
---

Emergent biological dynamics derive from the evolution of lower-level spatial and temporal processes. A long-standing challenge for scientists and engineers is identifying simple low-level rules that give rise to complex higher-level dynamics. High-resolution biological data acquisition enables this identification and has evolved at a rapid pace for both experimental and computational approaches. Simultaneously harnessing the resolution and managing the expense of emerging technologies—e.g. live cell imaging, scRNAseq, agent-based models—requires a deeper understanding of how spatial and temporal axes impact biological systems. Effective emulation is a promising solution to manage the expense of increasingly complex high-resolution computational models. In this research, we focus on the emulation of a tumor microenvironment agent-based model to examine the relationship between spatial and temporal environment features, and emergent tumor properties.
Despite significant feature engineering, we find limited predictive capacity of tumor properties from initial system representations. However, incorporating temporal information derived from intermediate simulation states dramatically improves the predictive performance of machine learning models. We train a deep-learning emulator on intermediate simulation states and observe promising enhancements over emulators trained solely on initial conditions. Our results underscore the importance of incorporating temporal information in the evaluation of spatio-temporal emergent behavior. Nevertheless, the emulators exhibit inconsistent performance, suggesting that the underlying model characterizes unique cell populations dynamics that are not easily replaced.
{: .fs-2 }

- **Model release**: [https://github.com/bagherilab/ARCADE/releases/tag/v2.4](https://github.com/bagherilab/ARCADE/releases/tag/v2.4)
- **Supporting code**: [https://github.com/bagherilab/emulation](https://github.com/bagherilab/emulation)
{: .fs-2 }
