package team.cappcraft.immersivechemical.client.gui;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMenu;
import team.cappcraft.immersivechemical.common.network.HeatExchangerMenuAction;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

@Mod.EventBusSubscriber(
        modid = ImmersiveChemicalEngineering.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public class HeatExchangerScreen extends AbstractContainerScreen<HeatExchangerMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    ImmersiveChemicalEngineering.MODID, "textures/gui/heatexchanger.png");

    private static final int GUI_WIDTH = 175;
    private static final int GUI_HEIGHT = 207;
    private static final int TEXTURE_SIZE = 256;
    private static final int TANK_SIZE = 16;
    private static final int ICON_U = 177;
    private static final int RESET_ICON_V = 0;
    private static final int RESET_ALL_ICON_V = 16;
    private static final int RUNNING_ICON_V = 32;
    private static final int IDLE_ICON_V = 77;
    private static final int STATUS_X = 81;
    private static final int STATUS_Y = 55;
    private static final int RESET_Y = 56;
    private static final int RESET_ALL_X = 80;
    private static final int RESET_ALL_Y = 79;
    private static final int FILTER_BUTTON_SIZE = 14;

    private static final int[] TANK_X = {35, 35, 125, 125};
    private static final int[] TANK_Y = {34, 79, 34, 79};
    private static final int[] FILTER_X = {54, 54, 107, 107};
    private static final int[] FILTER_Y = {35, 80, 35, 80};
    private static final int[] SIDE_RESET_X = {35, 125};

    private final Button[] sideResetButtons = new Button[2];
    private final Button[] filterButtons = new Button[HeatExchangerMenu.TANK_COUNT];
    private Button resetAllButton;

    public HeatExchangerScreen(HeatExchangerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        inventoryLabelY = 115;
        titleLabelY = 6;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                    for (HeatExchangerSizeVariants variant : HeatExchangerSizeVariants.values()) {
                        MenuScreens.register(
                                ICHEME_Contents.menuType(variant), HeatExchangerScreen::new);
                    }
                });
    }

    @Override
    protected void init() {
        super.init();
        for (int sideIndex = 0; sideIndex < sideResetButtons.length; ++sideIndex) {
            int side = sideIndex;
            sideResetButtons[side] =
                    addRenderableWidget(
                            new AtlasButton(
                                    leftPos + SIDE_RESET_X[side],
                                    topPos + RESET_Y,
                                    TANK_SIZE,
                                    TANK_SIZE,
                                    Component.translatable(
                                            "gui.immersivechemical.heat_exchanger_small.clear_side"),
                                    ignored ->
                                            sendAction(HeatExchangerMenuAction.CLEAR_SIDE, side)));
        }

        resetAllButton =
                addRenderableWidget(
                        new AtlasButton(
                                leftPos + RESET_ALL_X,
                                topPos + RESET_ALL_Y,
                                TANK_SIZE,
                                TANK_SIZE,
                                Component.translatable(
                                        "gui.immersivechemical.heat_exchanger_small.clear_all"),
                                ignored -> sendAction(HeatExchangerMenuAction.CLEAR_ALL, 0)));

        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            int index = tankIndex;
            filterButtons[index] =
                    addRenderableWidget(
                            Button.builder(
                                            Component.literal("F"),
                                            ignored ->
                                                    sendAction(
                                                            menu.isTankTypeLocked(index)
                                                                    ? HeatExchangerMenuAction
                                                                            .UNLOCK_FILTER
                                                                    : HeatExchangerMenuAction
                                                                            .LOCK_FILTER,
                                                            index))
                                    .bounds(
                                            leftPos + FILTER_X[index],
                                            topPos + FILTER_Y[index],
                                            FILTER_BUTTON_SIZE,
                                            FILTER_BUTTON_SIZE)
                                    .build());
        }
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        boolean canClearAny = false;
        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            FluidStack fluid = menu.getTankFluid(tankIndex);
            canClearAny |= menu.isTankTypeLocked(tankIndex) || !fluid.isEmpty();
        }

        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            FluidStack fluid = menu.getTankFluid(tankIndex);
            boolean typeLocked = menu.isTankTypeLocked(tankIndex);
            filterButtons[tankIndex].active = typeLocked || !fluid.isEmpty();
            filterButtons[tankIndex].setMessage(Component.literal(typeLocked ? "U" : "F"));
            canClearAny |= typeLocked || !fluid.isEmpty();
        }

        for (int sideIndex = 0; sideIndex < sideResetButtons.length; ++sideIndex) {
            int firstTank = sideIndex * 2;
            sideResetButtons[sideIndex].active = canClear(firstTank) || canClear(firstTank + 1);
        }
        resetAllButton.active = canClearAny;
    }

    private boolean canClear(int tankIndex) {
        return menu.isTankTypeLocked(tankIndex) || !menu.getTankFluid(tankIndex).isEmpty();
    }

    private void sendAction(HeatExchangerMenuAction action, int tankIndex) {
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, action.encodeButton(tankIndex));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(
                BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                GUI_WIDTH,
                GUI_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE);

        for (int sideIndex = 0; sideIndex < SIDE_RESET_X.length; ++sideIndex) {
            graphics.blit(
                    BACKGROUND,
                    leftPos + SIDE_RESET_X[sideIndex],
                    topPos + RESET_Y,
                    ICON_U,
                    RESET_ICON_V,
                    TANK_SIZE,
                    TANK_SIZE,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE);
        }
        graphics.blit(
                BACKGROUND,
                leftPos + RESET_ALL_X,
                topPos + RESET_ALL_Y,
                ICON_U,
                RESET_ALL_ICON_V,
                TANK_SIZE,
                TANK_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE);
        graphics.blit(
                BACKGROUND,
                leftPos + STATUS_X,
                topPos + STATUS_Y,
                ICON_U,
                menu.isProcessing() ? RUNNING_ICON_V : IDLE_ICON_V,
                TANK_SIZE,
                15,
                TEXTURE_SIZE,
                TEXTURE_SIZE);

        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            FluidStack fluid = menu.getTankFluid(tankIndex);
            if (!fluid.isEmpty()) {
                GuiHelper.drawRepeatedFluidSpriteGui(
                        graphics.bufferSource(),
                        graphics.pose(),
                        fluid,
                        leftPos + TANK_X[tankIndex],
                        topPos + TANK_Y[tankIndex],
                        TANK_SIZE,
                        TANK_SIZE);
            }
        }
        graphics.flush();

        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            if (menu.isTankTypeLocked(tankIndex)) {
                graphics.renderOutline(
                        leftPos + TANK_X[tankIndex] - 1,
                        topPos + TANK_Y[tankIndex] - 1,
                        TANK_SIZE + 2,
                        TANK_SIZE + 2,
                        0xFFFFB52E);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, titleLabelY, 0xFF3F3F3F);
        graphics.drawString(
                font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF3F3F3F, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        for (int tankIndex = 0; tankIndex < HeatExchangerMenu.TANK_COUNT; ++tankIndex) {
            if (isHovering(
                    TANK_X[tankIndex], TANK_Y[tankIndex], TANK_SIZE, TANK_SIZE, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, tankTooltip(tankIndex), mouseX, mouseY);
                return;
            }
            if (isHovering(
                    FILTER_X[tankIndex],
                    FILTER_Y[tankIndex],
                    FILTER_BUTTON_SIZE,
                    FILTER_BUTTON_SIZE,
                    mouseX,
                    mouseY)) {
                String key =
                        menu.isTankTypeLocked(tankIndex)
                                ? "gui.immersivechemical.heat_exchanger_small.unlock_filter"
                                : "gui.immersivechemical.heat_exchanger_small.lock_filter";
                graphics.renderTooltip(font, Component.translatable(key), mouseX, mouseY);
                return;
            }
        }

        for (int sideIndex = 0; sideIndex < SIDE_RESET_X.length; ++sideIndex) {
            if (isHovering(
                    SIDE_RESET_X[sideIndex], RESET_Y, TANK_SIZE, TANK_SIZE, mouseX, mouseY)) {
                graphics.renderTooltip(
                        font,
                        Component.translatable(
                                "gui.immersivechemical.heat_exchanger_small.clear_side"),
                        mouseX,
                        mouseY);
                return;
            }
        }

        if (isHovering(RESET_ALL_X, RESET_ALL_Y, TANK_SIZE, TANK_SIZE, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("gui.immersivechemical.heat_exchanger_small.clear_all"),
                    mouseX,
                    mouseY);
            return;
        }

        if (!ModList.get().isLoaded("jei")
                && isHovering(STATUS_X, STATUS_Y, TANK_SIZE, 15, mouseX, mouseY)) {
            Component status =
                    Component.translatable(
                            menu.isProcessing()
                                    ? "gui.immersivechemical.heat_exchanger_small.processing"
                                    : "gui.immersivechemical.heat_exchanger_small.idle");
            graphics.renderTooltip(font, status, mouseX, mouseY);
        }
    }

    private List<Component> tankTooltip(int tankIndex) {
        List<Component> tooltip = new ArrayList<>();
        FluidStack fluid = menu.getTankFluid(tankIndex);
        if (fluid.isEmpty()) {
            tooltip.add(
                    Component.translatable("gui.immersivechemical.heat_exchanger_small.empty")
                            .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(fluid.getDisplayName());
        }
        tooltip.add(
                Component.translatable(
                                "gui.immersivechemical.heat_exchanger_small.amount",
                                fluid.getAmount(),
                                menu.getTankCapacity(tankIndex))
                        .withStyle(ChatFormatting.GRAY));
        tooltip.add(
                Component.translatable(
                                menu.isTankTypeLocked(tankIndex)
                                        ? "gui.immersivechemical.heat_exchanger_small.filter_locked"
                                        : "gui.immersivechemical.heat_exchanger_small.filter_open")
                        .withStyle(
                                menu.isTankTypeLocked(tankIndex)
                                        ? ChatFormatting.GOLD
                                        : ChatFormatting.DARK_GRAY));
        return tooltip;
    }

    private static final class AtlasButton extends Button {
        private AtlasButton(
                int x, int y, int width, int height, Component narration, OnPress onPress) {
            super(x, y, width, height, narration, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(
                GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    }
}
