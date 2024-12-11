## Registration to populations

The `action.register` tag registers the action to a specific population.
The registered population id must match a population defined in the [cell population setup]({{site.baseurl}}/setup/cell_populations).
Actions may or may not need to have registered populations, depending on the specific action class.
For example, the `remove` action does not need a registered population while the `insert` action may be registered to multiple populations.

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | population id                             |

### Example: Registering one population to an action

_The action is registered to population A._

```xml
<action.register id="A" />
```

### Example: Registering two populations to an action

_The action is registered to population A and B._

```xml
<action.register id="A" />
<action.register id="B" />
```
