# ARCADE

__Agent-based Representation of Cells And Dynamic Environments__

- **[Code structure overview](#code-structure-overview)**
- **[Building from source](#building-from-source)**
- **[Running the code](#running-the-code)**
- **[Setup file structure](#setup-file-structure)**
    - [`<simulation>` tags](#simulation-tags)
        - [`<profilers>`](#profilers)
        - [`<checkpoints>`](#checkpoints)
    - [`<agents>` tags](#agents-tags)
        - [`<populations>`](#populations)
        - [`<helpers>`](#helpers)
    - [`<environment>` tags](#environment-tags)
        - [`<globals>`](#globals)
        - [`<components>`](#components)

## Code structure overview

`arcade.jar` is a compiled jar of the model, with the required library [MASON](https://cs.gmu.edu/~eclab/projects/mason/) included.

The `src/` directory contains all source files for the model.

The `docs/` directory contains class documentation (`javadoc`), sample input XML files (`sample_input`), and sample output JSON files (`sample_output`). There is also a `README` file in the `docs/` directory with more details on the sample input and output files.

## Building from source

The model can be built from source using Gradle. The included  `build.gradle` uses a `lib/` directory with the following libraries:

- `mason.19.jar`
- `vecmath.jar`
- `j3dcore.jar`
- `j3dutils.jar`

To build, use:

```bash
$ ./gradlew build
```

Documentation can be regenerated using:

```bash
$ ./gradlew javadoc
```

## Running the code

The model can be run directly through command line or through a GUI interface, as well as with or without real time visualization.

When using command line, first navigate to the directory holding this file. Then:

```bash
$ java -jar arcade.jar XML [(-v|--vis)]


    XML
        Path to .xml setup file
    [(-v|--vis)]
        Flag for running with visualization

```

For example, to run the model with setup file `setup.xml` with visualization:

```bash
$ java -jar arcade.jar /path/to/setup.xml --vis
```

You can also double click the `arcade.jar` file, which will open up a GUI dialog for selecting the setup file and running simulations. This dialog is also opened if a setup file is not provided to the command line:

```bash
$ java -jar arcade.jar
```

Note that running the model with visualization (either directly from the `arcade.jar` or using the `--vis` flag) is significantly slower than without visualization.

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
        <environment coordinate="COORD">
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

The __simulation__ tag describes the size and type of the simulation. Nested tags include various profilers and checkpointing for the simulation.

- `type` = simulation type (only `growth` is currently supported)
- `[radius]` = (integer) radius of simulation (default = `34`)
- `[height]` = (integer) height of simulation (default = `1`)
- `[margin]` = (integer) margin of simulation (default = `6`)

The __agents__ tag describes the agents initialization. Nested tags include definitions of the various cell populations to include (as well as cell modules and/or population parameters) and any helpers.

- `initialization` = (integer) radius to which cell agents are seeded
    + use `0` for no cells
    + use `FULL` to seed up to the `RADIUS` of the simulation

The __environment__ tag describes the environment. Nested tags include environment parameters and components.

- `coordinate` = (string) coordinate geometry
    + use `hex` for hexagonal
    + use `rect` for rectangular

### `<simulation>` tags

#### `<profilers>`

__Profilers__ save the simulation state to `.json` files at the selected interval. Different profilers will save different types of information. These are not used when the model is run with visualization.

```xml
<profilers>
    <profiler type="growth" interval="INTERVAL" suffix="SUFFIX" />
    <profiler type="parameter" interval="INTERVAL" suffix="SUFFIX" />
    <profiler type="graph" interval="INTERVAL" suffix="SUFFIX" />
    ...
</profilers>
```

- __growth__ saves a profile of cell state (including volume and cycle length) and a span of concentrations at the given `interval`
    + `interval` = (integer) minutes between each profile
    + `[suffix]` = appended to the output file name before the extension
- __parameter__ saves a profile of cell parameters at the given `interval`
    + `interval` = (integer) minutes between each profile
    + `[suffix]` = appended to the output file name before the extension
- __graph__ saves a profile of the vascular graph (nodes, edges, and hemodynamic properties) at the given `interval`
    + `interval` = (integer) minutes between each profile
    + `[suffix]` = appended to the output file name before the extension

#### `<checkpoints>`

__Checkpoints__ save certain parts of the simulation to a `.checkpoint` file, which can later be loaded. These are included when the model is run with visualization, although the seed may not match. They can either be class `SAVE` or `LOAD`, depending on if the checkpoint is being saved from or loaded to the simulation.

```xml
<checkpoints>
    <checkpoint type="graph" class="CLASS" name="NAME" path="PATH" day="DAY" />
    ...
</checkpoints>
```

- __graph__ saves a checkpoint of the graph structure OR loads a checkpoint of the graph structure (only use with the __sites__ / __graph__ component)
    + `name` = name of the checkpoint file
    + `path` = path to the checkpoint file directory
    + `[day]` = (integer) day that the checkpoint is to be saved (graph checkpoints can only be loaded when the simulation is initialized, so `day = 0`)

### `<agents>` tags

#### `<populations>`

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

#### `<helpers>`

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

### `<environment>` tags

#### `<globals>`

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

#### `<components>`

__Components__ define various environmental components included in the simulation. If a `sites` type component is not specified, the default is a constant source environment.

All components have specifications (shown below only for the __sites__ / __source__ component). If not specified for a given component, default values are used.

```xml
<components>
    <component type="sites" class="source">
        <specifications>
            <specification id="SPECIFICATION_NAME" value="VALUE" />
            ...
        </specifications>
    </component>
    <component type="sites" class="pattern" />
    <component type="sites" class="graph" complexity="COMPLEXITY" />
    <component type="remodel" interval="INTERVAL" />
    <component type="degrade" interval="INTERVAL" />
    <component type="cycle" />
    <component type="pulse" />
    ...
<components>
```

- __sites__ / __source__ uses source-based sites, with specifications for the spacing of sites in the x, y, and z directions, as well as any damage scaling
- __sites__ / __pattern__ uses pattern-based sites, with specifications for hemodynamic factor weighting, relative contribution of the hemodynamic factors, and damage scaling
- __sites__ / __graph__ uses graph-based sites, with specifications for layout type (default `*` matches the layout of __sites__ / __pattern__) and locations of roots
    + `[complexity]` = if `simple`, the graph sites will use simplified hemodynamics (analogous to the methods used by __sites__ / __source__ and __sites__ / __pattern__ without factors), otherwise, the graph sites use exact hemodynamic calculations
    + note that graph sites is not designed for 3D simulations and will only be grown in the 2D plane for simulations where `HEIGHT` > 1
- __degrade__ degrades the cell wall of vessels located where there are non-healthy (`H`) cell agents (only use with __sites__ / __graph__)
    + `interval` = (integer) minutes between degradation
- __remodel__ remodels the cell wall and vessel radius based on hemodynamic properties (only use with __sites__ / __graph__)
    + `interval` = (integer) minutes between remodeling
- __cycle__ cycles the source concentraiton of the specified molecule (only use with __sites__ / __source__)
- __pulse__ pulses the source concentraiton of the specified molecule (only use with __sites__ / __source__)
