# ARCADE

**Agent-based model of heterogeneous cell populations in dynamic microenvironments**

[![Build status](https://bagherilab.github.io/ARCADE/badges/build.svg)](https://github.com/bagherilab/ARCADE/actions?query=workflow%3Abuild)
[![Lint status](https://bagherilab.github.io/ARCADE/badges/lint.svg)](https://github.com/bagherilab/ARCADE/actions?query=workflow%3Alint)
[![Documentation](https://bagherilab.github.io/ARCADE/badges/documentation.svg)](https://bagherilab.github.io/ARCADE/)
[![Coverage](https://bagherilab.github.io/ARCADE/badges/coverage.svg)](https://bagherilab.github.io/ARCADE/_coverage/)
![Version](https://bagherilab.github.io/ARCADE/badges/version.svg)
[![License](https://bagherilab.github.io/ARCADE/badges/license.svg)](https://github.com/bagherilab/ARCADE/blob/develop/LICENSE)

ARCADE v2 is licensed under GPL 3. ARCADE v3+ is licensed under BSD 3-Clause.

---

- **[Code structure overview](#code-structure-overview)**
- **[Building from source](#building-from-source)**
- **[Running the code](#running-the-code)**
- **[Setup file structure](#setup-file-structure)**
- **[Snapshot file structure](#snapshot-file-structure)**

---

## Code structure overview

The `src` directory contains all source files for the model.
The `test` directory contains all files for testing.
The `docs` directory contains documentation.

`arcade-X.X.X.jar` is a compiled jar of the model, with the required library [MASON](https://cs.gmu.edu/~eclab/projects/mason/) included.
Compiled jars for each release are included as a release asset.

## Building from source

The model can be built from source using [Gradle](https://gradle.org/).

To build, use:

```bash
$ ./gradlew build
```

The build command will also output a command in the form:

```bash
$ docker build --build-arg VERSION=X.X.X -t arcade:X.X.X .
```

which can be used to build a Docker image from the compiled jar.

Documentation can be regenerated using:

```bash
$ ./gradlew javadoc
```

## Running the code

The model can be run directly through command line, with or without real time visualization.
Note that running the model with visualization is significantly slower than without visualization.

When using command line, first navigate to the directory containing the jar.
Then:

```bash
$ java -jar arcade-X.X.X.jar potts XML PATH [(-v|--vis)] [--loadpath] [(-c|--cells)] [(-l|--locations)]

    XML
        Absolute path to setup XML
    PATH
        Absolute path for snapshot JSON
    [(-v|--vis)]
        Flag for running with visualization
    [--loadpath]
        Path prefix for loading files
    [(-c|--cells)]
        Flag for loading .CELLS file
    [(-l|--locations)]
        Flag for loading .LOCATIONS file
```

<details>
<summary><strong>:small_orange_diamond: Example: Running model from command line </strong></summary>

```bash
$ java -jar arcade.jar potts /path/to/setup.xml /path/to/output/
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Running model from command line with interactive visualization</strong></summary>

```bash
$ java -jar arcade.jar potts /path/to/setup.xml /path/to/output/ --vis
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Running model from command line using loaded files</strong></summary>

```bash
$ java -jar arcade.jar potts /path/to/setup.xml /path/to/output/ --loadpath /path/to/load/files --cells --locations
```

</details>

## Setup file structure

The model uses a `.xml` file that describes the setup of the simulations, agents, and environment.
Values in `[ALL CAPS]` should be replaced with actual values.

The basic structure of the file is:

```xml
<set prefix="[PREFIX]">
    <series name="[NAME]" start="[START]" end="[END]"
            ds="[DS]" margin="[MARGIN]" height="[HEIGHT]" length="[LENGTH]" width="[WIDTH]"
            dt="[DT]" ticks="[TICKS]" interval="[INTERVAL]">
        <potts>
            <potts.term id="[ID]" />
            <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
            <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" term="[TERM]" />
            <potts.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" term="[TERM]" target="[TARGET]" />
            ...
        </potts>
        <agents>
            <populations>
                <population id="[ID]" init="[INIT]">
                    <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
                    <population.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" module="[MODULE]"  />
                    <population.region id="[ID]" />
                    ...
                </population>
                ...
            </populations>
        </agents>
    </series>
    ...
</set>
```

### :large_blue_diamond: Tag: set

A group of simulations series is called a **set**.
The setup XML can only include one set.

| ATTRIBUTE | DESCRIPTION                 | TYPE   | DEFAULT |
| --------- | --------------------------- | ------ | ------- |
| `prefix`  | prefix for all series names | string | ""      |

### :large_blue_diamond: Tag: series

A simulation **series** is a group of simulations that only differ in the random seed.
Each `<series>` includes `<potts>`, `<agents>`, and `<environment>` tags that define various parts of the series.
A set can include multiple `<series>` tags.

| ATTRIBUTE  | DESCRIPTION                                    | TYPE                     | DEFAULT |
| ---------- | ---------------------------------------------- | ------------------------ | ------- |
| `name`     | name of the simulation series for output files | string                   |         |
| `start`    | starting seed                                  | integer                  | 0       |
| `end`      | ending seed                                    | integer                  | 0       |
| `length`   | x size of the simulation                       | integer                  | 100     |
| `width`    | y size of the simulation                       | integer                  | 100     |
| `height`   | z size of the simulation                       | integer                  | 1       |
| `margin`   | simulation environment margin                  | integer                  | 0       |
| `ticks`    | total number of simulation ticks               | integer                  | 100     |
| `interval` | number of ticks between snapshots              | integer                  | 50      |
| `ds`       | spatial conversion factor                      | integer, float, fraction | 1       |
| `dt`       | temporal conversion factor                     | integer, float, fraction | 1       |

### :large_blue_diamond: Tag: potts

The **potts** tag describes Cellular Potts Model (CPM) framework-specific setup.
Nested tags include **potts.term** for Hamiltonian terms and **potts.parameter** for parameters.

#### :small_blue_diamond: Nested tag: potts.term

The **potts.term** tag is used to specify the terms used in the CPM Hamiltonian.
Valid options include:

- `volume` = target area (2D) or volume (3D) constraint
- `adhesion` = adhesion contact energy (2D and 3D)
- `substrate` = differential substrate adhesion contact energy (3D only)

<details>
<summary><strong>:small_orange_diamond: Example: Including volume and adhesion Hamiltonian terms</strong></summary>

```xml
<potts.term id="volume" />
<potts.term id="adhesion" />
```

</details>

#### :small_blue_diamond: Nested tag: potts.parameter

The **potts.parameter** tag lists CPM parameters that are either set to a new value or scaled from the default value. Unless modified, default values are used for all parameters (as listed in [parameter.potts.xml](src/arcade/potts/parameter.potts.xml)).
Either or both `value` and `scale` attributes can be applied, with `value` applied first.

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | Parameter name                            |
| `value`   | New parameter value                       |
| `scale`   | Scaling factor applied to parameter value |
| `term`    | Hamiltonian term the parameter applies to |
| `target`  | Target populations ids for the parameter  |

Parameter changes for terms can be applied to specific populations using the `target` attribute

<details>
<summary><strong>:small_orange_diamond: Example: Modifying global potts parameters</strong></summary>

```xml
<potts.parameter id="MCS" value="[VALUE]"  />
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Modifying term-specific potts parameters</strong></summary>

```xml
<potts.parameter term="volume" id="LAMBDA" value="[SCALE]" />
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Modifying term-specific potts parameters with target</strong></summary>

```xml
<!-- Modifies LAMBDA parameter in volume term for population A -->
<potts.parameter term="volume" id="LAMBDA" scale="[SCALE]" target="A" />
<!-- Modifies ADHESION parameter in adhesion term between population A and population B -->
<potts.parameter term="adhesion" id="ADHESION" value="[VALUE]" target="A:B" />
<!-- Modifies ADHESION parameter in adhesion term between population A and media -->
<potts.parameter term="adhesion" id="ADHESION" value="[VALUE]" target="A:*" />
```

</details>

### :large_blue_diamond: Tag: agents

The **agents** tag groups agent setup tags, including definitions of the various cell populations and actions.

### :large_blue_diamond: Tag: populations

The **Populations** tag lists the cell populations included in the simulation.
For each **population** tag, nested tags include **population.parameter** for parameters and **population.region** for regions.

| ATTRIBUTE | DESCRIPTION                            | TYPE    | DEFAULT | REQUIRED |
| --------- | -------------------------------------- | ------- | ------- | -------- |
| `id`      | unique name for the agent population   | string  |         | Y        |
| `init`    | number of initial agents in population | integer | 0       | N        |

<details>
<summary><strong>:small_orange_diamond: Example: Initializing one agent population</strong></summary>

```xml
<populations>
    <population id="A" init="10" />
</populations>
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Initializing two agent populations</strong></summary>

```xml
<populations>
    <population id="A" init="10" />
    <population id="B" init="5" />
</populations>
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Initializing single agent population with padding between cells</strong></summary>

```xml
<populations>
    <population id="A" init="10:3" />
</populations>
```

</details>

#### :small_blue_diamond: Nested tag: population.parameter

The **population.parameter** tag lists population-specific parameters that are either set to a new value or scaled from the default value.
Unless modified, default values are used for all parameters (as listed in [parameter.potts.xml](src/arcade/potts/parameter.potts.xml)).
Either or both `value` and `scale` attributes can be applied, with `value` applied first.

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | Parameter name                            |
| `value`   | New parameter value                       |
| `scale`   | Scaling factor applied to parameter value |
| `module`  | State module the parameter applies to     |

The `module` attribute is used to specify parameters specific to a module.

<details>
<summary><strong>:small_orange_diamond: Example: Modifying population parameters</strong></summary>

```xml
<population.parameter id="CRITICAL_VOLUME_MEAN" value="[VALUE]" />
```

</details>

<details>
<summary><strong>:small_orange_diamond: Example: Modifying population module parameters</strong></summary>

```xml
<population.parameter id="CELL_GROWTH_RATE" module="proliferation" value="[VALUE]" />
```

</details>

#### :small_blue_diamond: Nested tag: population.region

The **population.region** tag lists subcellular regions for the population.
Valid options include:

- `DEFAULT` = represents the entire cell
- `NUCLEUS` = represents the nuclear region

Unless specified, only the `DEFAULT` region is used.

<details>
<summary><strong>:small_orange_diamond: Example: Including cell and nuclear regions</strong></summary>

```xml
<population.region id="DEFAULT" />
<population.region id="NUCLEUS" />
```

</details>

## Snapshot file structure

The models saves simulation snapshots to `.json` files, which describe the simulation and agents.
Values in `[ALL CAPS]` are replaced with simulation values.
Each simulation series produces a single overview file, and a number `.CELLS`/`.LOCATIONS` snapshots determined by the total number of simulation ticks and the snapshot interval.

### Simulation series overview

The basic structure of the overview file is:

```python
{
  "version": "[VERSION NUMBER]",
  "conversions": {
    "DS": [DS],
    "DT": [DT]
  },
  "ticks": [TICKS],
  "size": {
    "length": [LENGTH],
    "width": [WIDTH],
    "height": [HEIGHT],
    "margin": [MARGIN]
  },
  "populations": {
    "[POPULATION_ID]": {
      "CODE": [CODE],
      "PADDING": [PADDING],
      "INIT": [INIT],
      "[PARAMETER]": [PARAMETER VALUE],
    ...
      "[MODULE]/[PARAMETER]": [PARAMETER VALUE],
    ...
    },
    ...
  },
  "potts": {
    "[PARAMETER]": [PARAMETER VALUE],
    ...
    "[TERM]/[PARAMETER]": [PARAMETER VALUE],
    ...
    "[TERM]/[PARAMETER]:[TARGET]": [PARAMETER VALUE],
    ...
  }
}
```

### Simulation `.CELLS` snapshot

The `.CELLS` snapshot file includes details about each cell agent.
This file can be used to as initial conditions by loading with `--cells`.

```python
[
  {
    "id": [ID],
    "parent": [PARENT ID],
    "pop": [POPULATION CODE],
    "age": [AGE],
    "divisions": [DIVISION],
    "state": [STATE],
    "phase": [PHASE],
    "voxels": [NUMBER OF VOXELS],
    "criticals": [[CRITICAL VOLUME], [CRITICAL HEIGHT]]
  },
  ...
]
```

### Simulation `.LOCATIONS` snapshot

The `.LOCATIONS` snapshot file file includes details about each cell agent location.
This file can be used to as initial conditions by loading with `--locations`.

```python
[
  {
    "id": [ID],
    "center": [[CENTER_X], [CENTER_Y], [CENTER_Z]],
    "location": [
      {
        "region": [REGION],
        "voxels": [
          [[X], [Y], [Z]],
          ...
        ]
      },
      ...
    ]
  },
  ...
]
```
