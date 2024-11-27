## Action creation

The `action` tag attributes are used to create the action.

- Each action must have a unique `id`

Valid options for actions include:

{% if include.implementation == "patch" %}
`insert`
: Inserts cell agents into the simulation

`remove`
: Remove cell agents from the simulation

`convert`
: Converts cell agents to a different population
{% endif %}

| ATTRIBUTE | DESCRIPTION                | TYPE    | REQUIRED |
| --------- | -------------------------- | ------- | -------- |
| `id`      | unique name for the action | string  | Y        |
| `class`   | action class option        | string  | Y        |

### Example: Initializing one agent action

_Specifies an agent action, A, with the insert action class._

```xml
<actions>
    <action id="A" class="insert" />
</actions>
```

### Example: Initializing two agent actions

_Specifies two agent actions, A and B. Action A uses the insert action class, and action B uses the convert action class._

```xml
<actions>
    <action id="A" class="insert" />
    <action id="B" class="convert" />
</actions>
```
