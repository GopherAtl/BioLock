package gopheratl.biolock.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.packet.Packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;


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
	
}
