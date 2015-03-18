package gopheratl.biolock.common;

import gopheratl.biolock.common.RedstoneProgram;
import gopheratl.biolock.common.network.PacketHandler;
import gopheratl.biolock.common.network.PacketKeypadButton;
import gopheratl.biolock.common.util.BLLog;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;


public class TileEntityKeypadLock extends TileEntityProgrammable {

	
	static String[] keyChars=new String[] {"1","2","3","4","5","6","7","8","9","*","0","#"};
	static int maxCodeLength=8;
	static Set<String> validChars = new HashSet<String>();
	static {
		 validChars.add("0");
		 validChars.add("1");
		 validChars.add("2");
		 validChars.add("3");
		 validChars.add("4");
		 validChars.add("5");
		 validChars.add("6");
		 validChars.add("7");
		 validChars.add("8");
		 validChars.add("9");
		 validChars.add("#");
		 validChars.add("*");
		 
	}

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
	
	public static class LockCode {
		String name;
		String code;
		byte accessLevel;
		
		public LockCode(String name, String code, byte accessLevel)
		{
			this.name=name;
			this.code=code;
			this.accessLevel=accessLevel;
		}

		public boolean matchesBuffer(String pressedBuffer) 
		{
			int skip=pressedBuffer.length()-code.length();
			if (skip>=0) 
			{
				String relevant=pressedBuffer.substring(skip,pressedBuffer.length());
				return relevant.equals(code);
			}
			return false;
		}
		
	}
	public ButtonState buttonStates[];
	
	public RedstoneProgram[] redstonePrograms;
	Map<String,LockCode> storedCodes;
	
	// true if the blocks redstone output state has changed
	boolean outputChanged;
	//true if one or more activation programs are running
	boolean programsActive;

	String pressedBuffer;

	public TileEntityKeypadLock()
	{		
		super();
		buttonStates=new ButtonState[] { 
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
			new ButtonState(), new ButtonState(), new ButtonState(),
		};
		
		redstonePrograms=new RedstoneProgram[] {
				null,
				null,
				null,
				null,
				null,
				null,
		};
		
		storedCodes=Collections.synchronizedMap(new HashMap<String,LockCode>());

		addLockingMethod(new MEForgetCode(),"forgetCode");
		addLockingMethod(new MELearnCode(),"learnCode");
		addLockingMethod(new MEProgram(), "program");
		addLockingMethod(new MEForgetProgram(),"forgetProgram");
		
		programsActive=false;
		outputChanged=false;
		pressedBuffer="";
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
		//System.out.println("[BioLock] [DEBUG] readInstanceFromNBT");
		NBTTagList codes=nbt.getTagList("codes", 8);
		NBTTagList names=nbt.getTagList("names", 8);
		byte[] accessLevels=nbt.getByteArray("lvls");
		if (codes.tagCount()!=names.tagCount() || codes.tagCount()!=accessLevels.length)
		{
		   System.out.println("[BioLock] Mismatch in lengths of print, name, and level arrays; not reading NBT data!");
		   return;
		}
		try {
			for (int i=0; i<accessLevels.length; i++)
			{
				String code=codes.getStringTagAt(i);
				String name=names.getStringTagAt(i);
				storedCodes.put(name,new LockCode(name,code,accessLevels[i]));
			}
		}
		catch (Exception e)
		{
			System.out.println("[BioLock] Exception in KeypadLock.readFromNBT: "+e.getLocalizedMessage());
			e.printStackTrace();
			storedCodes=Collections.synchronizedMap(new HashMap<String,LockCode>());
		}
		//now read the programs
		NBTTagList programs=nbt.getTagList("redstonePrograms", 10);
		if (programs!=null && programs.tagCount()==6)
			for (int i=0; i<6; i++)
			{
				NBTTagCompound prog=programs.getCompoundTagAt(i);
				if (prog.hasNoTags())
					redstonePrograms[i]=null;
				else
					redstonePrograms[i]=new RedstoneProgram(prog);
			}		
	}

	@Override
	public void writeInstanceToNBT(NBTTagCompound nbt) {
		//System.out.println("[BioLock] [DEBUG] writeInstanceToNBT");
		NBTTagList names=new NBTTagList();
		NBTTagList codes=new NBTTagList();
		if (storedCodes==null)
		{
			System.out.println("[BioLock] Wtf?! Writing my NBT before I've ever read it?");
			return;
		}
		byte[] accessLevels=new byte[storedCodes.size()];
		Iterator<LockCode> iter=storedCodes.values().iterator();
		int i=0;
		while (iter.hasNext())
		{
		    LockCode cur=iter.next();
		    names.appendTag(new NBTTagString(cur.name));
			codes.appendTag(new NBTTagString(cur.code));
			accessLevels[i++]=cur.accessLevel;
		}
		nbt.setTag("codes", codes);
		nbt.setTag("names", names);
		nbt.setByteArray("lvls", accessLevels);
		
		//now write the programs
		NBTTagList programs=new NBTTagList();
		for (i=0; i<6; ++i)
		{
			if (redstonePrograms[i]==null )
				programs.appendTag(new NBTTagCompound());
			else
				programs.appendTag(redstonePrograms[i].getNBT());
		}
		nbt.setTag("redstonePrograms", programs);
	}	
	
	
	
