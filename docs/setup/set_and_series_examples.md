### Example: Set with single series

_Specifies a simulation set with one simulation series. Series A is simulated with random seeds 0, 1, 2._

```xml
<set prefix="SET_">
    <series name="A" start="0" end="2">
        <!-- Simulation series produces outputs: SET_A_0000, SET_A_0001, SET_A_0002 -->
    </series>
</set>
```

### Example: Set with multiple series

_Specifies a simulation set with three simulation series. Series A is simulated with random seeds 0, 1, 2. Series B is simulated with random seed 2. Series C is simulated with random seeds 1 and 2._

```xml
<set prefix="SET_">
    <series name="A" start="0" end="2">
        <!-- Simulation series produces outputs: SET_A_0000, SET_A_0001, SET_A_0002 -->
    </series>
    <series name="B" start="2" end="2">
        <!-- Simulation series produces outputs: SET_B_0002 -->
    </series>
    <series name="C" start="1" end="2">
        <!-- Simulation series produces outputs: SET_C_0001, SET_C_0002 -->
    </series>
</set>
```
