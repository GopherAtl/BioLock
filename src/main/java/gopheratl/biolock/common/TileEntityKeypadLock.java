package gopheratl.biolock.common;

import gopheratl.biolock.common.network.PacketHandler;
import gopheratl.biolock.common.network.PacketKeypadButton;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		readFromNBT(packet.func_148857_g());
	}

	public void pressedButton(EntityPlayer player, int i) 
	{
		if (!worldObj.isRemote)
		{
			System.out.println("[BioLock] [DEBUG] pressButton on server...");
			PacketKeypadButton packet = new PacketKeypadButton((short)instanceID, worldObj.provider.dimensionId, xCoord, yCoord, zCoord, i);		
			EntityPlayerMP p=(EntityPlayerMP)player;			
			PacketHandler.INSTANCE.sendToAllAround(packet, new NetworkRegistry.TargetPoint(p.dimension, (double)xCoord, (double)yCoord, (double)zCoord, 64d));
		}
	}
	
}
