package gopheratl.biolock.common.network;

import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.util.BLLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerBiolockScan implements IMessageHandler<PacketBiolockScan, IMessage> {

	@Override
	public IMessage onMessage(PacketBiolockScan message, MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		short instanceID = message.id;
		int x = message.x;
		int y = message.y;
		int z = message.z;
		
		BLLog.log(Level.DEBUG, "TE packet at %d, %d, %d", x, y, z);
		
		World world=null;
		if (player instanceof EntityPlayerMP)		
		{
			for (World w : DimensionManager.getWorlds())
				if (w.playerEntities.contains(player))
				{
					world=w;
					break;
				}
		}
		else
			world=((EntityPlayerMP)player).worldObj;
		
		TileEntity te=world.getTileEntity(x, y, z);
		
		((TileEntityBioLock)te).doAnimate();
		return null;
	}

}
