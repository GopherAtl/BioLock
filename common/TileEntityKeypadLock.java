package gopheratl.biolock.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.packet.Packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;


public class TileEntityKeypadLock extends TileEntityProgrammable {

	public TileEntityKeypadLock()
	{
		super();
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
