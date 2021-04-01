# Immersive Chemical Engineering

A mod add chemical relevant items to Minecraft

# Build

Run `transformAccessTransformers` task in `access_transformer`
and **Refresh Gradle** to apply AccessTransformers in your runtime libraries

# Preview

Added 3 types of Heat Exchangers
![icon](https://github.com/CappCraft-Team/Immersive-Chemical-Engineering/blob/feature/first_block/Docs/showHeatExchanger.png?raw=true)
Gui
![icon](https://github.com/CappCraft-Team/Immersive-Chemical-Engineering/blob/feature/first_block/Docs/showGUI.png?raw=true)
JEI
![icon](https://github.com/CappCraft-Team/Immersive-Chemical-Engineering/blob/feature/first_block/Docs/showJEI.png?raw=true)

# Customization

## Config

```text
# Configuration file
heat_exchanger_capacity {
    # Min: 1
    # Max: 2147483647
    I:Large=30000

    # Min: 1
    # Max: 2147483647
    I:Medium=10000

    # Min: 1
    # Max: 2147483647
    I:Small=5000
}
heat_exchanger_tick_multiplier {
    # Min: 1.0E-7
    # Max: 1.0E7
    D:Large=0.2

    # Min: 1.0E-7
    # Max: 1.0E7
    D:Medium=0.4

    # Min: 1.0E-7
    # Max: 1.0E7
    D:Small=0.8
}
```

## CraftTweaker

Automatic transfer heat between fluids, base on their temperature

```zs
import team.cappcraft.icheme.HeatExchanger;
HeatExchanger.addCoolDownEntry(<liquid:water|5>*200, <liquid:water|0>*200, 1000);
HeatExchanger.addHeatUpEntry(<liquid:water|20>*200, <liquid:water|25>*200, 1000);
HeatExchanger.addBiDirectionEntry(<liquid:water|40>*200, <liquid:water|50>*200, 2000);
```

