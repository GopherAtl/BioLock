package gopheratl.biolock.common;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;


public class TileEntityKeypadLock extends TileEntityProgrammable {

	
	public static class ButtonState {
		public static int pressDelay=5;
		public long pressedTime;
		
		public ButtonState()
		{
			pressedTime=0;
		}
		
		public boolean isPressed(long time)
		{
			return time-pressedTime<pressDelay;			
		}
		
		public void press(long time)
		{
			pressedTime=time;			
		}		
	}
	
	public ButtonState buttonStates[];
	
	public TileEntityKeypadLock()
	{		
		super();
		buttonStates=new ButtonState[] { 
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
		};
		
	}

    public static String getBaseInstanceFileName()
    {
    	return "keypad";
    }

	@Override
	public String getType() {
		return "keypadlock";
	}

	@Override
	public void readInstanceFromNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeInstanceToNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	@SideOnly(Side.SERVER)
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) 
	{
		readFromNBT(packet.customParam1);
	}

	public void pressedButton(EntityPlayer player, int i) 
	{
		if (!worldObj.isRemote)
		{
			System.out.println("[BioLock] [DEBUG] pressButton on server...");
			Packet250CustomPayload packet=new Packet250CustomPayload();
			ByteArrayOutputStream outBytes=new ByteArrayOutputStream(17);
			DataOutputStream dataOut=new DataOutputStream(outBytes);
			try {
				dataOut.writeShort(1);
				dataOut.writeShort(instanceID);
				dataOut.writeInt(xCoord);
				dataOut.writeInt(yCoord);
				dataOut.writeInt(zCoord);
				dataOut.writeByte((byte)i);
				packet.data=outBytes.toByteArray();
				packet.length=outBytes.size();
				packet.channel="biolock";
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}			

			EntityPlayerMP p=(EntityPlayerMP)player;			
			PacketDispatcher.sendPacketToAllAround((double)xCoord, (double)yCoord, (double)zCoord, 64, p.dimension, packet);
		}
	}
	
}
