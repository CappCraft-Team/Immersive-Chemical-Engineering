# Porting findings: Forge 1.12.2 to 1.20.1 with Immersive Engineering

This document captures reusable lessons from porting Immersive Chemical
Engineering from Minecraft 1.12.2 to 1.20.1. It is intended as a practical
reference for other projects migrating old Forge and Immersive Engineering
code, not as a line-by-line history of this repository.

## Reference stack

- Minecraft 1.20.1
- Forge 47.3.0
- Java 17
- Gradle 8.5 and ForgeGradle 6.0.54
- Immersive Engineering 10.2.0-183
- JEI 15.2.0.23
- KubeJS 2001.6.5-build.26, optionally loaded

Pin the versions used by the project. Broad plugin ranges and snapshots make a
port difficult to reproduce and can turn a source problem into a toolchain
problem.

## Build and dependency lessons

1. The Gradle process itself must run on Java 17. A Java toolchain declaration
   controls compilation but does not change the JVM that starts Gradle.
2. Do not resolve `runtimeClasspath` during ForgeGradle configuration. Doing so
   can resolve the special `minecraft` configuration too early and produce
   misleading dependency and mutation errors.
3. Avoid ad-hoc dependency merging with `zipTree`. Prefer a supported Jar-in-Jar
   mechanism, or remove the dependency when the required algorithm is small.
   This port replaced JGraphT with a deterministic, dependency-free graph.
4. Track the Gradle wrapper JAR, properties, and launch scripts. Ignore build
   output and local caches, not the wrapper required by a clean clone.
5. Validate the built JAR, not only the development classpath. Check that it
   contains the expected metadata/resources and does not accidentally bundle
   Forge, Minecraft, JEI, KubeJS, or other dependency packages.

## Registration and side safety

Old proxy classes, metadata blocks, manual registries, and `GuiHandler` hooks do
not map directly to modern Forge.

- Use `DeferredRegister` for blocks, items, block entities, menus, recipe types,
  and serializers.
- Attach every deferred register to the mod event bus before registry freeze.
- Enqueue Immersive Engineering multiblock registration from common setup.
- Keep screens, render code, and JEI integration out of common initializers.
  Use client-only subscribers or lifecycle hooks so a dedicated server never
  resolves client classes.
- Remember that Forge, JEI, KubeJS, JUnit, and resource loaders discover some
  classes and files reflectively or by convention. A text-reference search
  alone is not sufficient proof that such a file is unused.

## Modern Immersive Engineering multiblocks

Modern IE separates registration, immutable logic, per-machine state, and the
formation template.

- Build one `MultiblockRegistration` per machine size and register its formed
  block, item, master/dummy block entities, logic, and structure definition.
- Put all mutable runtime values in the state object. A field on a shared logic
  instance is shared by every formed machine.
- Implement `IServerTickableComponent` when server ticks are required; a method
  named `tickServer` is not dispatched merely because it exists.
- Treat template coordinates as canonical. Master and trigger positions are not
  necessarily zero, especially when preserving an old part-index layout.
- IE model offsets are `positionInMultiblock - masterPositionInMultiblock`.
  `basic_split` keys and OBJ geometry must use those master-relative offsets.
- Let IE transform canonical positions for facing and mirroring. Do not redo the
  same world-coordinate rotation in capabilities, menus, or model code.
- Formation must replace every structure block, initialize master/dummy state,
  and support disassembly. Merely returning `true` from a structure check does
  not create a working multiblock.

Code-backed `StructureTemplate` data can be used when a legacy structure has no
NBT template, but it must still provide real template data for IE's client login
and manual synchronization.

## Fluid semantics and capabilities

Forge fluid APIs no longer use `null` as an empty fluid value.

- Store and return `FluidStack.EMPTY`.
- Test `FluidStack.isEmpty()` rather than checking for `null`.
- Failed drains must return `FluidStack.EMPTY`.
- Simulated operations must not mutate state or invoke change callbacks.
- Copy incoming and returned stacks where ownership is not transferred.
- Tank callbacks should mark the master dirty and synchronize only after real
  mutations.

Expose ports with IE `CapabilityPosition` values and direction-restricted
handlers. An input handler must not drain its tank, and an output handler must
not fill its tank. Test every physical part and side, including unsided access.

For active output, cache adjacent `CapabilityReference<IFluidHandler>` instances
in each machine state using the initial multiblock context. The external target
for a port is one block along its outward face, queried on the opposite face.
This automatically follows facing and mirroring, refreshes invalidated
capabilities, and does not force-load chunks.

Push output before cooldown and no-recipe early returns so completed products do
not remain stuck while the machine is idle. Use a simulate-first Forge transfer
and keep both outputs independent. IE pipes store no fluid themselves and may
apply a lower network throughput than the machine's offered rate, so a pipe
needs an accepting downstream destination.

## Transaction-safe processing

The legacy exchanger could convert one side, bank heat, and later attempt the
other side. That permits partial conversion, stranded heat after reload, and
fluid loss when an output changes between checks.

The safer modern design performs one atomic, heat-balanced transaction:

1. Determine the integer repetition count for both conversions using the GCD of
   their heat values.
2. Scale both fluid batches with checked arithmetic.
3. Simulate both input drains and both output fills.
4. Execute only when all four operations can succeed exactly.
5. Verify execution results and roll back unexpected partial mutations.

For multi-step conversion paths, intermediate fluid amounts must also balance.
Compose rational repetition ratios, reduce them, integerize with an LCM, and
reject any transaction whose minimal exact amounts or heat exceed the runtime
integer representation.

