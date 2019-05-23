package cn.nukkit.blockentity;

import cn.nukkit.block.Block;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

public class BlockEntityBanner extends BlockEntitySpawnable {

    public BlockEntityBanner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean isBlockEntityValid() {
        return this.getBlock().getId() == Block.WALL_BANNER || this.getBlock().getId() == Block.STANDING_BANNER;
    }

    @Override
    public String getName() {
        return "Banner";
    }

    public int getBaseColor() {
        return this.namedTag.getInt("Base");
    }

    public void setBaseColor(int color) {
        this.namedTag.putInt("Base", color & 0x0f);
    }

    public ListTag<CompoundTag> getPatterns() {
        return (ListTag<CompoundTag>) this.namedTag.getList("Patterns", CompoundTag.class).copy();
    }

    public void setPatterns(ListTag<CompoundTag> patterns) {
        patterns.setName("Patterns");
        this.namedTag.putList(patterns);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString("id", BlockEntity.BANNER)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z)
                .putInt("Base", getBaseColor())
                .putList(this.getPatterns());
    }
}
