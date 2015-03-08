package gopheratl.biolock.common.network;

import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.util.BLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
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
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		short instanceID = message.id;
		int x = message.x;
		int y = message.y;
		int z = message.z;
		
		BLLog.debug("TE packet at %d, %d, %d", x, y, z);
		
		World world=null;
		if (player instanceof EntityClientPlayerMP)		
		{
			for (World w : DimensionManager.getWorlds())
				if (w.playerEntities.contains(player))
				{
					world=w;
					break;
				}
		}
		else
			world=((EntityClientPlayerMP)player).worldObj;
		
		TileEntity te=world.getTileEntity(x, y, z);
		
		int frame = message.frame;
		((TileEntityBioLock)te).doAnimate(frame);
		return null;
	}

}
