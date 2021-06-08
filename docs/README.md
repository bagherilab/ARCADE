# ARCADE

__Agent-based Representation of Cells And Dynamic Environments__

- **[Class documentation](#class-documentation)**
- **[Sample input and output](#sample-input-and-output)**
    + [`0. no agents`](#0-no-agents)
    + [`1. healthy cells`](#1-healthy-cells)
    + [`2. random seeds`](#2-random-seeds)
    + [`3. multiple populations`](#3-multiple-populations)
    + [`4. environment parameters`](#4-environment-parameters)
    + [`5. colony growth`](#5-colony-growth)
    + [`6. nutrient sources`](#6-nutrient-sources)
    + [`7. vascular layouts`](#7-vascular-layouts)
    + [`8. graph checkponts`](#8-graph-checkpoint)

## Class documentation

The `javadoc` directory contains documentation for all classes. To view, open the `javadoc/index.html` file in a web browser.

## Sample input and output

The `sample_input` and `sample_output` directories contain sample input XML files and their corresponding output JSON files for sample simulations.

__Before running any sample simulations, make sure to change the `path="/path/to/output/"` attribute in the setup file to your desired output directory. Otherwise, the simulation output will not be saved.__

### 0. no agents

`0_no_agents.xml` is the simplest possible setup file, run for 0 days with no cell agents.

When running this without visualization, the simulation should complete almost immediately with no output.

When running this with visualization, it will take time to generate the visualization. With visualization, the specified time (0 days) does not matter; the simulation will run until "stop" or "pause" is clicked.

### 1. healthy cells

`1_healthy_cells.xml` includes one population of healthy cells seeded at the full radius, with the radius and margin changed from the default values to 4 and 2, respectively. The simulation is run for 2 days.

`1_healthy_cells_00.json` is the output of the growth profiler. The profiler takes profiles at intervals of 720 ticks (720 minutes) for five total timepoints over the 2 day simulation. The `init` value of `-1` indicates cell were initialized at the full radius.

Note that an initialization of 4 (`initialization="4"`) gives the same results.

### 2. random seeds

`2_random_seeds.xml` runs the same simulation as `1_healthy_cells.xml`, but for random seeds 4, 5, and 6. The `start` attribute denotes the starting random seed, and simulations are run for every random seed up until (but not including) the `end` attribute.

`2_random_seeds_04.json`, `2_random_seeds_05.json`, and `2_random_seeds_06.json` are the outputs for random seeds 4, 5, and 6. Note that the two number appended on the end are indicate the random seed.

### 3. multiple populations

`3_multiple_populations.xml` simulates two cell populations:

- population 0 has simple metabolism and random signaling modules
- population 1 has a `MAX_HEIGHT` parameter double the default value

The populations are initialized at a radius of 3, where population 0 is 80% of the cells and population 1 is 20% of the cells.

`3_multiple_populations_00.json` is the output of the simulation. The `"pops"` list under `"config"` summarizes the two populations, including the actual number of cells agents initialized (16 of population 0 and 3 of population 1). The actual ratios (84% and 16%) are not exact the same as the specified ratios because cells are discrete objects.

The `"pops"` list under `"parameters"` lists the parameter values specific to each population. For population 1, the `MAX_HEIGHT` parameter value is set to `17.4` (population 0 has the default value of `8.7`).

### 4. environment parameters

`4_environment_parameters.xml` simulates a cell population in an environment under three conditions: (i) no glucose or oxygen, (ii) no glucose but with oxygen, and (iii) no oxygen but with glucose. All three simulation series are included in the same simulation set. The population parameter of `NECRO_FRAC` is set to zero, indicating that all cells become apoptotic rather than necrotic (and are therefore removed from the simulation).

`4_environment_parameters_NO_GLUC_NO_OXY_00.json` is the output for no glucose or oxygen. Under `"globals"` in the `"parameters"` list, both `CONCENTRATION_GLUCOSE` and `CONCENTRATION_OXYGEN` are set to 0. In the `"timepoints"` list, at `time = "1.5"`, there are no longer any cells remaining (the `"cells"` list is empty).

`4_environment_parameters_NO_GLUC_00.json` is the output for no glucose but with oxygen. Here cells use autophagy to convert cell mass into glucose and are able to survive for a longer period of time. The number of cell decreases over time and the average cell volumes decrease from around 2200 at `time = "0.0"` to around 1100 `time = "11.5"`. After `time = "11.5"`, no cells remain.

`4_environment_parameters_NO_OXY_00.json` is the output for no oxygen but with glucose. The cells are able to survive by using glycolysis to produce energy.

### 5. colony growth

`5_colony_growth.xml` simulates the growth of a cell colony with cancerous cells using three methods: (i) cells are initialized at a radius of 2, (ii) cells are _inserted_ into the simulation after a delay of 1440 minutes (1 day), and (ii) the simulation is initialized with healthy cells and the healthy cell in the center is _converted_ into a cancerous cell after a delay of 1440 minutes.

`5_colony_growth_INITIALIZED_00.json` is the output for initializing the cells at a radius of 2. At `time = "0.0"`, there are 7 cells. By  `time = "4.0"`, there are 32 cells.

`5_colony_growth_INSERTED_00.json` is the output for inserting the cells at a radius of 2 using the `insert` helper. At `time = "0.0"` and `time = "0.5"`, there are no cells. At time `time = "1.0"`, the helper has been stepped and there are now 7 cells.

`5_colony_growth_CONVERTED_00.json` is the output for converting the center cell into a different population. At `time = "0.0"` and `time = "0.5"`, there are only healthy cells (population 0). At time `time = "1.0"`,  the cell at the center coordinate `[0,0,0,0]` is converted to population 1.

### 6. nutrient sources

`6_nutrient_sources.xml` simulates cell growth with different source patterns: (i) sources only on the top half of the environment, (ii) stripes of sources, and (iii) evenly distributed grid sources. All simulations are otherwise identical.

Running with visualization (`-v` or `--vis` flags in command line, or the _vis_ option in the GUI) will display the locations of the nutrient sources (remember that only the first simulation series in a setup file is loaded by the visualization; move the other series into the first position in the setup file to visualize them).

`6_nutrient_sources_TOP.json` is the output for nutrient sources in the top of half of the environment. On average, nutrient concentrations (the `"molecules"` object under each time point lists average concentrations of each molecule at each radius from the center of the simulation) are near source concentrations due to diffusion with glucose at 85% of source (4.230 umol/cm^3) at `time = "2.0"`.

`6_nutrient_sources_LINES.json` is the output for nutrient sources in lines. This layout introduces larger distances between cells and sources, resulting in lower concentrations with glucose at 68% of source (3.392 umol/cm^3) at `time = "2.0"`).

`6_nutrient_sources_GRID.json` is the output for nutrient sources located in a sparse grid. This option has the lowest concentrations with glucose at 29% of source (1.431 umol/cm^3) at `time = "2.0"`.

### 7. vascular layouts

`7_vascular_layouts.xml` demonstrates two different vascular layouts grown from root: (i) one arterial and one venous root in a "line" (`L`) layout and (ii) two arterial and two venous roots in a "single" (`S`) layout.

Running with visualization (`-v` or `--vis` flags in command line, or the _vis_ option in the GUI) will display the edges of the vascular graph (remember that only the first simulation series in a setup file is loaded by the visualization; move the other series into the first position in the setup file to visualize them) along with relevant hemodynamic properties.

Running the simulations with different seeds will generate different layouts as the growth process from the roots is stochastic.

`7_vascular_layouts_L11.GRAPH.json` and `7_vascular_layouts_S22.GRAPH.json` are the outputs of the graph profiler. The profiler takes profiles at intervals of 720 ticks (720 minutes) for three total timepoints over the 1 day simulation. Unlike the growth profiler, each timepoint now contains information about the vascular graph (nodes, edges, and hemodynamic properties) rather than cell agents. More than one profiler can be assigned to a simulation series.

### 8. graph checkpoints

`8_graph_checkpoints.xml` demonstrates graph checkpointing.

The first simulation series `A` in the set creates a checkpoint of the vascular graph `8_graph_checkpoints_00.checkpoint` after it is generated before running the rest of the simulation (this simulation will take longer than previous examples). The checkpoint contains the serialized version of the graph, which can then be loaded into other simulations.

The second simulation series `B` then loads the graph checkpoint into the simulation, which matches `A` except for the graph checkpoint. The simulation time for `B` is faster than for `A` by eliminating the time needed to generate the graph; there is less "lag" before the simulation starts.

The third simulation series `C` also loads the graph checkpoint, but with a different initialization of cells and using complex hemodynamics (this simulation will take the longest). Checkpoints enable multiple simulations to utilize the same graph under different initial conditions so that the model does not need to repeat the graph generation process.

Both `B` and `C` use parentheses around the `GRAPH_LAYOUT` value to indicate that the graph is will be loaded from a checkpoint so the model does not generate it again.
