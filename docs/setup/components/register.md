## Registration to layers

The `component.register` tag registers the component to a specific layer.
The registered layer id must match a layer defined in the [lattice layer setup]({{site.baseurl}}/setup/lattice_layers).

| ATTRIBUTE | DESCRIPTION |
| --------- | ----------- |
| `id`      | layer id    |

### Example: Registering a component

_The component is registered to layer A._

```xml
<component.register id="A" />
```
