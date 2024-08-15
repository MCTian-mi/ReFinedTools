package mcjty.rftools.blocks.storagemonitor;


import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketReturnInventoryInfo implements IMessage {

    private List<InventoryInfo> inventories;

    public List<InventoryInfo> getInventories() {
        return inventories;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            String name = NetworkTools.readString(buf);
            boolean routable = buf.readBoolean();
            ItemStack stack = ItemStack.EMPTY;
            if (buf.readBoolean()) {
                stack = NetworkTools.readItemStack(buf);
            }
            var info = new PacketReturnInventoryInfo.InventoryInfo(pos, name, routable, stack);
            inventories.add(info);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(inventories.size());
        for (PacketReturnInventoryInfo.InventoryInfo info : inventories) {
            NetworkTools.writePos(buf, info.getPos());
            NetworkTools.writeString(buf, info.getName());
            buf.writeBoolean(info.isRoutable());
            ItemStack stack = info.getStack();
            if (stack.isEmpty()) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            }
        }
    }

    public PacketReturnInventoryInfo() {
    }

    public PacketReturnInventoryInfo(ByteBuf buf) {
    }

    public PacketReturnInventoryInfo(List<InventoryInfo> inventories) {
        this.inventories = inventories;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ReturnInfoHelper.onMessageFromServer(this);
        });
        ctx.setPacketHandled(true);
    }

    public static class InventoryInfo {
        private final BlockPos pos;
        private final String name;
        private final boolean routable;
        private final ItemStack stack;

        public InventoryInfo(BlockPos pos, String name, boolean routable, ItemStack stack) {
            this.pos = pos;
            this.name = name;
            this.routable = routable;
            this.stack = stack;
        }

        public BlockPos getPos() {
            return pos;
        }

        public String getName() {
            return name;
        }

        public boolean isRoutable() {
            return routable;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}