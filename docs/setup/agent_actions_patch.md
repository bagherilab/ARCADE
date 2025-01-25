---
title: Patch agent actions
nav_order: 1
parent: Agent actions
---

# Patch agent actions
{: .no_toc }

## Table of Contents
{: .no_toc .text-delta }

- TOC
{:toc}

The `action` tag defines a single action.
Nested tags include `action.parameter` for parameters and `action.register` for registration to a population.

```xml
<actions>
    <action id="[ID]" class="[CLASS]">
        <action.parameter id="[ID]" value="[VALUE]" scale="[SCALE]" />
        <action.register id="[ID]" />
        ...
    </action>
    ...
</actions>
```

{% assign implementation = "patch" %}
{% include_relative actions/creation.md implementation=implementation %}
{% include_relative actions/parameter.md implementation=implementation %}
{% include_relative actions/register.md %}
