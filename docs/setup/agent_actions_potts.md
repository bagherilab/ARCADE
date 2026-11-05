---
title: Potts agent actions
nav_order: 2
parent: Agent actions
---

# Potts agent actions
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `action` tag defines a single action.
Nested tags include `action.parameter` for parameters and `action.register` for registration to a population.

```xml
<actions>
    <action id="[ID]" class="[CLASS]">
        <action.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <action.register id="[ID]" />
        ...
    </action>
    ...
</actions>
```

{: .note }
An action with an unknown `class` is skipped without an error.

{% assign implementation = "potts" %}
{% include_relative actions/creation.md implementation=implementation %}
{% include_relative actions/parameter.md implementation=implementation %}
{% include_relative actions/register.md %}

## Insert action (`insert`)

Inserts new cell agents once, at tick `TIME_DELAY`.
For each registered population, it tries to insert `INSERT_NUMBER` cells.
Each new cell starts on one random empty interior voxel and grows toward its critical volume.

| PARAMETER       | DESCRIPTION                                        | DEFAULT |
| --------------- | -------------------------------------------------- | ------- |
| `TIME_DELAY`    | tick at which cells are inserted                   | 0       |
| `INSERT_NUMBER` | number of cells inserted per registered population | 1       |

- Inserted cells take their first step on the tick after insertion, and appear in saved output from tick `TIME_DELAY + 1`.
- New cells get the next unused ids.
- A cell is not inserted, and an info message is logged, if there are no empty voxels left or the population uses regions.

### Example: Inserting three macrophages at tick 10

_Population `MAC` uses the `macrophage` cell class. The action is registered to the population id, `MAC`._

```xml
<populations>
    <population id="MAC" class="macrophage" init="0" />
</populations>
<actions>
    <action id="RECRUIT" class="insert">
        <action.parameter id="TIME_DELAY" value="10" />
        <action.parameter id="INSERT_NUMBER" value="3" />
        <action.register id="MAC" />
    </action>
</actions>
```
