## Component creation

The `component` tag attributes are used to create the component.

- Each component must have a unique `id`
- Each component must specify a `class`

Valid options for classes include:

{% if include.implementation == "patch" %}
`source_sites`
: Sites for generator based on source layout

`pattern_sites`
: Sites for generator based on pattern layout

`graph_sites_simple`
: Sites for generator based on graph layout with simple hemodynamics

`graph_sites_complex`
: Sites for generator based on graph layout with complex hemodynamics

`pulse`
: Component for pulsing source concentrations
: _Requires `source_sites` component_

`cycle`
: Component for cycling source concentrations
: _Requires `source_sites` component_

`degrade`
: Component for degrading graph sites
: _Requires `graph_sites_simple` or `graph_sites_complex` component_

`remodel`
: Component for remodeling graph sites
: _Requires `graph_sites_simple` or `graph_sites_complex` component_
{% endif %}

| ATTRIBUTE | DESCRIPTION                   | TYPE    | REQUIRED |
| --------- | ----------------------------- | ------- | -------- |
| `id`      | unique name for the component | string  | Y        |
| `class`   | component class option        | string  | Y        |

### Example: Initializing one environment component

_Specifies an environment component, A, with the source sites component class._

```xml
<components>
    <component id="A" class="source_sites" />
</components>
```

### Example: Initializing two environment components

_Specifies two environment components, A and B. Component A uses the complex graph sites component class, and component B uses the remodeling component class._

```xml
<components>
    <component id="A" class="graph_sites_complex" />
    <component id="B" class="remodel" />
</components>
```
