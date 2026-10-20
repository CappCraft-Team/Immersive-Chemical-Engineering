# KubeJS heat-exchanger test fixture

Copy both folders in this directory into the instance's `kubejs` folder, then
restart Minecraft. Fluid registration runs only during startup; `/reload` is
enough for later edits to the two recipe rules.

For a Forge development workspace, copy the two script folders to `run/kubejs`
and launch with:

```text
gradlew.bat runClient -PenableKubeJsRun=true
```

Give yourself the two input buckets:

```text
/give @s kubejs:immcheme_test_superheated_bucket
/give @s kubejs:immcheme_test_chilled_bucket
```

Put 1000 mB of each input into opposite exchanger inputs. The two rules are
energy-balanced as one atomic transaction: the hot rule releases 400 heat,
while two 500 mB cold-rule batches consume 400 heat. The outputs should be
exactly 1000 mB each of `kubejs:immcheme_test_warm` and
`kubejs:immcheme_test_tempered`, with no partial transfer.
