package gopheratl.biolock.common.network;

import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.util.BLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerBiolockScan implements IMessageHandler<PacketBiolockScan, IMessage> {

	@Override
	public IMessage onMessage(PacketBiolockScan message, MessageContext ctx) {
		short instanceID = message.id;
		int dim = message.dimension;
		int x = message.x;
		int y = message.y;
		int z = message.z;
		
		BLLog.debug("Biolock packet at %d, %d, %d in dim %d", x, y, z, dim);
		
		World world = BioLock.proxy.getWorld(dim);
		
		if (world != null) {
			TileEntity te=world.getTileEntity(x, y, z);
			
			int frame = message.frame;
			((TileEntityBioLock)te).doAnimate(frame);
		}
		return null;
	}

}
