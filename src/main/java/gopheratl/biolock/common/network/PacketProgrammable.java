package gopheratl.biolock.common.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

public class PacketProgrammable extends PacketGeneric {
	public short instance;
	public int dimension;
	public int x, y, z;
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeShort(instance);
		buf.writeInt(dimension);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		instance = buf.readShort();
		dimension = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}


}
