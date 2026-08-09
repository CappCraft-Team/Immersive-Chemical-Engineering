# Immersive Chemical Engineering

This branch contains the Minecraft 1.20.1 / Forge 47 port of the original
1.12.2 mod. The legacy implementation remains available from the older branch
and repository history.

## Current port status

The small, medium, and large heat exchangers use IE 10.2 multiblock
registrations with formation/disassembly, persistent tanks, sided fluid
capabilities, comparators, server ticking, atomic heat-balanced exchange,
ported four-tank GUIs, and the legacy formed OBJ models running through IE's
modern split model loader. The Engineer's Manual documents construction,
materials, controls, and exact fluid-port layouts for all three sizes. JEI reads
the client-synchronized recipe set and links each machine GUI to its directional
conversion displays.

Right-click any formed part with the main hand to open the GUI. Sneak-use passes
through for pipe/tool interaction. The arrow buttons deliberately discard both
tanks on that side, matching the original mod; the center button clears all four
tanks. Filter buttons lock or unlock a non-empty tank's current fluid type.

Each output port automatically offers up to 1,000 mB per server tick to an
adjacent fluid handler, including an IE pipe network with an accepting
destination, even while the exchanger is idle or cooling down. The two outputs
eject independently and remain available for external extraction. The connected
pipe network or destination may impose a lower transfer rate.

## Build

Forge 1.20.1 requires a Java 17 Gradle runtime. Point `JAVA_HOME` and the IDE's
Gradle JVM at JDK 17, then run:

```text
gradlew.bat clean build
```

The Java toolchain declaration does not replace selecting Java 17 for the
Gradle process itself.

## Datapack recipes

Heat exchanger conversions use normal datapack recipes and are replaced on
`/reload`. Put recipes below `data/<namespace>/recipes/`:

```json
{
  "type": "immersivechemical:heat_exchanger",
  "fluid_a": "example:hot_fluid",
  "amount_a": 1000,
  "fluid_b": "example:cold_fluid",
  "amount_b": 1000,
  "heat": 500,
  "direction": "two_way"
}
```

`direction` accepts `cool_down`, `heat_up`, or `two_way`. Fluid IDs must exist,
amounts and heat must be positive integers, and the two fluids must be distinct
and have different registered temperatures. No artificial water/lava recipe is
bundled; packs should define conversions for their actual process fluids.

When the two sides use different heat values, the machine scales both recipe
batches to the smallest whole-number, energy-balanced transaction. Keep heat
quanta and fluid amounts reasonably sized so the balanced batch fits its tanks.

## KubeJS recipes

KubeJS 6.5 is optional. With it installed, IChemE registers a recipe schema for
the native recipe DSL:

```js
ServerEvents.recipes(event => {
  event.recipes.immersivechemical.heat_exchanger(
    'example:hot_fluid', 1000,
    'example:cold_fluid', 1000,
    500,
    'two_way'
  ).id('kubejs:immersivechemical/example_exchange')
})
```

The dependency-free generic form also remains supported:

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'immersivechemical:heat_exchanger',
    fluid_a: 'example:hot_fluid',
    amount_a: 1000,
    fluid_b: 'example:cold_fluid',
    amount_b: 1000,
    heat: 500,
    direction: 'two_way'
  }).id('kubejs:immersivechemical/example_exchange')
})
```

Recipe scripts belong in `kubejs/server_scripts` and are rebuilt by `/reload`;
custom-fluid definitions belong in `kubejs/startup_scripts` and require a full
restart. See `examples/kubejs/README.md` for a complete four-fluid, two-rule
development fixture. KubeJS remains optional in normal development runs; add
`-PenableKubeJsRun=true` to a Gradle `runClient` or `runServer` command to load
the pinned KubeJS/Rhino/Architectury test runtime.

The old 1.12 CraftTweaker-specific Java API is not part of this port. Datapacks
and the tested KubeJS integration are the supported recipe-authoring paths.

## Porting findings

See [`PORTING_FINDINGS.md`](PORTING_FINDINGS.md) for the reusable technical
findings from this migration, including Forge/IE API changes, fluid transaction
safety, model conversion, recipe synchronization, and the verification matrix.
