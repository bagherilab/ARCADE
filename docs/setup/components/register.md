## Registration to layers

The `component.register` tag registers the component to a specific layer.
A component may be registered to more than one layer.
The registered layer id must match a layer defined in the [lattice layer setup]({{site.baseurl}}/setup/lattice_layers).

| ATTRIBUTE | DESCRIPTION |
| --------- | ----------- |
| `id`      | layer id    |

### Example: Registering a component to a single layer

_The component is registered to layer A._

```xml
<component.register id="A" />
```

### Example: Registering a component to multiple layers

_The component is registered to layer A and layer B._

```xml
<component.register id="A" />
<component.register id="B" />
```
