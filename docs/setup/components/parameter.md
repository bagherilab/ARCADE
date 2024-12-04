## Component parameters

The `component.parameter` tag defines component parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.{{ include.implementation }}.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/{{ include.implementation }}/parameter.{{ include.implementation }}.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | parameter name                            |
| `value`   | new parameter value                       |
| `scale`   | scaling factor applied to parameter value |

### Example: Modifying component parameters

_The degradation rate parameter is set to the new value 2000._

```xml
<component.parameter id="DEGRADATION_RATE" value="2000" />
```

_The default value of the degradation rate parameter is scaled by 2._

```xml
<component.parameter id="DEGRADATION_RATE" scale="2" />
```

_The degradation rate parameter is set to the new value 10 * 2 = 20._

```xml
<component.parameter id="DEGRADATION_RATE" value="10" scale="2" />
```
