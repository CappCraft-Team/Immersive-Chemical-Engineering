package team.cappcraft.immersivechemical.common.blocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.BlockRenderLayer;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;

import javax.annotation.Nonnull;

public class BlockStructure extends BlockIEBase<BlockTypes_Structure> {
    public BlockStructure() {
        super(
                "block_structure",
                Material.IRON,
                PropertyEnum.create("type", BlockTypes_Structure.class),
                ItemBlockIEBase.class
        );
        setHardness(3.0F);
        setResistance(15.0F);
        setCreativeTab(ImmersiveChemicalEngineering.creativeTab);
        setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT);
    }

    @Nonnull
    @Override
    public String createRegistryName() {
        return ImmersiveChemicalEngineering.MODID + ":" + name;
    }


}