	@Override
	public void updateEntity() 
	{
		super.updateEntity();		

		if (programsActive)
		{
			programsActive=false;
			for (int i=0; i<6; ++i)
			{
				if (redstonePrograms[i]!=null && redstonePrograms[i].active)
				{
					redstonePrograms[i].tick();
					//if it's still active, not done updating
					if (redstonePrograms[i].active)
						programsActive=true;
					else
						//no longer active, block update
						outputChanged=true;
				}
			}
		}
				
		if (outputChanged)
		{
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, BioLock.Blocks.keypadLock);
			outputChanged=false;
		}
				
	}
	
	/**
	 * returns whether the block is currently outputing redstone on the specified side
	 * 
	 * @param side the side - in WORLD, not LOCAL orientations
	 * @return true if block is outputing redstone on this side; otherwise false
	 */
	@Override
	public boolean isPowering(int side)
	{		
		//convert side to side relative to my facing direction
		int relSide=getRelativeSide(side);
		//System.out.println("[BioLock] [DEBUG] TileEntityProgrammable:isPowering("+side+") => relSide "+relSide);//+"="+redstonePrograms[relSide].getOutput());
		
		return redstonePrograms[relSide]==null ? false : redstonePrograms[relSide].getOutput();
	}

	/**
	 * returns the output level on a given side as an int, 0-15
	 * 
	 * @param side the side in WORLD, not local, orientations
	 * @return
	 */
	public int getSideOutput(int side)
	{
		return isPowering(side)?15:0;
	}
	
	//****************
	//  Peripheral method implementations
	
	private class MELearnCode implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{			
			if ( args.length<3 || 
			   		 ! (args[0] instanceof String) ||
			   		 ! (args[1] instanceof String) ||
			   		 ! (args[2] instanceof Double) )
			{
		   		return new Object[] {false, "Invalid arguments: Expected String, String, Number, got "+args[0].getClass().getName()+","+(args.length>1?(args[1].getClass().getName()+","+(args.length>2?args[2].getClass().getName():"nil")):"nil, nil")};
			}
			
			String name=(String)args[0];
			String code=(String)args[1];	

			if (!storedCodes.containsKey(name) && storedCodes.size()>=BioLock.Config.internalMemorySize)
				return new Object[] {false, "Internal Memory Full"};				
			
			if (code.length()>maxCodeLength || code.length()<1)
				return new Object[] {false, "Codes must be between 1 and "+maxCodeLength+" symbols long."};
			
			for (int i=0; i<code.length(); ++i)
			{
				if(!validChars.contains(code.substring(i,i+1)))
					return new Object[] {false, "illegal symbol in code at index "+i+"("+code.substring(i,i+1)+")"};
			}
			
			int accessLevel=((Double)args[2]).intValue();
			if (accessLevel<1 || accessLevel>5)
			  	return new Object[] {false, "Access Level must be in range 1-5"};
			
			
			storedCodes.put(name, new LockCode(name,code,(byte)accessLevel));
			
			stateChanged=true;
			return returnSuccess;
		}
		
	}

	private class MEForgetCode implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{			
			if ( args.length<1 || !(args[0] instanceof String) )
			{
		   		return new Object[] {false, "Invalid arguments"};
			}
			
			String name=(String)args[0];
			LockCode p=storedCodes.get(name);
			
			if (p!=null)
			{
				storedCodes.remove(p.name);
				stateChanged=true;							
				return new Object[] { true }; 
			}
			
			return new Object[] { false, "Code not stored!" };
		}
		
	}

	private class MEProgram implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{			
			String strSide=((String)args[0]).toLowerCase();
			Integer n=sideMap.get(strSide);
			if(n==null)
				return new Object[] { false, "Invalid side" };
			
			Object result=RedstoneProgram.buildFromObjArr(args);
			if (result instanceof RedstoneProgram)
			{
				redstonePrograms[n]=(RedstoneProgram)result;
				outputChanged=true;
				stateChanged=true;
				return returnSuccess;
			}
			return (Object[]) result;
		}
	}

	private class MEForgetProgram implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{			
			if (args.length<1 || !(args[0] instanceof String))
				return new Object[] { false, "Invalid arguments, expected string" };
			
			String strSide=((String)args[0]).toLowerCase();
			Integer n=sideMap.get(strSide);
			if (n==null)
				return new Object[] { false, "Invalid side" };
			
			if (redstonePrograms[n]==null)
				return new Object[] { true, "Warning: No program to forget" };
			
			redstonePrograms[n]=null;
			outputChanged=true;
			stateChanged=true;
			return returnSuccess;
		}
		
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

	public void pressedButton(EntityPlayer player, int buttonIndex) 
	{
		if (!worldObj.isRemote)
		{
			BLLog.debug("pressButton on server...");
			PacketKeypadButton packet = new PacketKeypadButton((short)instanceID, worldObj.provider.dimensionId, xCoord, yCoord, zCoord, buttonIndex);		
			EntityPlayerMP p=(EntityPlayerMP)player;			
			PacketHandler.INSTANCE.sendToAllAround(packet, new NetworkRegistry.TargetPoint(p.dimension, (double)xCoord, (double)yCoord, (double)zCoord, 64d));
			
			queueForAttached("keypad_button",new Object[] { null, keyChars[buttonIndex]} );
			
			if (pressedBuffer.length()==maxCodeLength)
				pressedBuffer=pressedBuffer.substring(2, maxCodeLength);
			pressedBuffer = pressedBuffer+keyChars[buttonIndex];
			
			Iterator<LockCode> codes=storedCodes.values().iterator();
			LockCode lock=null;
			while (codes.hasNext())
			{
				lock=codes.next();
				if (lock.matchesBuffer(pressedBuffer))
				{
					queueForAttached("keypad_code",new Object[] {null, lock.name,lock.accessLevel});
					for (int i=0; i<6; ++i)
					{
						if (redstonePrograms[i]!=null && redstonePrograms[i].onActivation(lock.accessLevel))
						{
							programsActive=true;
							outputChanged=true;
						}
					}					
				}
			}
		}
	}
	
}
