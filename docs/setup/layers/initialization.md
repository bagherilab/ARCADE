## Layer initialization

The `layer` tag initialization are used to initialize the layer.

- Each layer must have a unique `id`

| ATTRIBUTE | DESCRIPTION               | TYPE    | REQUIRED |
| --------- | ------------------------- | ------- | -------- |
| `id`      | unique name for the layer | string  | Y        |

### Example: Initializing one lattice layer

_Specifies a lattice layer, A._

```xml
<layers>
    <layer id="A" />
</layers>
```

### Example: Initializing two lattice layers

_Specifies two lattice layers, A and B._

```xml
<layers>
    <layer id="A" />
    <layer id="B" />
</layers>
```
