## Subcellular regions

The `population.region` tag lists subcellular regions for the population.
If regions are not specified, only the `DEFAULT` region is used.
Valid options include:

`DEFAULT`
: Region representing the entire cell

`NUCLEUS`
: Region representing the nucleus

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | region option id                          |

### Example: Including cell and nuclear regions

_Specifies a population with both default and nuclear regions._

```xml
<population.region id="DEFAULT" />
<population.region id="NUCLEUS" />
```
