package gopheratl.biolock.common.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class PacketGeneric implements IMessage {
	public short id;
	
	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeShort(id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		id = buf.readShort();
	}
}
