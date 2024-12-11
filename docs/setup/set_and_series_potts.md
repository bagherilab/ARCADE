---
title: Potts simulations
nav_order: 2
parent: Simulation set and series
---

# Potts simulation set and series

A group of simulation series is called a `set`.
A setup file can only contain one `set`.
A simulation `series` is a group of simulations that only differ in the random seed.
Each `series` includes additional tags that define various parts of the series.
A `set` can include multiple `series` tags.

```xml
<set prefix="[PREFIX]">
    <series name="[NAME]" start="[START]" end="[END]"
            ds="[DS]" margin="[MARGIN]" length="[LENGTH]" width="[WIDTH]" height="[HEIGHT]"
            dt="[DT]" ticks="[TICKS]" interval="[INTERVAL]">
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
| `ds`       | spatial conversion factor (um/voxel)           | integer, float, fraction | 1       |
| `margin`   | initialization boundary margin                 | integer                  | 0       |
| `length`   | x size of the simulation environment           | integer                  | 100     |
| `width`    | y size of the simulation environment           | integer                  | 100     |
| `height`   | z size of the simulation environment           | integer                  | 1       |
| `dt`       | temporal conversion factor (hrs/tick)          | integer, float, fraction | 1       |
| `ticks`    | total number of simulation ticks               | integer                  | 100     |
| `interval` | number of ticks between snapshots              | integer                  | 50      |

If `height = 1`, the simulation uses 2D rules.
If `height > 1`, the simulation uses 3D rules.

Note that the environment grid assumes a one-voxel border in each direction (except in the z direction in a 2D simulation).
For 2D simulations, `length` and `width` must be at least 3 for valid simulations.
For 3D simulations, `length`, `width`, and `height` must be at least 3 for valid simulations.

The initialization `margin` defines the number of voxels in the x and y directions that are not used when initializing cells.

{% include_relative set_and_series_examples.md %}
