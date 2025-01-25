## Links between populations

The `population.link` tag defines links between populations.
These links define transitions between populations during cell division.
If not defined, the default behavior is to produce a daughter cell in the same population as the mother cell.

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | linked population id                      |
| `weight`  | relative probability weighting            |

### Example: Adding links with equal weights to other populations

_Specifies links to populations A and B. When a cell in this population divides, the daughter cell has a 50% chance of being in population B and a 50% chance of being in population C._

```xml
<population.link id="A" weight="1" />
<population.link id="B" weight="1" />
```

Note that this specification is equivalent to:

```xml
<population.link id="A" weight="50" />
<population.link id="B" weight="50" />
```

### Example: Adding links with unequal weights to other populations

_Specifies links to populations A, B, and C. When a cell in this population divides, the daughter cell has a 20% chance of being in population A, a 30% change of being in population B, and a 50% chance of being in population C._

```xml
<population.link id="A" weight="20" />
<population.link id="B" weight="30" />
<population.link id="C" weight="50" />
```
