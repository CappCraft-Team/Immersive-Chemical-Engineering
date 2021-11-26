package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

public class MultiBlockHeatExchangerLarge implements MultiblockHandler.IMultiblock {
    public static final MultiblockHandler.IMultiblock INSTANCE = new MultiBlockHeatExchangerLarge();
    private static final ItemStack[][][] structure = new ItemStack[3][7][3];
    private static final IngredientStack[] materials = new IngredientStack[]{
            new IngredientStack(new ItemStack(IEContent.blockSheetmetal, 48, BlockTypes_MetalsAll.IRON.getMeta())),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 14, BlockTypes_MetalDecoration0.RADIATOR.getMeta())),
    };

    static {
        for (int h = 0; h < 3; h++)
            for (int l = 0; l < 7; l++)
                for (int w = 0; w < 3; w++) {
                    final boolean isRadiator = l == 0 || (w == 1 && l < 6 && h == 1);
                    structure[h][l][w] =
                            isRadiator ? new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta())
                                    : new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
                }
    }

    @Override
    public String getUniqueName() {
        return "ICHEME:HeatExchangerLarge";
    }

    @Override
    public boolean isBlockTrigger(IBlockState state) {
        return state.getBlock() == IEContent.blockMetalDecoration0
                && state.getBlock().getMetaFromState(state) == BlockTypes_MetalDecoration0.RADIATOR.getMeta();
    }

    @Override
    public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            side = EnumFacing.fromAngle(player.rotationYaw);
        else
            side = side.getOpposite();

        boolean mirrored = false;
        if (structureCheck(world, pos, side, false) && structureCheck(world, pos, side, mirrored = true)) return false;

        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER) ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
        if (MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
            return false;
        else {
            IBlockState state = ICHEME_Contents.blockHeatExchanger.getStateFromMeta(BlockTypes_HeatExchanger.HEAT_EXCHANGER_LARGE.getMeta());
            state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);

            pos = pos.down();//The Origin need to be at Y=0
            pos = pos.offset(side, 3);//Forward 3 block to make the master in the middle
            for (int h = 0; h < 3; h++)
                for (int l = -3; l <= 3; l++)
                    for (int w = -1; w <= 1; w++) {
                        final int offsetX = -side.getFrontOffsetZ() * w * (mirrored ? -1 : 1) + side.getFrontOffsetX() * l;
                        final int offsetZ = side.getFrontOffsetX() * w * (mirrored ? -1 : 1) + side.getFrontOffsetZ() * l;
                        final BlockPos blockPos = pos.add(offsetX, h, offsetZ);

                        world.setBlockState(blockPos, state);
                        TileEntity tileEntity = world.getTileEntity(blockPos);


                        if (tileEntity instanceof AbstractTileHeatExchanger) {
                            final AbstractTileHeatExchanger tileHeatExchanger = (AbstractTileHeatExchanger) tileEntity;
                            tileHeatExchanger.formed = true;
                            //See blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart.getBlockPosForPos
                            tileHeatExchanger.pos = h * 21 + (l + 3) * 3 + (w + 1);
                            tileHeatExchanger.offset = new int[]{offsetX, h, offsetZ};
                            tileHeatExchanger.mirrored = mirrored;
                            tileHeatExchanger.markDirty();
                            world.addBlockEvent(blockPos, ICHEME_Contents.blockHeatExchanger, 255, 0);
                        }
                    }
        }
        return true;
    }

    private boolean structureCheck(World world, BlockPos pos, EnumFacing side, boolean mirrored) {
        for (int h = -1; h <= 1; h++)
            for (int l = 0; l < 7; l++)
                for (int w = -1; w <= 1; w++) {
                    final boolean isRadiator = l == 0 || (w == 0 && l < 6 && h == 0);
                    final int offsetX = -side.getFrontOffsetZ() * w * (mirrored ? -1 : 1) + side.getFrontOffsetX() * l;
                    final int offsetZ = side.getFrontOffsetX() * w * (mirrored ? -1 : 1) + side.getFrontOffsetZ() * l;
                    final BlockPos blockPos = pos.add(offsetX, h, offsetZ);
                    if (isRadiator) {
                        if (!Utils.isBlockAt(world,
                                blockPos,
                                IEContent.blockMetalDecoration0,
                                BlockTypes_MetalDecoration0.RADIATOR.getMeta()))
                            return true;
                    } else if (!Utils.isBlockAt(world,
                            blockPos,
                            IEContent.blockSheetmetal,
                            BlockTypes_MetalsAll.IRON.getMeta()))
                        return true;
                }
        return false;
    }

    @Override
    public ItemStack[][][] getStructureManual() {
        return structure;
    }

    @Override
    public IngredientStack[] getTotalMaterials() {
        return materials;
    }

    @Override
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        return false;
    }

    static ItemStack renderStack = ItemStack.EMPTY;

    @Override
    public float getManualScale() {
        return 12f;
    }

    @Override
    public boolean canRenderFormedStructure() {
        return true;
    }

    @Override
    public void renderFormedStructure() {
        if (renderStack.isEmpty())
            renderStack = new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_LARGE.getMeta());

        GlStateManager.translate(3, 1, 1);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(4, 4, 4);

        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
