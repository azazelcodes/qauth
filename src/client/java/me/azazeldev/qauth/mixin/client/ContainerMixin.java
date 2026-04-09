package me.azazeldev.qauth.mixin.client;

import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import me.azazeldev.qauth.client.StateManager;
import me.azazeldev.qauth.client.gui.QuestTracker;
import me.azazeldev.qauth.client.gui.StashTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.contains;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerMixin<T extends AbstractContainerMenu> extends Screen {
    protected ContainerMixin(Component title) { super(title); }

    @Final
    @Shadow
    protected T menu;

    @Shadow public abstract void onClose();

    // TODO: maybe better management with state enum?
    @Unique
    String npc = "";
    @Unique
    int page = 0;

    @Unique
    List<Item> stashables = new ArrayList<>();

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V", at = @At("RETURN"))
    private void constructed(final T menu, final Inventory inventory, final Component title, CallbackInfo ci) {
        System.out.println("Constructor!");
        String t = title.getString().toLowerCase();

        StateManager.setState(switch (t) {
            case String s when s.contains("stash") -> StateManager.AuthState.STASH;
            case String s when s.contains("quests") -> StateManager.AuthState.QUEST;
            case String s when s.contains("profile") && s.contains(Minecraft.getInstance().player.getPlainTextName()) -> StateManager.AuthState.PROFILE;
            case String s when s.contains("shop") -> StateManager.AuthState.SHOP;

            case String s when s.contains("bitcoin") -> StateManager.AuthState.BITCOIN;
            case String s when s.contains("smithing") -> StateManager.AuthState.SMITHING;
            case String s when s.contains("med") -> StateManager.AuthState.MED;
            case String s when s.contains("stand") -> StateManager.AuthState.ARMOR_STAND;
            case String s when s.contains("weapon") -> StateManager.AuthState.ARMORY;

            default -> StateManager.getState();
        });
        if (StateManager.getState() == StateManager.AuthState.STASH) page = Integer.parseInt(t.split(" \\| page ")[1]) - 1;
        else {
            stashables = Config.stash.values().stream().map(ItemStack::getItem).toList();
            Config.write(Main.MOD_ID);
        } // FIXME: inefficient, on every container open repop stashables

        npc = "";
        if (StateManager.getState() == StateManager.AuthState.QUEST || StateManager.getState() == StateManager.AuthState.SHOP) {
            npc = t.split(" ")[0];
            MainClient.lastNPC = npc;
        }
        if (StateManager.getState() == StateManager.AuthState.PROFILE) QuestTracker.clear();
    }
    // FIXME: menu.getType crashes on player inventory => way to check if container
    @Inject(method = "init()V", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        System.out.println("Init!");
        //System.out.println(menu.getType().toString());
    }

    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V",
            shift = At.Shift.BEFORE
    ))
    public void beforeRenderItem(final GuiGraphics graphics, final Slot slot, final int mouseX, final int mouseY, CallbackInfo ci) {
        List<Component> disp = slot.getItem().getDisplayName().toFlatList();
        // stash tracker
        if (StateManager.getState() == StateManager.AuthState.STASH && slot.index < 5*9 && !contains(StashTracker.nostash, disp.get(1).getString())) {
            Config.stash.put(5*9*page+slot.index, slot.getItem()); // FIXME: inefficient, on every render repop stash + write it!! => ioob for regular list
            Config.write(Main.MOD_ID);
            return;
        }

        if (slot.getItem().isEmpty()) return;
        if (slot.index >= menu.slots.size()-9*4) return;

        // quest tracker
        //if (questS && slot.index > 9 && slot.index < 19 && contains(QuestTracker.questi, slot.getItem().getItem())/* && disp.get(2).getString().contains("(Click to ")*/ && !QuestTracker.questIndices.containsKey(npc)) QuestTracker.fetchQuest(npc, disp.get(1).getString().stripTrailing()); // FIXME: commented out check: replacement for item type check, not working atm because kkona messed up mechanic quest names
        // FIXME: as soon as I added this, katherine added /profile, which also has the players current quest! this can be removed as soon as /profile shows ALL accepted quests, see FIXME below

        // new quest tracker
        if (StateManager.getState() == StateManager.AuthState.PROFILE && slot.index < 3) {
            String qnpc = slot.getItem().getDisplayName().toFlatList().get(1).getString().toLowerCase();
            if (QuestTracker.questIndices.containsKey(qnpc)) return;
            List<Component> lore = slot.getItem().get(DataComponents.LORE).lines(); // FIXME: only last accepted quest, karthylynne plez fix -> once added, loop over all lines, if startsWith("Quest: ") add
            String quest = lore.get(lore.size()-2).toFlatList().get(1).getString();
            if (!quest.equals("Not Started")) QuestTracker.fetchQuest(qnpc, quest);
        }
        if (StateManager.getState() == StateManager.AuthState.PROFILE && slot.index == 4 && QuestTracker.shouldOpenQT) {
            Minecraft.getInstance().player.closeContainer();
            QuestTracker.showGUI(String.format("quests_fetched %s %s", MainClient.lastNPC, 0));
        }

        // barrel marking
        if (!Config.markBarrels) return;
        int col = 0x00FFFFFF;

        if (minecraft.player.getInventory().contains(slot.getItem())) col = 0xA800FF00;
        if (Config.valuables.contains(slot.getItem().getItem())) col = 0xA8FFFF00;
        if (stashables.contains(slot.getItem().getItem())) col = 0xA80000FF;
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, col);
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"))
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> ci) {
        if (MainClient.markValuable.matches(event)) {
            MainClient.markValuable.consumeClick();
            ItemStack hover = getHoveredSlot().getItem();
            if (!hover.isEmpty()) Config.toggleItem(Config.valuables, hover.getItem());
            ci.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At(value = "HEAD"), ordinal = 1)
    private int modifyDropped(int modify, Slot slot, int i, int j, ClickType clickType) {
        if (clickType.equals(ClickType.THROW) && Config.alwaysAllDrop) return 1;
        return modify;
    }

    @Inject(method = "onClose", at = @At("RETURN"))
    private void onClose(CallbackInfo ci) { // FIXME: there has to be a better way to do this
        if (StateManager.getState() != StateManager.AuthState.RAID) StateManager.setState(StateManager.AuthState.LOBBY);
    }


    // TODO: find a way to access this without abstraction?
    @Accessor("hoveredSlot")
    abstract Slot getHoveredSlot();
}
