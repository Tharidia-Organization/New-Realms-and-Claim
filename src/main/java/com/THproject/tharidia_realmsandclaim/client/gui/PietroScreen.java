package com.THproject.tharidia_realmsandclaim.client.gui;

import com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim;
import com.THproject.tharidia_realmsandclaim.block.entity.PietroBlockEntity;
import com.THproject.tharidia_realmsandclaim.client.ClientPacketHandler;
import com.THproject.tharidia_realmsandclaim.client.gui.medieval.MedievalGuiRenderer;
import com.THproject.tharidia_realmsandclaim.client.gui.medieval.MedievalButton;
import com.THproject.tharidia_realmsandclaim.client.gui.components.ImageTabButton;
import com.THproject.tharidia_realmsandclaim.client.gui.components.ImageProgressBar;
import com.THproject.tharidia_realmsandclaim.gui.PietroMenu;
import com.THproject.tharidia_realmsandclaim.gui.inventory.PlayerInventoryPanelLayout;
import com.THproject.tharidia_realmsandclaim.network.UpdateHierarchyPacket;
import com.THproject.tharidia_realmsandclaim.realm.HierarchyRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Medieval-styled realm management screen with PNG-based textures
 */
public class PietroScreen extends AbstractContainerScreen<PietroMenu> {
    // PNG Texture Resources
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/realm_background.png");
    private static final ResourceLocation BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/bar.png");
    private static final ResourceLocation EXPANSION_BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/expansion_button.png");
    private static final ResourceLocation EXPANSION_BUTTON_PRESSED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/expansion_button_pressed.png");
    private static final ResourceLocation CLAIMS_BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/claims_button.png");
    private static final ResourceLocation CLAIMS_BUTTON_PRESSED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/claims_button_pressed.png");
    private static final ResourceLocation DUNGEON_BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/dungeon_button.png");
    private static final ResourceLocation DUNGEON_BUTTON_PRESSED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/dungeon_button_pressed.png");
    private static final ResourceLocation ENTER_LARGE_BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/enter_large_button.png");
    private static final ResourceLocation SLOT_EMPTY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/slot_empty.png");
    private static final ResourceLocation SLOT_SELECTED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "textures/gui/slot_selected.png");

    // Enter large button texture dimensions
    private static final int ENTER_BTN_TEX_WIDTH = 650;
    private static final int ENTER_BTN_TEX_HEIGHT = 110;

    // Medieval font for GUI text
    private static final ResourceLocation MEDIEVAL_FONT =
            ResourceLocation.fromNamespaceAndPath(TharidiaRealmsAndClaim.MODID, "medieval");

    // Tab identifiers
    private static final int TAB_EXPANSION = 0;
    private static final int TAB_CLAIMS = 1;
    private static final int TAB_DUNGEON = 2;

    // GUI display dimensions (scaled from texture)
    private static final int PARCHMENT_WIDTH = 256;  // Display width
    private static final int PARCHMENT_HEIGHT = 384; // Display height (maintains aspect ratio)
    private static final int BORDER_WIDTH = 20;

    private int currentTab = TAB_EXPANSION;
    private ImageTabButton expansionTabButton;
    private ImageTabButton claimsTabButton;
    private ImageTabButton dungeonTabButton;
    private List<MedievalButton> hierarchyButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE_PLAYERS = 6;
    private UUID selectedPlayerForRankChange = null;
    private boolean showRankSelectionMenu = false;
    private int rankMenuX = 0;
    private int rankMenuY = 0;
    private ImageTabButton enterDungeonButton;
    private ImageProgressBar expansionProgressBar;

    // Claims tab - clickable rank entries
    private List<PlayerHierarchyEntry> currentHierarchyEntries = new ArrayList<>();
    private int claimsListStartY = 0;
    private int claimsListItemHeight = 20;
    private int rankTextX = 0;
    private int rankTextWidth = 60;

    public PietroScreen(PietroMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = PARCHMENT_WIDTH;
        this.imageHeight = PARCHMENT_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Tab button positions matching the white paper areas in realm_background.png
        int tabY = this.topPos + 102;
        int tabHeight = 26;
        int tabSpacing = 8;

        // Tab button widths
        int expWidth = 62;
        int claimsWidth = 70;
        int dungeonWidth = 62;
        int totalTabWidth = expWidth + claimsWidth + dungeonWidth + (tabSpacing * 2);

        // Center the tabs horizontally in the GUI
        int tabStartX = this.leftPos + (PARCHMENT_WIDTH - totalTabWidth) / 2;

        // Create PNG-based tab buttons with pressed textures
        // Expansion button (left tab)
        int expX = tabStartX;
        expansionTabButton = ImageTabButton.builder(EXPANSION_BUTTON_TEXTURE, EXPANSION_BUTTON_PRESSED_TEXTURE, 254, 99,
                button -> switchTab(TAB_EXPANSION))
                .bounds(expX, tabY, expWidth, tabHeight)
                .setActive(true)
                .build();

        // Claims button (middle tab)
        int claimsX = expX + expWidth + tabSpacing;
        claimsTabButton = ImageTabButton.builder(CLAIMS_BUTTON_TEXTURE, CLAIMS_BUTTON_PRESSED_TEXTURE, 268, 104,
                button -> switchTab(TAB_CLAIMS))
                .bounds(claimsX, tabY, claimsWidth, tabHeight)
                .build();

        // Dungeon button (right tab)
        int dungeonX = claimsX + claimsWidth + tabSpacing;
        dungeonTabButton = ImageTabButton.builder(DUNGEON_BUTTON_TEXTURE, DUNGEON_BUTTON_PRESSED_TEXTURE, 256, 102,
                button -> switchTab(TAB_DUNGEON))
                .bounds(dungeonX, tabY, dungeonWidth, tabHeight)
                .build();

        this.addRenderableWidget(expansionTabButton);
        this.addRenderableWidget(claimsTabButton);
        this.addRenderableWidget(dungeonTabButton);

        // Initialize expansion progress bar with PNG texture
        int barDisplayWidth = 190;
        int barDisplayHeight = 18;
        expansionProgressBar = new ImageProgressBar(
                BAR_TEXTURE,
                BORDER_WIDTH + 20,
                180,
                barDisplayWidth,
                barDisplayHeight);

        // Create Enter button using PNG texture (centered) - DISABLED for dungeon functionality
        int enterBtnWidth = 140;
        int enterBtnHeight = 25;
        enterDungeonButton = ImageTabButton.builder(ENTER_LARGE_BUTTON_TEXTURE, ENTER_BTN_TEX_WIDTH, ENTER_BTN_TEX_HEIGHT,
                button -> {
                    // Dungeon functionality disabled in this standalone version
                })
                .bounds(this.leftPos + (PARCHMENT_WIDTH - enterBtnWidth) / 2, this.topPos + PARCHMENT_HEIGHT - 120, enterBtnWidth, enterBtnHeight)
                .build();
        enterDungeonButton.visible = false;
        this.addRenderableWidget(enterDungeonButton);

        updateTabButtons();
    }

    private void updateTabButtons() {
        expansionTabButton.setActive(currentTab == TAB_EXPANSION);
        claimsTabButton.setActive(currentTab == TAB_CLAIMS);
        dungeonTabButton.setActive(currentTab == TAB_DUNGEON);

        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean onDungeonTab = (currentTab == TAB_DUNGEON);

        // Dungeon button - show only on dungeon tab but DISABLED (button functionality removed)
        if (enterDungeonButton != null) {
            enterDungeonButton.visible = onDungeonTab;
            // Button is visible but does nothing when clicked (dungeon not available)
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Render PNG background texture (scaled to fit display dimensions)
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // Render player inventory panel background (left side outside main GUI)
        int inventoryBgX = x + PlayerInventoryPanelLayout.PANEL_OFFSET_X;
        int inventoryBgY = y + PlayerInventoryPanelLayout.PANEL_OFFSET_Y;
        int panelWidth = PlayerInventoryPanelLayout.PANEL_WIDTH;
        int panelHeight = PlayerInventoryPanelLayout.PANEL_HEIGHT;

        // Render medieval-styled inventory panel background
        renderInventoryPanelBackground(guiGraphics, inventoryBgX, inventoryBgY, panelWidth, panelHeight);

        // Render slot backgrounds
        int slotStartX = x + PlayerInventoryPanelLayout.SLOT_OFFSET_X;
        int slotStartY = y + PlayerInventoryPanelLayout.SLOT_OFFSET_Y;
        renderInventorySlotBorders(guiGraphics, slotStartX, slotStartY);
    }

    /**
     * Renders a medieval-styled inventory panel background
     */
    private void renderInventoryPanelBackground(GuiGraphics gui, int x, int y, int width, int height) {
        // Main parchment background
        gui.fill(x, y, x + width, y + height, 0xFFD4C4A8);

        // Inner darker area for slots
        int innerPadding = 5;
        gui.fill(x + innerPadding, y + innerPadding,
                x + width - innerPadding, y + height - innerPadding, 0xFFC4B498);

        // Simple border
        int borderColor = 0xFF8B7355;
        gui.fill(x, y, x + width, y + 2, borderColor);
        gui.fill(x, y + height - 2, x + width, y + height, borderColor);
        gui.fill(x, y, x + 2, y + height, borderColor);
        gui.fill(x + width - 2, y, x + width, y + height, borderColor);

        // Corner accents
        int cornerSize = 4;
        int accentColor = 0xFF6B5344;
        gui.fill(x, y, x + cornerSize, y + cornerSize, accentColor);
        gui.fill(x + width - cornerSize, y, x + width, y + cornerSize, accentColor);
        gui.fill(x, y + height - cornerSize, x + cornerSize, y + height, accentColor);
        gui.fill(x + width - cornerSize, y + height - cornerSize, x + width, y + height, accentColor);
    }

    // Gap between main inventory and hotbar (in pixels)
    private static final int HOTBAR_GAP = 58;

    /**
     * Renders slot borders for the inventory panel
     */
    private void renderInventorySlotBorders(GuiGraphics gui, int slotStartX, int slotStartY) {
        // Main inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = slotStartX + col * 18;
                int slotY = slotStartY + row * 18;
                gui.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, MedievalGuiRenderer.BRONZE);
                gui.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF1A1A1A);
            }
        }
        // Hotbar (1 row, with small gap below main inventory)
        for (int col = 0; col < 9; col++) {
            int slotX = slotStartX + col * 18;
            int slotY = slotStartY + HOTBAR_GAP;
            gui.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, MedievalGuiRenderer.BRONZE);
            gui.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF1A1A1A);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Content area starts below the tab buttons
        int contentStartY = 140;

        // Render content based on current tab
        if (currentTab == TAB_EXPANSION) {
            renderExpansionTab(guiGraphics);
        } else if (currentTab == TAB_CLAIMS) {
            renderClaimsTab(guiGraphics);
        } else if (currentTab == TAB_DUNGEON) {
            renderDungeonTab(guiGraphics);
        }
    }

    // Dark text color for contrast on parchment
    private static final int TEXT_DARK = 0xFF1A1208;

    private void renderExpansionTab(GuiGraphics guiGraphics) {
        PietroBlockEntity pietroEntity = this.menu.getBlockEntity();
        if (pietroEntity != null) {
            int yPos = 160;
            int textX = BORDER_WIDTH + 15;

            // Lord information
            renderMedievalTextLine(guiGraphics,
                    Component.translatable("gui.tharidia_realmsandclaim.realm.lord_label").getString(), textX, yPos,
                    TEXT_DARK);
            yPos += 18;

            String owner = pietroEntity.getOwnerName();
            if (owner == null || owner.isEmpty()) {
                owner = Component.translatable("gui.tharidia_realmsandclaim.common.unknown").getString();
            }
            renderMedievalTextLine(guiGraphics, owner, textX + 15, yPos, TEXT_DARK);
            yPos += 40;

            // Realm size information
            int size = this.menu.getRealmSize();
            renderMedievalTextLine(guiGraphics,
                    Component.translatable("gui.tharidia_realmsandclaim.realm.size_label").getString(), textX, yPos,
                    TEXT_DARK);
            yPos += 18;
            renderMedievalTextLine(guiGraphics,
                    Component.translatable("gui.tharidia_realmsandclaim.realm.size_value", size).getString(), textX + 15, yPos,
                    TEXT_DARK);
            yPos += 35;

            // Expansion progress with progress bar
            if (size >= 15) {
                renderMedievalTextLine(guiGraphics,
                        Component.translatable("gui.tharidia_realmsandclaim.realm.max_level").getString(), textX, yPos,
                        TEXT_DARK);
            } else {
                renderMedievalTextLine(guiGraphics,
                        Component.translatable("gui.tharidia_realmsandclaim.realm.expansion_cost_label").getString(), textX,
                        yPos, TEXT_DARK);
                yPos += 22;

                int stored = this.menu.getStoredPotatoes();
                int required = pietroEntity.getPotatoCostForNextLevel();
                float progress = required > 0 ? (float) stored / required : 0f;

                // Update and render progress bar
                expansionProgressBar.position(BORDER_WIDTH + 15, yPos).setProgress(progress).showValueText(stored,
                        required, "");
                expansionProgressBar.render(guiGraphics);

                yPos += 22;
                int remaining = required - stored;
                renderMedievalTextLine(guiGraphics,
                        Component.translatable("gui.tharidia_realmsandclaim.realm.coins_needed", remaining).getString(), textX,
                        yPos, TEXT_DARK);
            }
        }
    }

    /**
     * Renders text with medieval font and dark color for authentic parchment appearance
     */
    private void renderMedievalTextLine(GuiGraphics gui, String text, int x, int y, int color) {
        Component styledText = Component.literal(text).withStyle(style -> style.withFont(MEDIEVAL_FONT));
        gui.drawString(Minecraft.getInstance().font, styledText, x, y, color, false);
    }

    private void renderClaimsTab(GuiGraphics guiGraphics) {
        int yPos = 160;
        int textX = BORDER_WIDTH + 15;

        // Title
        renderMedievalTextLine(guiGraphics, Component.translatable("gui.tharidia_realmsandclaim.realm.claims_title").getString(),
                textX, yPos, TEXT_DARK);
        yPos += 18;

        // Total coins
        int totalPotatoes = this.menu.getTotalClaimPotatoes();
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.treasury", totalPotatoes).getString(), textX, yPos,
                TEXT_DARK);
        yPos += 40;

        // Hierarchy section
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.hierarchy_title").getString(), textX, yPos,
                TEXT_DARK);
        yPos += 22;

        // Get hierarchy data from client cache
        Map<UUID, Integer> hierarchyData = ClientPacketHandler.getCachedHierarchyData();
        UUID ownerUUID = ClientPacketHandler.getCachedOwnerUUID();
        String ownerName = ClientPacketHandler.getCachedOwnerName();

        // Check if current player is the lord
        UUID currentPlayerUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID()
                : null;
        boolean isLord = currentPlayerUUID != null && currentPlayerUUID.equals(ownerUUID);

        // Build list of entries
        currentHierarchyEntries.clear();
        if (ownerUUID != null) {
            currentHierarchyEntries.add(new PlayerHierarchyEntry(ownerUUID, ownerName, HierarchyRank.LORD.getLevel()));
        }

        // Add other vassals from hierarchy data
        for (Map.Entry<UUID, Integer> entry : hierarchyData.entrySet()) {
            String playerName = getPlayerNameFromUUID(entry.getKey());
            currentHierarchyEntries.add(new PlayerHierarchyEntry(entry.getKey(), playerName, entry.getValue()));
        }

        // Sort by rank level (descending)
        currentHierarchyEntries.sort((a, b) -> Integer.compare(b.rankLevel, a.rankLevel));

        // Store list position for click detection
        claimsListStartY = yPos;
        rankTextX = textX + 120;

        // Get mouse position for hover detection
        int mouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                * Minecraft.getInstance().getWindow().getGuiScaledWidth()
                / Minecraft.getInstance().getWindow().getScreenWidth()) - this.leftPos;
        int mouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                * Minecraft.getInstance().getWindow().getGuiScaledHeight()
                / Minecraft.getInstance().getWindow().getScreenHeight()) - this.topPos;

        // Display vassal list with medieval styling
        int displayStartIndex = scrollOffset;
        int displayEndIndex = Math.min(scrollOffset + MAX_VISIBLE_PLAYERS, currentHierarchyEntries.size());

        for (int i = displayStartIndex; i < displayEndIndex; i++) {
            PlayerHierarchyEntry entry = currentHierarchyEntries.get(i);
            HierarchyRank rank = HierarchyRank.fromLevel(entry.rankLevel);

            // Alternate background for medieval list effect
            boolean even = (i % 2 == 0);
            MedievalGuiRenderer.renderListItem(
                    guiGraphics,
                    textX - 5,
                    yPos - 2,
                    this.imageWidth - BORDER_WIDTH * 2 - 20,
                    18,
                    "",
                    even,
                    false);

            // Player name with dark styling
            String displayName = entry.playerName.length() > 15 ? entry.playerName.substring(0, 15) : entry.playerName;
            renderMedievalTextLine(guiGraphics, displayName, textX + 5, yPos + 4, TEXT_DARK);

            // Rank title - make clickable for lord (except for themselves)
            String rankText = Component.translatable(getRankTranslationKey(rank)).getString();
            boolean canChangeRank = isLord && !entry.playerUUID.equals(ownerUUID);

            // Check if mouse is hovering over rank text
            boolean isHovered = canChangeRank &&
                    mouseX >= rankTextX && mouseX <= rankTextX + rankTextWidth &&
                    mouseY >= yPos && mouseY <= yPos + claimsListItemHeight;

            if (canChangeRank) {
                // Render clickable rank with hover effect
                if (isHovered) {
                    guiGraphics.fill(rankTextX - 2, yPos, rankTextX + rankTextWidth + 2, yPos + 16,
                            0x40000000);
                }
                renderMedievalTextLine(guiGraphics, rankText, rankTextX, yPos + 4,
                        isHovered ? MedievalGuiRenderer.GOLD_MAIN : MedievalGuiRenderer.BRONZE);
            } else {
                renderMedievalTextLine(guiGraphics, rankText, rankTextX, yPos + 4, TEXT_DARK);
            }

            yPos += claimsListItemHeight;
        }

        // Medieval scroll indicators
        if (scrollOffset > 0) {
            MedievalGuiRenderer.renderScrollIndicator(guiGraphics, this.imageWidth - BORDER_WIDTH - 25, 165,
                    true);
        }
        if (displayEndIndex < currentHierarchyEntries.size()) {
            MedievalGuiRenderer.renderScrollIndicator(guiGraphics, this.imageWidth - BORDER_WIDTH - 25,
                    claimsListStartY + (MAX_VISIBLE_PLAYERS * claimsListItemHeight) - 15, false);
        }

        // Render rank selection menu if open
        if (showRankSelectionMenu && selectedPlayerForRankChange != null) {
            renderRankSelectionMenu(guiGraphics);
        }
    }

    private void renderDungeonTab(GuiGraphics guiGraphics) {
        int yPos = 160;
        int textX = BORDER_WIDTH + 15;

        // Dungeon information
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.dungeon_enter_label").getString(), textX, yPos,
                TEXT_DARK);
        yPos += 20;

        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.dungeon_desc_1").getString(), textX, yPos,
                TEXT_DARK);
        yPos += 14;
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.dungeon_desc_2").getString(), textX, yPos,
                TEXT_DARK);
        yPos += 35;

        // Status information - show as NOT AVAILABLE in this standalone version
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.queue_status_label").getString(), textX, yPos,
                TEXT_DARK);
        yPos += 18;
        renderMedievalTextLine(guiGraphics,
                Component.translatable("gui.tharidia_realmsandclaim.realm.dungeon_not_available").getString(),
                textX + 15, yPos, 0xFF8B0000); // Dark red to indicate not available
    }

    @Override
    protected void rebuildWidgets() {
        super.rebuildWidgets();

        // Clear existing button
        if (enterDungeonButton != null) {
            this.removeWidget(enterDungeonButton);
            enterDungeonButton = null;
        }

        // Always create dungeon button (visibility controlled by updateButtonVisibility)
        int enterBtnWidth = 140;
        int enterBtnHeight = 25;
        enterDungeonButton = ImageTabButton.builder(ENTER_LARGE_BUTTON_TEXTURE, ENTER_BTN_TEX_WIDTH, ENTER_BTN_TEX_HEIGHT,
                button -> {
                    // Dungeon functionality disabled in this standalone version
                })
                .bounds(this.leftPos + (PARCHMENT_WIDTH - enterBtnWidth) / 2, this.topPos + PARCHMENT_HEIGHT - 120, enterBtnWidth, enterBtnHeight)
                .build();
        enterDungeonButton.visible = false;
        this.addRenderableWidget(enterDungeonButton);

        updateButtonVisibility();
    }

    private void renderRankSelectionMenu(GuiGraphics guiGraphics) {
        int menuWidth = 100;
        int menuHeight = 90;

        int menuX = rankMenuX;
        int menuY = rankMenuY;

        // Simple popup menu
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xFFE8DCC8);
        guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, 0xFF8B7355);
        guiGraphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, 0xFF8B7355);
        guiGraphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, 0xFF8B7355);
        guiGraphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF8B7355);

        // Rank options (except LORD)
        HierarchyRank[] selectableRanks = { HierarchyRank.CONSIGLIERE, HierarchyRank.GUARDIA, HierarchyRank.MILIZIANO,
                HierarchyRank.COLONO };
        int optionY = menuY + 8;

        // Get mouse position for hover detection
        int relMouseX = (int) (Minecraft.getInstance().mouseHandler.xpos()
                * Minecraft.getInstance().getWindow().getGuiScaledWidth()
                / Minecraft.getInstance().getWindow().getScreenWidth());
        int relMouseY = (int) (Minecraft.getInstance().mouseHandler.ypos()
                * Minecraft.getInstance().getWindow().getGuiScaledHeight()
                / Minecraft.getInstance().getWindow().getScreenHeight());

        for (HierarchyRank rank : selectableRanks) {
            boolean isHovered = relMouseX >= menuX && relMouseX <= menuX + menuWidth &&
                    relMouseY >= optionY && relMouseY <= optionY + 18;

            if (isHovered) {
                guiGraphics.fill(menuX + 2, optionY, menuX + menuWidth - 2, optionY + 18, 0x30000000);
            }

            // Center text in menu
            String rankText = Component.translatable(getRankTranslationKey(rank)).getString();
            int textWidth = Minecraft.getInstance().font.width(rankText);
            int textX = menuX + (menuWidth - textWidth) / 2;

            renderMedievalTextLine(guiGraphics, rankText, textX, optionY + 4,
                    isHovered ? MedievalGuiRenderer.GOLD_MAIN : TEXT_DARK);
            optionY += 20;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int relX = (int) mouseX - this.leftPos;
            int relY = (int) mouseY - this.topPos;

            // Handle rank selection menu clicks
            if (showRankSelectionMenu && selectedPlayerForRankChange != null) {
                int menuWidth = 100;
                int menuHeight = 90;

                if (relX >= rankMenuX && relX <= rankMenuX + menuWidth &&
                        relY >= rankMenuY && relY <= rankMenuY + menuHeight) {

                    HierarchyRank[] selectableRanks = {HierarchyRank.CONSIGLIERE, HierarchyRank.GUARDIA,
                            HierarchyRank.MILIZIANO, HierarchyRank.COLONO};
                    int optionY = rankMenuY + 8;

                    for (HierarchyRank rank : selectableRanks) {
                        if (relY >= optionY && relY <= optionY + 18) {
                            sendRankChangePacket(selectedPlayerForRankChange, rank);
                            showRankSelectionMenu = false;
                            selectedPlayerForRankChange = null;
                            return true;
                        }
                        optionY += 20;
                    }
                }

                showRankSelectionMenu = false;
                selectedPlayerForRankChange = null;
                return true;
            }

            // Handle claims tab rank text clicks
            if (currentTab == TAB_CLAIMS && !currentHierarchyEntries.isEmpty()) {
                UUID ownerUUID = ClientPacketHandler.getCachedOwnerUUID();
                UUID currentPlayerUUID = Minecraft.getInstance().player != null ?
                        Minecraft.getInstance().player.getUUID() : null;
                boolean isLord = currentPlayerUUID != null && currentPlayerUUID.equals(ownerUUID);

                if (isLord) {
                    int displayStartIndex = scrollOffset;
                    int displayEndIndex = Math.min(scrollOffset + MAX_VISIBLE_PLAYERS, currentHierarchyEntries.size());

                    for (int i = displayStartIndex; i < displayEndIndex; i++) {
                        int itemY = claimsListStartY + (i - scrollOffset) * claimsListItemHeight;

                        if (relX >= rankTextX && relX <= rankTextX + rankTextWidth &&
                                relY >= itemY && relY <= itemY + claimsListItemHeight) {

                            PlayerHierarchyEntry entry = currentHierarchyEntries.get(i);

                            if (!entry.playerUUID.equals(ownerUUID)) {
                                selectedPlayerForRankChange = entry.playerUUID;
                                showRankSelectionMenu = true;
                                rankMenuX = relX;
                                rankMenuY = relY;
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (currentTab == TAB_CLAIMS && !currentHierarchyEntries.isEmpty()) {
            int relX = (int) mouseX - this.leftPos;
            int relY = (int) mouseY - this.topPos;

            int listEndY = claimsListStartY + (MAX_VISIBLE_PLAYERS * claimsListItemHeight);
            if (relY >= claimsListStartY && relY <= listEndY) {
                int maxScroll = Math.max(0, currentHierarchyEntries.size() - MAX_VISIBLE_PLAYERS);

                if (scrollY > 0) {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                } else if (scrollY < 0) {
                    scrollOffset = Math.min(maxScroll, scrollOffset + 1);
                }
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void sendRankChangePacket(UUID playerUUID, HierarchyRank newRank) {
        if (this.menu.getBlockEntity() != null) {
            BlockPos pos = this.menu.getBlockEntity().getBlockPos();
            PacketDistributor.sendToServer(new UpdateHierarchyPacket(pos, playerUUID, newRank.getLevel()));
        }
    }

    private void switchTab(int tab) {
        currentTab = tab;
        scrollOffset = 0;
        updateTabButtons();
    }

    private String getPlayerNameFromUUID(UUID uuid) {
        // First check if it's the local player
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(uuid)) {
            return Minecraft.getInstance().player.getName().getString();
        }

        // Check cached player names from hierarchy sync
        String cachedName = ClientPacketHandler.getCachedPlayerName(uuid);
        if (cachedName != null && !cachedName.equals("Unknown")) {
            return cachedName;
        }

        // Fallback to truncated UUID
        return uuid.toString().substring(0, 8);
    }

    private static String getRankTranslationKey(HierarchyRank rank) {
        return "gui.tharidia_realmsandclaim.realm.rank." + rank.name().toLowerCase(Locale.ROOT);
    }

    private static class PlayerHierarchyEntry {
        final UUID playerUUID;
        final String playerName;
        final int rankLevel;

        PlayerHierarchyEntry(UUID uuid, String name, int level) {
            this.playerUUID = uuid;
            this.playerName = name != null ? name
                    : Component.translatable("gui.tharidia_realmsandclaim.common.unknown").getString();
            this.rankLevel = level;
        }
    }
}
