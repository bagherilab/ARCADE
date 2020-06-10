# ARCADE

__Agent-based Representation of Cells And Dynamic Environments__

- [File structure](#file-structure)
- [Running the code](#running-the-code)
- [Setup file structure](#setup-file-structure)
- [`<simulation>` tags](#simulation-tags)
    + [`<profilers>`](#profilers)
- [`<agents>` tags](#agents-tags)
    + [`<populations>`](#populations)
    + [`<helpers>`](#helpers)
- [`<environment>` tags](#environment-tags)
    + [`<globals>`](#globals)

---

## File structure

The contents of this directory include:

	README.md
	LICENSE
	arcade.jar
	src/
	docs/

The `arcade.jar` is a compiled jar of the source code, with the required library [MASON](https://cs.gmu.edu/~eclab/projects/mason/) included.

The `src/` directory contains all source files for the model.

The `docs/` directory contains class documentation (`javadoc`), sample input XML files (`sample_input`), sample output JSON files (`sample_output`), and the setup files used for the manuscript. There is also a `README` file in the `docs/` directory with more details on the sample input and output files.

If modifying source code or compiling from source, we suggest also including the following directories:

	lib/
	bin/

The `lib` directory should hold the `mason.jar` library (version 19 or later) and any additional libraries that you may need.

The `bin` directory holds the compiled code.

---

## Running the code

The model can be run directly through command line or through a GUI interface, as well as with or without real time visualization.

When using command line, first navigate to the directory holding this file. Then:

```bash
java -jar arcade.jar XML [(-v|--vis)]


    XML
        Path to .xml setup file
    [(-v|--vis)]
        Flag for running with visualization

```

For example, to run the model with setup file `setup.xml` with visualization:

```bash
java -jar arcade.jar /path/to/setup.xml --vis
```

You can also double click the `arcade.jar` file, which will open up a GUI dialog for selecting the setup file and running simulations. This dialog is also opened if a setup file is not provided to the command line:

```bash
java -jar arcade.jar
```

Note that running the model with visualization (either directly from the `arcade.jar` or using the `--vis` flag) is significantly slower than without visualization.

---

## Setup file structure

The model uses an `.xml` file that describes the setup of the simulations, agents, and environments. Attributes in brackets (`[]`) are optional. Values in `ALL CAPS` should be replaced with actual values.

The basic structure of the file is:

```xml
<set prefix="PREFIX" path="PATH">
    <series name="NAME" start="START" end="END" days="DAYS">
        <simulation type="TYPE" radius="RADIUS" height="HEIGHT" margin="MARGIN">
            <!-- SIMULATION TAGS HERE -->
        </simulation>
        <agents initialization="INIT">
            <!-- AGENTS TAGS HERE -->
        </agents>
        <environment>
            <!-- ENVIRONMENT TAGS HERE-->
        </environment>
    </series>
    ...
</set>
```

A group of simulations series is called a __set__. The setup XML can only include one set.

- `path` = absolute path to the output directory
- `[prefix]` = prefix for output files

A simulation __series__ is a group of simulations that only differ in the random seed. Each `<series>` includes `<simulation>`, `<agents>`, and `<environment>` tags that define various parts of the series. A set can include multiple `<series>` tags.

- `name` = name of the simulation series for output files
- `[start]` = (integer) starting seed (default = `0`)
- `[end]` = (integer) ending seed (default = `1`)
- `days` = (integer) number of days for each simulation

The __simulation__ tag describes the size and type of the simulation. Nested tags include various profilers for the simulation.

- `type` = simulation type (only `growth` is currently supported)
- `[radius]` = (integer) radius of simulation (default = `34`)
- `[height]` = (integer) height of simulation (default = `1`)
- `[margin]` = (integer) margin of simulation (default = `6`)

The __agents__ tag describes the agents initialization. Nested tags include definitions of the various cell populations to include (as well as cell modules and/or population parameters) and any helpers.

- `initialization` = (integer) radius to which cell agents are seeded
    + use `0` for no cells
    + use `FULL` to seed up to the `RADIUS` of the simulation

The __environment__ tag describes the environment. Nested tags include environment parameters.

---

## `<simulation>` tags

### `<profilers>`

__Profilers__ save the simulation state to `.json` files at the selected interval. Different profilers will save different types of information. These are not used when the model is run with visualization.

```xml
<profilers>
    <profiler type="growth" interval="INTERVAL" suffix="SUFFIX" />
    <profiler type="parameter" interval="INTERVAL" suffix="SUFFIX" />
    ...
</profilers>
```

- __growth__ saves a profile of cell state (including volume and cycle length)
and a span of concentrations at the given `interval`
    + `interval` = (integer) minutes between each profile
    + `[suffix]` = appended to the output file name before the extension
- __parameter__ saves a profile of cell parameters at the given `interval`
    + `interval` = (integer) minutes between each profile
    + `[suffix]` = appended to the output file name before the extension

---

## `<agents>` tags

### `<populations>`

__Populations__ define the cell populations included in the simulation. Within each __population__, you can optionally define __variables__ and __modules__.

```xml
<populations>
    <population type="TYPE" fraction="FRACTION">
        <variables>
            <variable id="PARAMETER_NAME" value="VALUE" scale="SCALE" />
            ...
        </variables>
        <modules>
            <module type="TYPE" version="VERSION" />
            ...
        </modules>
    </population>
    ...
</populations>
```

The __population__ tag describes the type and fraction of a cell population included in the model.

- `type` = cell type (`H` = healthy, `C` = cancerous, `S` = cancer stem cell)
- `fraction` = (number) fraction of this population in the initialized cells, total fractions across all populations should sum to 1.0

The __variable__ tag lists parameters for the population that are either set to a new value or scaled from the default value. Unless modified, default values are used for all cell parameters. Either or both `value` and `scale` attributes can be applied, with `value` applied first.

- `id` = name of the parameter to be modified
- `[value]` = (number) new value for the parameter
- `[scale]` = (number) scale value of the parameter

The __module__ tag lists the cell modules. When undefined, complex metabolism and complex signaling are used by default.

- `type` = module type, currently support for `metabolism` and `signaling`
- `version` = version of the module (`C` = complex, `M` = medium, `S` = simple, `R` = random)

### `<helpers>`

__Helpers__ define the various helper agents included in the simulation. Depending on the type, different attributes are required.

```xml
<helpers>
    <helper type="convert" delay="DELAY" population="POPULATION" />
    <helper type="insert" delay="DELAY" populations="POPULATIONS" bounds="BOUNDS"/>
    <helper type="wound" delay="DELAY" bound="BOUNDS" />
    ...
<helpers>
```

- __convert__ changes the cell in the center of the simulation to a cell of the given `population` after `delay` time has passed
    + `population` = (integer) zero-indexed population number for the cell to convert to
    + `delay` = (integer) minutes before convert occurs
- __insert__ adds a mix of cells of the selected `populations` at a radius of `bounds`x`RADIUS` after `delay` time has passed
    + `populations` = (comma-separated integers) list of zero-indexed population numbers to add
    + `bounds` = (number) fraction of total simulation radius to add cells
    + `delay` = (integer) minutes before insert occurs
- __wound__ removes all cells within a radius of `bounds`x`RADIUS` after `delay` time has passed
    + `bounds` = (number) fraction of total simulation radius to remove cells
    + `delay` = (integer) minutes before wound occurs

---

## `<environment>` tags

### `<globals>`

__Globals__ define environment parameters that are changed, analogous to the population-specific variables. Note that some global parameters may only apply to certain components. Unless modified, default values are used for global parameters. Either or both `value` and `scale` attributes can be applied, with `value` applied first.

```xml
<globals>
    <global id="PARAMETER_NAME" value="VALUE" scale="SCALE" />
    ...
<globals>
```

- `id` = name of the parameter to be modified
- `[value]` = (number) new value for the parameter
- `[scale]` = (number) scale value of the parameter
