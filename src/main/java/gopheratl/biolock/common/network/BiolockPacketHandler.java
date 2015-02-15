package gopheratl.biolock.common.network;

import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class BiolockPacketHandler implements IPacketHandler {

	public static abstract class BioPacket {
		short id;
		
		public abstract void handle(Player player, DataInputStream dataIn) throws IOException;
		
		public void WriteToStream(DataOutputStream dataOut) throws IOException
		{
			dataOut.writeShort(id);
		}
	}
	
	public abstract class BioPacketProgrammable extends BioPacket  {
		public void handle(Player player, DataInputStream dataIn) throws IOException
		{
			short instanceID;
			int x, y, z;

			try {
				instanceID=dataIn.readShort();
				x=dataIn.readInt();
				y=dataIn.readInt();
				z=dataIn.readInt();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println("[BioLock] [DEBUG] TE packet at "+x+","+y+","+z);

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
				world=((EntityPlayer)player).worldObj;

			
			TileEntity te=world.getBlockTileEntity(x, y, z);
			handle(player, te, dataIn);
		}
		
		public abstract void handle(Player p, TileEntity te, DataInputStream dataIn) throws IOException;
	}

	public class BioPacketKeypadButton extends BioPacketProgrammable {

		@Override
		public void handle(Player player, TileEntity te, DataInputStream dataIn) throws IOException 
		{
			int button=(int)dataIn.readByte();
			System.out.println("[BioLock] [DEBUG] got button for button # "+button);
			
			TileEntityKeypadLock tek=(TileEntityKeypadLock)te;
			tek.buttonStates[button].press(te.worldObj.getTotalWorldTime());
		}		
	}

	public class BioPacketBioLockScan extends BioPacketProgrammable {

		@Override
		public void handle(Player p, TileEntity te, DataInputStream dataIn)
				throws IOException 
		{
			((TileEntityBioLock)te).doAnimate();			
		}
				
	}
	
	public BioPacketKeypadButton keypadButton;
	public BioPacketBioLockScan bioLockScan;
	
	private Map<Integer,BioPacket> handlers;
	
	public BiolockPacketHandler()
	{
		super();
		handlers=new Hashtable<Integer,BioPacket>();
		
		keypadButton=new BioPacketKeypadButton();
		bioLockScan=new BioPacketBioLockScan();
		
		handlers.put(1,keypadButton);
		handlers.put(2,bioLockScan);
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{			
		DataInputStream dataIn=new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try {			
			int packetID = (int)dataIn.readShort();
			System.out.println("[BioLock] [DEBUG] got packet with id "+packetID+", handlers="+(handlers==null?"null":"valid")+", packet handler="+(handlers.get(packetID)==null?"null":"valid"));
			handlers.get(packetID).handle(player, dataIn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
		

}
