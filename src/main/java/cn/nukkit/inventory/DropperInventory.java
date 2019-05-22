package cn.nukkit.inventory;

import cn.nukkit.blockentity.BlockEntityDropper;

public class DropperInventory extends ContainerInventory {

    public DropperInventory(BlockEntityDropper holder) {
        super(holder, InventoryType.DROPPER);
    }

    @Override
    public BlockEntityDropper getHolder() {
        return (BlockEntityDropper) this.holder;
    }
}
