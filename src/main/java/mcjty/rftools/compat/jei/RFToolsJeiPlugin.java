package mcjty.rftools.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.network.RFToolsMessages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class RFToolsJeiPlugin implements IModPlugin {

    private static ItemStack getPreferredItem(List<ItemStack> stacks) {

        if (GeneralConfiguration.modPriority.isEmpty()) {
            return stacks.get(0);
        }

        ItemStack preferred = ItemStack.EMPTY;
        int bestPriority = -1;

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            ResourceLocation id = stack.getItem().getRegistryName();
            if (id == null) continue;
            String namespace = id.getNamespace();
            int prio = GeneralConfiguration.modPriority.getOrDefault(namespace, 0);
            if (prio > bestPriority) {
                preferred = stack;
                bestPriority = prio;
            }
        }
        return preferred;
    }

    public static void transferRecipe(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        ItemStackList items = ItemStackList.create(10);
        guiIngredients.forEach((recipeSlot, guiIngredient) -> {
            List<ItemStack> allIngredients = guiIngredient.getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, getPreferredItem(allIngredients));
            }
        });

        RFToolsMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
        if (CrafterConfiguration.enabled.get()) {
            CrafterRecipeTransferHandler.register(transferRegistry);
        }
        ModularStorageRecipeTransferHandler.register(transferRegistry);
        ModularStorageItemRecipeTransferHandler.register(transferRegistry);
        RemoteStorageItemRecipeTransferHandler.register(transferRegistry);
        StorageScannerRecipeTransferHandler.register(transferRegistry);
    }
}
