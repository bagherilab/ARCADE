## Population initialization

The `population` tag attributes are used to initialize the population.

- Each population must have a unique `id`
{%- if include.implementation == "patch" %}
- For `init`, use `N%` to initialize with `N` percent of the total patches with cells
{% endif %}
{%- if include.implementation == "potts" %}
- For `init`, use `N:M` to initialize `N` cells with `M` voxels of padding between cells
{% endif %}

{% case include.implementation %}
  {% when "patch" %}
  {% assign default_class = "tissue" %}
  {% when "potts" %}
  {% assign default_class = "stem" %}
{% endcase %}

Valid options for classes include:

{% if include.implementation == "patch" %}
`tissue`
: Healthy tissue cell

`cancer`
: Cancerous tissue cell (inherits from `tissue`)

`cancer_stem`
: Cancerous stem cells (inherits from `cancer`)

`random`
: Cell with randomly assigned states.
{% elsif include.implementation == "potts" %}
`stem`
: Basic stem cell
{% endif %}

| ATTRIBUTE | DESCRIPTION                            | TYPE    | DEFAULT             | REQUIRED |
| --------- | -------------------------------------- | ------- | ------------------- | -------- |
| `id`      | unique name for the agent population   | string  |                     | Y        |
| `class`   | population cell class                  | string  | {{ default_class }} | N        |
| `init`    | number of initial agents in population | integer | 0                   | N        |

### Example: Initializing different agent classes

_Specifies an agent population, A, with the {{ default_class }} cell class._

```xml
<populations>
    <population id="A" class="{{ default_class }}" />
</populations>
```

### Example: Initializing one agent population

_Specifies one agent population, A. Population A is initialized with 10 agents._

```xml
<populations>
    <population id="A" init="10" />
</populations>
```

### Example: Initializing two agent populations

_Specifies two agent populations, A and B. Population A is initialized with 10 agents, and population B is initialized with 5 agents._

```xml
<populations>
    <population id="A" init="10" />
    <population id="B" init="5" />
</populations>
```

{%- if include.implementation == "patch" %}
### Example: Initializing single agent population with percentage

_Specifies one agent population, A. Population A is in 50% of the total patches in the simulation._

```xml
<populations>
    <population id="A" init="50%" />
</populations>
```
{% endif %}

{%- if include.implementation == "potts" %}
### Example: Initializing single agent population with padding between cells

_Specifies one agent population, A. Population A is initialized with 10 agents, with 3 voxels of padding between cells._

```xml
<populations>
    <population id="A" init="10:3" />
</populations>
```
{% endif %}
