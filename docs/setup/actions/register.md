## Registration to populations

The `action.register` tag registers the action to a specific population.
The registered population id must match a population defined in the [cell population setup]({{site.baseurl}}/setup/cell_populations).

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | population id                             |

### Example: Registering an action

_The action is registered to population A._

```xml
<action.register id="A" />
```
