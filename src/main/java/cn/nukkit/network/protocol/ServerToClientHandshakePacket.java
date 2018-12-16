package cn.nukkit.network.protocol;

public class ServerToClientHandshakePacket extends DataPacket {

    public String token;

    @Override
    public byte pid() {
        return ProtocolInfo.SERVER_TO_CLIENT_HANDSHAKE_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();

        this.putString(token);
    }
}