## Datapack recipes and reloads

Use a real `RecipeType` and `RecipeSerializer` rather than a mutable global list
populated during common setup.

- Validate resource locations, direction values, positive amounts, positive
  heat, distinct fluids, and distinct registered temperatures.
- Validate network payloads as strictly as JSON payloads.
- Rebuild the derived lookup graph after `RecipeManager` finishes applying a
  reload, then replace the old graph as one snapshot.
- Clear generated shortcuts when recipes are removed or replaced.
- Sort recipe IDs before inserting them when duplicate resolution or traversal
  order must be deterministic.
- Do not keep world or tank work on an executor. A small immutable lookup is
  fast enough on the server tick and avoids unsafe off-thread state access.

No universal conversion should be invented merely to make a machine appear
active. Ship real domain recipes or provide an explicit example datapack/script
fixture.

## KubeJS integration

Any registered datapack serializer can already be used with KubeJS
`event.custom(...)`. A custom KubeJS schema is an ergonomic layer for the native
recipe DSL, not a requirement for functionality.

- Keep KubeJS optional in metadata and class loading.
- Register the plugin through `kubejs.plugins.txt` when providing a schema.
- Let the IChemE serializer remain the final validation boundary.
- Custom fluids belong in `startup_scripts` and require a full restart.
- Recipe scripts belong in `server_scripts` and are rebuilt by `/reload`.
- Verify startup and reload both with and without KubeJS installed.

## JEI synchronization and GUI integration

A static registry populated by the logical server is empty in a remote client
process. JEI recipes must come from the client's synchronized `RecipeManager`.
JEI 15 restarts its recipe registration after the relevant recipe and tag update
events, so reading the client manager during `registerRecipes` handles login and
`/reload` without a custom recipe-sync packet.

- Split a bidirectional conversion into two directional JEI displays so input
  and output pairings remain unambiguous.
- Register all machine variants as catalysts.
- Register recipe click areas for every concrete screen class.
- Give a shared click area a single tooltip owner. Rendering a machine-status
  tooltip and JEI's “Show Recipes” tooltip at the same cursor position produces
  overprinted text.

For menus, synchronize only the data the screen needs. Validate every client
action against the current menu, server player, formed master, dimension,
distance, allowed action, and tank index. Do not trust a client-supplied block
position as authority.

## OBJ and split-model migration

The old OBJ files can remain useful, but their loader contract changed.

- Use IE's `immersiveengineering:basic_split` around a Forge `forge:obj` inner
  model for formed blocks.
- Use a separate, centered unsplit OBJ for the inventory item.
- Convert texture references from legacy `blocks/` paths to modern `block/`
  paths and ensure every MTL material resolves.
- Rotate or re-export geometry so its axes match the canonical template.
- Ensure raw OBJ bounds cover the same master-relative cells listed by
  `split_parts`.
- Do not add a block-entity renderer merely because the old mod had a TESR. IE's
  split model loader is the appropriate static rendering path.

Dense formed structures can become almost black when ambient occlusion samples
full-cube collision shapes around inset OBJ faces. The exact vanilla model key
is `"ambientocclusion": false`, and for `basic_split` it belongs on the inner OBJ
model. This disables per-block AO while preserving ordinary directional quad
shading; emissive materials or artificial light would only mask the problem.

Item camera transforms are separate from world split rendering. A full-machine
item needs context-specific scaling and placement, while a malformed world
render requires checking block-entity model offsets, split keys, normals, and
AO. Do not rebase a correct world OBJ to fix a held-item transform.

## Engineer's Manual resources

Modern IE manual pages are resource-driven, but multiblock pages still require
client manual data from `initializeClient`. Supply a non-null
`MultiblockManualData`, accurate material stacks, and a deliberate formed-preview
policy before adding the JSON/text page. Adding only the resources can produce a
null lookup when the manual opens.

## Verification matrix

Use Java 17 for every command:

```text
gradlew.bat clean compileJava
gradlew.bat test
gradlew.bat clean build
gradlew.bat runServer
gradlew.bat runClient
```

Then verify:

- Dedicated server startup without JEI or other client classes resolving.
- Client startup with JEI present and absent.
- KubeJS startup, `/reload`, recipe replacement, and recipe removal.
- Formation and disassembly in all horizontal facings and supported mirrors.
- Save, chunk unload/reload, and full world restart.
- Every input/output capability position and side.
- Empty, incompatible, full, hard-locked, and type-locked tanks.
- Exact and insufficient input batches; no partial four-tank mutation.
- Multiple machines running simultaneously with independent state.
- Automatic output into direct handlers and real IE pipe networks.
- GUI actions, distance validation, progress sync, and JEI click areas.
- Missing-model, missing-texture, translation, loot-table, and tag warnings.
- Final JAR contents and an installed-JAR production launch, not only userdev.

## Intentional scope decisions

- The old typed CraftTweaker API used removed 1.12 classes. Datapacks and KubeJS
  are the supported recipe-authoring paths for this port.
- Decorative legacy shell/tube blocks were not part of exchanger operation and
  were not restored. The tube texture remains because formed models use it.
- The modern atomic transaction deliberately replaces the legacy partial
  cached-heat state machine.
- Test fluids and recipes remain an example KubeJS fixture rather than bundled
  production content.

The central lesson is that a successful port is a dependency-ordered rewrite,
not an import-renaming exercise: stabilize the build, register one complete
vertical slice, make state and capabilities correct, make processing atomic,
then add data loading, client synchronization, rendering, and compatibility.
