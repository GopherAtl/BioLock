package gopheratl.biolock.common.network;

import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.util.BLLog;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerProgrammable implements IMessageHandler<PacketProgrammable, IMessage> {

	@Override
	public IMessage onMessage(PacketProgrammable message, MessageContext ctx) {
		return null;
	}
	

}
