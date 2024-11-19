---
title: Overview
nav_order: 0
---

# ARCADE: Agent-based Representation of Cells And Dynamic Environments

ARCADE is a flexible, extensible, interface-based Java framework for agent-based modeling of biological systems.
{: .fs-6 .fw-300 }

[Get the latest release on GitHub](https://github.com/bagherilab/ARCADE/releases/latest){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 }

ARCADE is a lightweight agent-based modeling framework that uses the [MASON](https://cs.gmu.edu/~eclab/projects/mason/) library for multi-agent scheduling and simulation.

Existing implementations of the core interfaces can be used to run simulations via highly configurable setup files. ARCADE currently has two major implementation:

- `patch` represents one or more agents in each environmental patch location
- `potts` represents agents as a collection of connected voxels (Cellular Potts modeling)

New implementations of these interfaces can be easily integrated with existing functionality to develop models for different biological systems or questions of interest.

{: .warning }
The ARCADE v3 patch implementation is actively under development and not yet at parity with ARCADE v2. You can track progress using the [migration: v2 to v3](https://github.com/bagherilab/ARCADE/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22migration%3A%20v2%20to%20v3%22) tag.

{% capture latestRelease %}
{% include_relative _changelog/{{ site.github.latest_release.tag_name }}.md %}
{% endcapture %}

{{ latestRelease | split: "---" | last | strip | replace: "# ", "## " | replace_first: "## ", "## What's new in " }}

## Getting started

Coming soon!

## About the project

ARCADE v2 is © 2020-2023 Bagheri Lab at Northwestern University. ARCADE v3 is © 2022-{{ "now" | date: "%Y" }}, Bagheri Lab at the University of Washington Seattle and [Jessica S. Yu](https://github.com/jessicasyu) at the Allen Institute for Cell Science.

### License

ARCADE v2 is distributed under a [GPL 3 license](https://github.com/bagherilab/ARCADE/blob/b8adced65b0bdf90e1a989e91b3e7c65b5ae88a2/LICENSE). ARCADE v3 is distributed under a [BSD 3-Clause license](https://github.com/bagherilab/ARCADE/blob/main/LICENSE).

### Contributors

<ul class="list-style-none">
{% for contributor in site.github.contributors %}
  <li class="d-inline-block mr-1">
     <a href="{{ contributor.html_url }}"><img src="{{ contributor.avatar_url }}" width="50" height="50" alt="{{ contributor.login }}"></a>
  </li>
{% endfor %}
</ul>
