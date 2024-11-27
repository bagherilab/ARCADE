## Action parameters

The `action.parameter` tag defines action parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.{{ include.implementation }}.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/{{ include.implementation }}/parameter.{{ include.implementation }}.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | parameter name                            |
| `value`   | new parameter value                       |
| `scale`   | scaling factor applied to parameter value |

### Example: Modifying action parameters

_The time delay parameter is set to the new value 2000. The default value of the insert radius parameter is scaled by 2. The insert number parameter is set to the new value 10 * 2 = 20._

```xml
<action.parameter id="TIME_DELAY" value="2000" />
<action.parameter id="INSERT_RADIUS" scale="2" />
<action.parameter id="INSERT_NUMBER" value="10" scale="2" />
```
