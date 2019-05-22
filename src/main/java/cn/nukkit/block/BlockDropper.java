package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityContainer;
import cn.nukkit.blockentity.BlockEntityDropper;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.utils.Faceable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockDropper extends BlockSolidMeta implements Faceable {

    public BlockDropper() {
        this(0);
    }

    public BlockDropper(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Dropper";
    }

    @Override
    public int getId() {
        return DROPPER;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        int f = 0;
        if (player instanceof Player) {
            f = player.getDirection().getHorizontalIndex();
            double pitch = player.getPitch();
            if (pitch < -45) {
                f = 4;
            } else if (pitch > 45) {
                f = 5;
            }
        }
        int[] faces = new int[]{2, 5, 3, 4, 0, 1};
        this.setDamage(faces[f]);

        this.getLevel().setBlock(block, this, true, true);

        CompoundTag nbt = BlockEntity.getDefaultCompound(this, BlockEntity.DROPPER);
        if (item.hasCustomName()) {
            nbt.putString("CustomName", item.getCustomName());
        }

        if (item.hasCustomBlockData()) {
            Map<String, Tag> customData = item.getCustomBlockData().getTags();
            for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                nbt.put(tag.getKey(), tag.getValue());
            }
        }

        new BlockEntityDropper(this.getLevel().getChunk(this.getChunkX(), this.getChunkZ()), nbt);
        return true;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (player == null) {
            return false;
        }

        BlockEntity entity = this.getLevel().getBlockEntity(this);
        BlockEntityDropper dropper;
        if (entity instanceof BlockEntityDropper) {
            dropper = (BlockEntityDropper) entity;
        } else {
            dropper = new BlockEntityDropper(this.getLevel().getChunk(this.getChunkX(), this.getChunkZ()), BlockEntity.getDefaultCompound(this, BlockEntity.DROPPER));
        }

        if (dropper.namedTag.contains("Lock") && dropper.namedTag.get("Lock") instanceof StringTag) {
            if (!dropper.namedTag.getString("Lock").equals(item.getCustomName())) {
                return true;
            }
        }

        player.addWindow(dropper.getInventory());
        return true;
    }

    @Override
    public int onUpdate(int type) {
        Level level = this.getLevel();
        int damage = this.getDamage();
        if (level.isBlockPowered(this) && damage < 8) {
            this.setDamage(damage + 8);
            level.setBlock(this, this);
            this.dropItem();
            return 0;
        }

        if (!level.isBlockPowered(this) && damage >= 8) {
            this.setDamage(damage - 8);
            level.setBlock(this, this);
            return 0;
        }
        return 0;
    }

    protected void dropItem() {
        BlockEntity entity = this.getLevel().getBlockEntity(this);
        if (!(entity instanceof BlockEntityDropper)) {
            return;
        }

        BlockEntityDropper dropper = (BlockEntityDropper) entity;
        Inventory inventory = dropper.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            Item item = inventory.getItem(i);
            if (item.getId() == Item.AIR) {
                continue;
            }

            slots.add(i);
        }

        if (slots.size() == 0) {
            return;
        }

        int slot = slots.get(new Random().nextInt(slots.size()));
        Item item = inventory.getItem(slot);
        Item drop = item.clone();
        drop.setCount(1);

        item.setCount(item.getCount() - 1);
        if (item.getCount() == 0) {
            item = Item.get(Item.AIR);
        }
        inventory.setItem(slot, item);


        BlockFace face = this.getBlockFace();
        Block side = this.getSide(face);
        entity = this.getLevel().getBlockEntity(side);
        if (entity instanceof BlockEntityContainer && entity instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) entity).getInventory();
            if (!inv.canAddItem(drop)) {
                return;
            }

            inv.addItem(drop);

            item.setCount(item.getCount() - 1);
            if (item.getCount() == 0) {
                item = Item.get(Item.AIR);
            }
            inventory.setItem(slot, item);
            return;
        }

        item.setCount(item.getCount() - 1);
        if (item.getCount() == 0) {
            item = Item.get(Item.AIR);
        }
        inventory.setItem(slot, item);

        Vector3 motion = new Vector3().getSide(face);
        Vector3 pos = new Vector3(this.x + motion.x * 2 + 0.5, this.y + (motion.y < 0 ? motion.y : 0.5), this.z + motion.z * 2 + 0.5);
        this.getLevel().dropItem(pos, drop, motion.multiply(0.3));
        for (int i = 0; i < 10; i++) {
            this.getLevel().addParticle(new SmokeParticle(this.add(motion.x * i * 0.3 + 0.5, motion.y == 0 ? 0.5 : motion.y * i * 0.3, motion.z * i * 0.3 + 0.5)));
        }

        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_SOUND_CLICK;
        pk.data = 1000;
        pk.x = this.getFloorX() + 0.5f;
        pk.y = this.getFloorY() + 0.5f;
        pk.z = this.getFloorZ() + 0.5f;
        this.getLevel().addChunkPacket(this.getChunkX(), this.getChunkZ(), pk);
    }

    @Override
    public double getHardness() {
        return 3.5;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromIndex(this.getDamage() & 0x07);
    }
}
