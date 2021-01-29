package team.cappcraft

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import team.cappcraft.ImmersiveChemical.MOD_ID
import team.cappcraft.ImmersiveChemical.MOD_NAME
import team.cappcraft.ImmersiveChemical.VERSION

@Mod(
    modid = MOD_ID,
    name = MOD_NAME,
    version = VERSION,
    modLanguageAdapter = "net.toliner.korgelin.KotlinAdapter"
)
@Mod.EventBusSubscriber
object ImmersiveChemical {
    const val MOD_ID = "icheme"
    const val MOD_NAME = "Immersive Chemical Engineering"
    const val VERSION = "@version@"
    
    @Mod.EventHandler
    fun onPreInit(event: FMLPreInitializationEvent) {
    
    }
}