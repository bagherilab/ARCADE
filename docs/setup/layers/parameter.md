## Layer parameters

The `layer.parameter` tag defines layer parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.{{ include.implementation }}.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/{{ include.implementation }}/parameter.{{ include.implementation }}.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`
- Changes for parameters specific to a operation can be applied to using the `operation` attribute

| ATTRIBUTE   | DESCRIPTION                               |
| ----------- | ----------------------------------------- |
| `id`        | parameter name                            |
| `value`     | new parameter value                       |
| `scale`     | scaling factor applied to parameter value |
| `operation` | operation the parameter belongs to        |

### Example: Modifying layer parameters

_The initial concentration parameter is set to the new value 2000._

```xml
<layer.parameter id="INITIAL_CONCENTRATION" value="2000" />
```

_The default value of the initial concentration parameter is scaled by 2._

```xml
<layer.parameter id="INITIAL_CONCENTRATION" scale="2" />
```

_The initial concentration parameter is set to the new value 10 * 2 = 20._

```xml
<layer.parameter id="INITIAL_CONCENTRATION" value="10" scale="2" />
```

### Example: Modifying layer operation parameters

_The diffusivity parameter for the diffuser operation is set to the new value 3._

```xml
<layer.parameter id="DIFFUSIVITY" operation="diffuser" value="3" />
```
