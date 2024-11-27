## Population parameters

The `population.parameter` tag defines population parameters.
Unless modified, default values are used for all parameters.
Defaults are listed in [parameter.{{ include.implementation }}.xml](https://github.com/bagherilab/ARCADE/blob/main/src/arcade/{{ include.implementation }}/parameter.{{ include.implementation }}.xml).

- Either or both `value` and `scale` attributes can be applied, with `value` applied first
- Only numeric parameters can be modified using `scale`
- Changes for parameters specific to a module can be applied to using the `module` attribute
{%- if include.process %}
- Changes for parameters specific to a process can be applied to using the `process` attribute
{% endif %}

| ATTRIBUTE | DESCRIPTION                               |
| --------- | ----------------------------------------- |
| `id`      | parameter name                            |
| `value`   | new parameter value                       |
| `scale`   | scaling factor applied to parameter value |
| `module`  | module the parameter belongs to           |
{%- if include.process == true %}
| `process` | process the parameter belongs to          |
{% endif %}

Parameter values can be defined as distributions, rather than a single value, using the `value="DISTRIBUTION_TYPE(PARAM=VALUE,...)"` syntax.
Cells will draw from the parameter distribution for their population to set parameter values.
Valid distributions include:

`UNIFORM`
: Uniform distribution between minimum value `MIN` and maximum value `MAX`

`NORMAL`
: Normal distribution with mean `MU` and stdev `SIGMA`

`TRUNCATED_NORMAL`
: Normal distribution with mean `MU` and stdev `SIGMA` truncated at ± 2σ

`FRACTIONAL_NORMAL`
: Normal distribution with mean `MU` and stdev `SIGMA` bounded between 0 and 1

`BERNOULLI`
: Bernoulli distribution with probability of success `PROBABILITY`

### Example: Modifying population parameters

_The critical volume parameter is set to the new value 2000. The default value of the critical height parameter is scaled by 2. The cell age parameter is set to the new value 10 * 2 = 20._

```xml
<population.parameter id="CRITICAL_VOLUME" value="2000" />
<population.parameter id="CRITICAL_HEIGHT" scale="2" />
<population.parameter id="CELL_AGE" value="10" scale="2" />
```

### Example: Modifying population parameters with distributions

_The critical volume parameter is drawn from a normal distribution with mean of 2000 and standard deviation of 100. The cell age parameter is drawn from a uniform distribution between 10 and 20._

```xml
<population.parameter id="CRITICAL_VOLUME" value="NORMAL(MU=2000,SIGMA=100)" />
<population.parameter id="CELL_AGE" value="UNIFORM(MIN=10,MAX=20)" />
```

### Example: Modifying module parameters

_The cell growth rate parameter in the proliferation module is set to the new value 20._

```xml
<population.parameter id="CELL_GROWTH_RATE" module="proliferation" value="20" />
```

{%- if include.process %}
### Example: Modifying process parameters

_The ATP production rate parameter in the metabolism process is set to the new value 10._

```xml
<population.parameter id="ATP_PRODUCTION_RATE" process="metabolism" value="10" />
```
{% endif %}
