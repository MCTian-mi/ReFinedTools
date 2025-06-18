package mcjty.rftools.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.blocks.crafter.CrafterConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerConfiguration;
import mcjty.rftools.network.RFToolsMessages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class RFToolsJeiPlugin implements IModPlugin {

    private static final Map<String, Integer> PRIORITY =
            parseToMap(StorageScannerConfiguration.MODLISTPRIORITY.get());

    private static Map<String, Integer> parseToMap(String input) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String[] parts = input.split(";");
        int n = parts.length;
        for (int i = 0; i < n; i++) {
            String s = parts[i].trim();
            if (!s.isEmpty()) {
                map.put(s, n - i);
            }
        }
        return map;
    }

    private static ItemStack getPreferredItem(List<ItemStack> stacks) {
        ItemStack preferred = ItemStack.EMPTY;
        int bestPriority = -1;

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) continue;
            String namespace = id.getNamespace();
            int prio = PRIORITY.getOrDefault(namespace, 0);
            if (prio > bestPriority) {
                preferred = stack;
                bestPriority = prio;
            }
        }
        return preferred;
    }

    public static void transferRecipe(
            Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        ItemStackList items = ItemStackList.create(10);
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry :
                guiIngredients.entrySet()) {
            int recipeSlot = entry.getKey();
            List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, getPreferredItem(allIngredients));
            }
        }

        RFToolsMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
        if (CrafterConfiguration.enabled.get()) CrafterRecipeTransferHandler.register(transferRegistry);
        ModularStorageRecipeTransferHandler.register(transferRegistry);
        ModularStorageItemRecipeTransferHandler.register(transferRegistry);
        RemoteStorageItemRecipeTransferHandler.register(transferRegistry);
        StorageScannerRecipeTransferHandler.register(transferRegistry);
    }
}
