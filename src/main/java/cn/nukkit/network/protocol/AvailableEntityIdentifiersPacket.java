package cn.nukkit.network.protocol;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.common.io.ByteStreams;
import lombok.ToString;

import java.io.IOException;
import java.nio.ByteOrder;

@ToString()
public class AvailableEntityIdentifiersPacket extends DataPacket {
    public static final byte NETWORK_ID = ProtocolInfo.AVAILABLE_ENTITY_IDENTIFIERS_PACKET;

    private static CompoundTag entityIdentifiers;

    static {
        try {
            byte[] data = ByteStreams.toByteArray(Nukkit.class.getClassLoader().getResourceAsStream("entity_identifiers.dat"));
            entityIdentifiers = NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load entity_identifiers_net.dat");
        }
    }

    public static void registerIdentifier(int id, String name) {
        ListTag<CompoundTag> list = entityIdentifiers.getList("idlist", CompoundTag.class);
        CompoundTag data = new CompoundTag();
        data.putBoolean("hasspawnegg", true);
        data.putBoolean("experimental", false);
        data.putBoolean("summonable", true);
        data.putString("id", name);
        data.putString("bid", ":");
        data.putInt("rid", id);
        list.add(data);
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        try {
            entityIdentifiers = NBTIO.read(this.get(), ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            Server.getInstance().getLogger().error("", e);
        }
    }

    @Override
    public void encode() {
        this.reset();
        try {
            this.put(NBTIO.write(entityIdentifiers, ByteOrder.LITTLE_ENDIAN, true));
        } catch (IOException e) {
            Server.getInstance().getLogger().error("", e);
        }
    }
}
