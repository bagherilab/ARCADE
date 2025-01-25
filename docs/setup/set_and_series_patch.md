---
title: Patch simulations
nav_order: 1
parent: Simulation set and series
---

# Patch simulation set and series

A group of simulation series is called a `set`.
A setup file can only contain one `set`.
A simulation `series` is a group of simulations that only differ in the random seed.
Each `series` includes additional tags that define various parts of the series.
A `set` can include multiple `series` tags.

```xml
<set prefix="[PREFIX]">
    <series name="[NAME]" start="[START]" end="[END]"
            radius="[RADIUS]" depth="[DEPTH]" margin="[MARGIN]"
            ticks="[TICKS]" interval="[INTERVAL]">
        ...
    </series>
</set>
```

| ATTRIBUTE  | DESCRIPTION                                    | TYPE                     | DEFAULT |
| ---------- | ---------------------------------------------- | ------------------------ | ------- |
| `prefix`   | prefix for all series names                    | string                   | ""      |
| `name`     | name of the simulation series for output files | string                   |         |
| `start`    | starting random seed                           | integer                  | 0       |
| `end`      | ending random seed                             | integer                  | 0       |
| `radius`   | radius of the simulation environment           | integer                  | 1       |
| `depth`    | depth of the simulation environment            | integer                  | 1       |
| `margin`   | simulation environment margin                  | integer                  | 0       |
| `ticks`    | total number of simulation ticks               | integer                  | 100     |
| `interval` | number of ticks between snapshots              | integer                  | 50      |

The `radius` and `depth` attributes are converted to `length`, `width`, and `height` based on the simulation geometry.
Values for `radius` and `margin` must be even and `depth` must be odd; if not, given values will be rounded to the nearest even or odd value.

Spatial scaling `ds` and `dz` are automatically set based on simulation geometry.
Temporal scaling `dt` is assumed to be 1 min/tick.

{% include_relative set_and_series_examples.md %}
