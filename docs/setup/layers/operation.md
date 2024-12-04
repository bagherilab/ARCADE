## Quantity operations

The `layer.operation` tag lists operations on quantities in the layer.
Valid options include:

`GENERATOR`
: Operation for generating the quantity in the lattice

`DIFFUSER`
: Operation for diffusion the quantity in the lattice

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | operation option id                       |

### Example: Including operation versions

_Specifies a layer with generator and diffuser operations._

```xml
<layer.operation id="GENERATOR" />
<layer.operation id="DIFFUSER" />
```
