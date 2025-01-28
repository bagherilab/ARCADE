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

ARCADE uses a setup file that describes the setup of the simulations, agents, and environment.

:small_orange_diamond: **Read the [setup files](https://bagherilab.github.io/ARCADE/setup) documentation for details on each section**

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
