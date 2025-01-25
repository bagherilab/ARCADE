## Intracellular processes

The `population.process` tag lists intracellular process versions for the population.
If process versions are not specified, the `random` version for each process is used.
Valid options include:

`METABOLISM`
: Process for cell metabolism (`random`, `simple`, `medium`, `complex`)

`SIGNALING`
: Process for cell signaling (`random`, `simple`, `medium`, `complex`)

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | process option id                         |
| `version` | process version                           |

To modify process parameters, use the [`population.parameter`](#population-parameters) tag with the corresponding `process` attribute.

### Example: Including process versions

_Specifies a population with complex metabolism and simple signaling processes._

```xml
<population.process id="METABOLISM" version="complex" />
<population.process id="SIGNALING" version="simple" />
```
