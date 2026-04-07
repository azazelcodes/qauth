package me.azazeldev.qauth.mixin.client;

import com.google.gson.JsonElement;
import me.azazeldev.qauth.Config;
import me.azazeldev.qauth.Main;
import me.azazeldev.qauth.client.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.contains;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerMixin<T extends AbstractContainerMenu> extends Screen {
    protected ContainerMixin(Component title) { super(title); }

    @Shadow
    protected T menu;

    @Shadow public abstract T getMenu();

    // TODO: maybe better management with state enum?
    @Unique
    boolean stashS = false;
    @Unique
    boolean questS = false;
    @Unique
    boolean shopS = false;
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


        stashS = t.contains("stash");
        if (stashS) page = Integer.parseInt(t.split(" \\| page ")[1])-1;
        else {
            stashables = Config.stash.values().stream().map(ItemStack::getItem).toList();
            Config.write(Main.MOD_ID);
        } // FIXME: inefficient, on every container open repop stashables

        questS = t.endsWith("quests"); // TODO: test
        shopS = t.contains("shop");
        npc = "";
        if (questS || shopS) npc = t.split(" ")[0];
        MainClient.questIndices.remove(npc);
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
        if (stashS && slot.index < 5*9 && !contains(MainClient.nostash, disp.get(1).getString())) {
            Config.stash.put(5*9*page+slot.index, slot.getItem()); // FIXME: inefficient, on every render repop stash + write it!! => ioob for regular list
            Config.write(Main.MOD_ID);
            return;
        }

        if (slot.getItem().isEmpty()) return;
        if (slot.index >= menu.slots.size()-9*4) return;

        // quest tracker
        // TODO: maybe move to own function
        if (questS && slot.index > 9 && slot.index < 19 && contains(MainClient.questi, slot.getItem().getItem())/* && disp.get(2).getString().contains("(Click to ")*/) { // FIXME: commented out check: replacement for item type check, not working atm because kkona messed up mechanic quest names
            if (!MainClient.questIndices.containsKey(npc)) {
                MainClient.quests.remove(npc);
                if (Minecraft.getInstance().getCurrentServer() != null) MainClient.sendClient(Component.literal(Minecraft.getInstance().getCurrentServer().ip)); // FIXME: temp, base for unauth check

                MainClient.questIndices.put(npc, MainClient.fetchAPI("quests/"+npc+".index"));
                MainClient.sendClient(Component.literal("quest name: "+disp.get(1).getString()));
                MainClient.sendClient(Component.literal("size: "+MainClient.questIndices.get(npc).size()));
                MainClient.sendClient(Component.literal("inside: \""+MainClient.questIndices.get(npc).entrySet().iterator().next().getKey()+"\""));
                JsonElement index = MainClient.questIndices.get(npc).get(disp.get(1).getString());
                if (index != null) MainClient.quests.put(npc, MainClient.fetchAPI("quests/"+npc+"/"+index.toString()));
                else MainClient.sendClient(Component.literal("Your current quest was not found on the API!").withColor(0xFFFF0000));
            }
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


    // TODO: find a way to access this without abstraction?
    @Accessor("hoveredSlot")
    abstract Slot getHoveredSlot();
}
