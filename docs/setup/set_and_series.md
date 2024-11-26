---
title: Simulation set and series
nav_order: 2
parent: Setup files
---

# Simulation set and series
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Simulation sets

A group of simulation series is called a **set**.
The setup XML can only include one set.

| ATTRIBUTE | DESCRIPTION                 | TYPE   | DEFAULT |
| --------- | --------------------------- | ------ | ------- |
| `prefix`  | prefix for all series names | string | ""      |

## Simulation series

A simulation **series** is a group of simulations that only differ in the random seed.
Each `<series>` includes additional tags that define various parts of the series.
A set can include multiple `<series>` tags.

| ATTRIBUTE  | DESCRIPTION                                    | TYPE                     | DEFAULT |
| ---------- | ---------------------------------------------- | ------------------------ | ------- |
| `name`     | name of the simulation series for output files | string                   |         |
| `start`    | starting random seed                           | integer                  | 0       |
| `end`      | ending random seed                             | integer                  | 0       |
| `ticks`    | total number of simulation ticks               | integer                  | 100     |
| `interval` | number of ticks between snapshots              | integer                  | 50      |

### `patch` implementation only

| ATTRIBUTE  | DESCRIPTION                                    | TYPE                     | DEFAULT |
| ---------- | ---------------------------------------------- | ------------------------ | ------- |
| `radius`   | radius of the simulation environment           | integer                  | 100     |
| `depth`    | depth of the simulation environment            | integer                  | 100     |
| `margin`   | simulation environment margin                  | integer                  | 0       |

The `radius` and `depth` attributes are converted to `length`, `width`, and `height` based on the simulation geometry.
Values for `radius` and `margin` must be even and `depth` must be odd; if not, given values will be rounded to the nearest even or odd value.

Spatial scaling `ds` and `dz` are automatically set based on simulation geometry.
Temporal scaling `dt` is assumed to be 1 min/tick.

### `potts` implementation only

| ATTRIBUTE  | DESCRIPTION                                    | TYPE                     | DEFAULT |
| ---------- | ---------------------------------------------- | ------------------------ | ------- |
| `length`   | x size of the simulation environment           | integer                  | 100     |
| `width`    | y size of the simulation environment           | integer                  | 100     |
| `height`   | z size of the simulation environment           | integer                  | 1       |
| `margin`   | initialization boundary margin                 | integer                  | 0       |
| `ds`       | spatial conversion factor (um/voxel)           | integer, float, fraction | 1       |
| `dt`       | temporal conversion factor (hrs/tick)          | integer, float, fraction | 1       |

If `height = 1`, the simulation uses 2D rules.
If `height > 1`, the simulation uses 3D rules.

Note that the environment grid assumes a one-voxel border in each direction (except in the z direction in a 2D simulation).
For 2D simulations, `length` and `width` must be at least 3 for valid simulations.
For 3D simulations, `length`, `width`, and `height` must be at least 3 for valid simulations.

The initialization `margin` defines the number of voxels in the x and y directions that are not used when initializing cells.
