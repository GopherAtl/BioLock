package gopheratl.biolock.common;

import gopheratl.GopherCore.GopherCore;
import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.common.RedstoneProgram;
import gopheratl.biolock.common.TileEntityBioLock.StoredPrint;
import gopheratl.biolock.common.util.BLLog;
import gopheratl.biolock.server.ProxyBioLockServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public abstract class TileEntityProgrammable extends TileEntity implements IPeripheral {

	//base for channel handlers that are associated with specific TileEntity instances
		
	
	//**********************
	//  static member data

	//some generic return tables for method calls, save on newing
	public static Object[] returnSuccess = new Object[] { true };
	
	//map of in-game side names to relative side numbers
	public static HashMap<String,Integer> sideMap;
		
	static {
		sideMap=new HashMap<String,Integer>();
		sideMap.put("bottom",0);
		sideMap.put("top",1);
		sideMap.put("front",2);
		sideMap.put("back",3);
		sideMap.put("right",4);
		sideMap.put("left",5);
	}
	
	public static float[] facingToAngle={0,0,0,180,90,270};
	
	public static String[] sideNames={"bottom","top","front","back","right","left"};

	
	private static HashMap<Pair<Integer,String>,Integer> computerPeripheralCounts=new HashMap<Pair<Integer,String>,Integer>();
	
	//**********************
	//  utility classes
	
	/**
	 * Interface for peripheral methods.
	 * @author Will
	 *
	 */
	protected interface IMethodExecutor {
		public abstract Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args ) throws Exception;
	}
	
	private static class MELock implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{
			if (args.length>0 && args[0] instanceof String)
			{
				if (!te.isLocked)
				{					
					te.isLocked=true;
					te.accessPassword=(String)args[0];
					return returnSuccess;
				}
				else
					return new Object[] {false,"Already Locked!"};
			}
			else
				return new Object[] {false, "Invalid args: String expected" };
		}
	}
	
	private static class MEUnlock implements IMethodExecutor {
		
		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{
			if (te.isLocked)
				if (args.length>0 && args[0] instanceof String)
					if (te.accessPassword.equals((String)args[0]))
					{
						te.accessPassword=null;
						te.isLocked=false;
						return returnSuccess;				
					}
					else
						return new Object[] {false,"Invalid password"};
				else
					return new Object[] {false, "Invalid args: String expected" };
			else
				return new Object[] {false, "Peripheral not locked!"};
		}
	}
	
	private static class MESetAccessLevel implements IMethodExecutor {
		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args)
		{
			if (args.length>=2 && args[0] instanceof Integer && args[1] instanceof Integer)
			{
				te.setAccessLevel((Integer)args[0], (Integer)args[1]);
				return returnSuccess;
			}
			else
				return new Object[] { false, "Invalid args: int, int expected" };
		}
	}

	private static class MEGetAccessLevel implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) 
		{
			if (args.length>=1 && args[0] instanceof Integer)
				return new Object[] { te.getAccessLevel((Integer)args[0]) };
			else if (args.length==0)
				return new Object[] { te.getAccessLevel(computer.getID())};
			else
				return new Object[] { false, "Invalid args: int or nil expected" };			
		}
	}
	
	private final class PeripheralMethod {
		public String name;
		public IMethodExecutor executor;
		public int accessLevel;
		public boolean locking;
		
		public PeripheralMethod(String name, IMethodExecutor executor, boolean locking, int accessLevel)
		{
			this.name=name;
			this.executor=executor;
			this.locking=locking;
			this.accessLevel=accessLevel;
		}
	}
	
	//**********************
	//  Instance members

	/**
	 * Is this peripheral locked, so access levels are checked to call peripheral methods?
	 */
	private boolean isLocked;
	private String accessPassword;	
	private HashMap<Integer,Integer> computerLevels;
	private String label;	
	
	//these are generated, and stored simply for efficiency 
	private ArrayList<PeripheralMethod> methods;
	private String[] methodNames;
	
	//map of attached computers by ID
	Map<Integer,IComputerAccess> attachedPuters;
	
	protected int instanceID;
	
	protected static String loadedWorldDir;
		

	// true if the internal instance state needs to be saved
	boolean stateChanged;

	//**********************
	//  Constructors
	
	public TileEntityProgrammable()
	{
		BLLog.debug("TileEntityProgrammable() (class:%s)", this.getClass().getSimpleName());
		BLLog.debug("Equality check: %b", BioLock.proxy instanceof ProxyBioLockServer);
		BLLog.debug("WorldObj null check: %b", (worldObj == null));
		if (worldObj != null) {
			BLLog.debug("WorldObj remote check: %b", (worldObj.isRemote));
		}
		BLLog.debug("Relauncher check: %b", FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER);
		//hacky but apparently successful way to detect server side, where !worldObj.isRemote won't work.
		if (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			BLLog.debug("Server...");
			//get the current world and check against the last we loaded
			String curWorldDir=GopherCore.getSaveSubDirPath("biolocks");
			if (loadedWorldDir==null || !curWorldDir.equals(loadedWorldDir))
			{
				BLLog.debug("new world, resetting instance managers...");
				loadedWorldDir=curWorldDir;
				//new world means new instance managers, kill old ones
				BioLock.instance.resetInstanceManagers();
			}
		}

		attachedPuters=Collections.synchronizedMap(new	HashMap());
		computerLevels=new HashMap();
		accessPassword="";
		
		methods=new ArrayList<PeripheralMethod>(4);
		
		addMethod(new MELock(),"lock");
		addMethod(new MEUnlock(),"unlock");
		addLockingMethod(new MESetAccessLevel(),"setComputerAccessLevel");
		addLockingMethod(new MEGetAccessLevel(),"getComputerAccessLevel");		
	}

		
	protected int getAccessLevel(int computerID)
	{
		if (computerLevels.containsKey(computerID))
			return computerLevels.get(computerID);
		else
			return 0;
	}
	
	protected void setAccessLevel(int computerID,int level)
	{
		computerLevels.put(computerID,level);
	}
		
	protected void addRestrictedMethod(IMethodExecutor method, String name, int accessLevel)
	{
		methods.add(new PeripheralMethod(name,method,false,accessLevel));
	}
	
	protected void addMethod(IMethodExecutor method, String name)
	{
		methods.add(new PeripheralMethod(name,method,false,0));
	}
	
	protected void addLockingMethod(IMethodExecutor method, String name)
	{
		methods.add(new PeripheralMethod(name,method,true,0));
	}
	
	public void queueForAttached(String eventName, Object[] args)
	{
		Iterator<IComputerAccess> iter=attachedPuters.values().iterator();
		int sideIndex=-1;
		for (int i=0; i<args.length; ++i)
			if (args[i]==null)
			{
				sideIndex=i;
				break;
			}
		
		while (iter.hasNext())
		{
			IComputerAccess comp=iter.next();
			Object[] argsCopy=args.clone();
			if (sideIndex>=0)
				argsCopy[sideIndex]=comp.getAttachmentName();
			comp.queueEvent(eventName, argsCopy );
		}
		
	}
	
	public int getRelativeSide(int absSide)
    {
    	//above and below are always above and below
    	if (absSide<2)
    		return absSide;
    	
    	int absToOrdered[] = { 0,2,1,3};

    	int facingSide=absToOrdered[worldObj.getBlockMetadata(xCoord,yCoord,zCoord)-2];
    	
    	int result=absToOrdered[(absToOrdered[absSide-2]+4-facingSide)%4]+2;
    	
    	return result;
    	
    }

	public int getFacing() 
	{		
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}
	
	public float getAngle() 
	{
		return facingToAngle[getFacing()];
	}
	
	public int getInstanceID()
	{
		return instanceID;
	}

	@Override
	public void updateEntity()
	{
		if (stateChanged)
		{
			saveInstanceData();
			stateChanged=false;
		}		
	}
	
	public void setInstanceID(int id)
    {
    	if (instanceID!=id)
    	{
    		if (instanceID!=id && instanceID!=0)    	
    			BioLock.getInstanceManager(this.getClass()).releaseID(id);
    	
	    	instanceID=id;
	    	if (!this.isInvalid() && worldObj!=null && !worldObj.isRemote && instanceID!=0)
	    		loadInstanceData();
    	}
    }
    
		
	public abstract void readInstanceFromNBT(NBTTagCompound nbt);
	public abstract void writeInstanceToNBT(NBTTagCompound nbt);
	
	public void loadInstanceData()
	{
		if (instanceID!=0)
		{
			File inFile=new File(BioLock.getInstanceManager(this.getClass()).getFilePath(instanceID));
			if(inFile.canRead())
			{
				NBTTagCompound nbt=null;
				DataInputStream dataIn=null;
				try {
					dataIn = new DataInputStream(new FileInputStream(inFile));
					nbt=new NBTTagCompound();
					nbt=CompressedStreamTools.readCompressed(dataIn);
					dataIn.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			    //read the common programmable peripheral info
			    //private boolean isLocked;
			    isLocked=nbt.getBoolean("isLocked");
			    
				//private String accessPassword;	
				accessPassword=nbt.getString("pw");
				if (accessPassword==null)
				{
					accessPassword="";
					isLocked=false;
				}
				
				//private HashMap<Integer,Integer> computerLevels;
			    NBTTagList puters=nbt.getTagList("puterIDs", 3);
			    NBTTagList levels=nbt.getTagList("puterLvls", 3);

			    while (puters.tagCount() > 0)
					computerLevels.put(((NBTTagInt)puters.removeTag(1)).func_150287_d(),((NBTTagInt)levels.removeTag(1)).func_150287_d());
					
				readInstanceFromNBT(nbt);
			}
		}
	}

	public void saveInstanceData()
	{		
		System.out.println("[BioLock] saveInstanceData");
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)//BioLock.proxy instanceof ProxyBioLockServer) //TODO: shouldSaveData? 
		{
			System.out.println("[BioLock] is on server");
			File outFile=new File(BioLock.getInstanceManager(this.getClass()).getFilePath(instanceID));
			if (!outFile.exists() || outFile.canWrite())
			{
				System.out.println("[BioLock] can write");
			    NBTTagCompound nbt=new NBTTagCompound();
				
			    //write the common programmable peripheral info
			    //private boolean isLocked;
			    nbt.setBoolean("isLocked", isLocked);
			    
				//private String accessPassword;	
				if (accessPassword!=null && accessPassword.length()>0)
					nbt.setString("pw", accessPassword);
				
				//private HashMap<Integer,Integer> computerLevels;
			    NBTTagList puters=new NBTTagList();
			    NBTTagList levels=new NBTTagList();
			    Iterator<Integer> iter=computerLevels.keySet().iterator();			    
				while (iter.hasNext())
				{
					int k=iter.next();
					puters.appendTag(new NBTTagInt(k));
					levels.appendTag(new NBTTagInt(computerLevels.get(k)));
				}
			    
			    nbt.setTag("puterIDs", puters);
			    nbt.setTag("puterLvls",levels);
			    
				writeInstanceToNBT(nbt);
				//now get my file
				
				DataOutputStream dataOut=null;
				try {
					dataOut = new DataOutputStream(new FileOutputStream(outFile));
					CompressedStreamTools.writeCompressed(nbt, dataOut);
					dataOut.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	
	/**
	 * called by mc, writes persistent data to the provided NBT, also calls saveInstanceData, which saves server-only data that
	 * is connected to the instanceID, not the actual block
	 */
	@Override 
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("instanceID",instanceID);
 		if (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
 			saveInstanceData();
	}
	
	/**
	 * called by mc, reads persistent data from the provided NBT, also calls loadInstanceData, which reads back server-only data that
	 * is connected to the instanceID, not the actual block
	 */
	@Override 
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
 		setInstanceID(nbt.getInteger("instanceID"));
 		if (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
 			loadInstanceData();
 		
	}

	/**
	 * returns whether the block is currently outputing redstone on the specified side
	 * 
	 * @param side the side - in WORLD, not LOCAL orientations
	 * @return true if block is outputing redstone on this side; otherwise false
	 */
	public boolean isPowering(int side)
	{
		//System.out.println("[BioLock] [DEBUG] TileEntityProgrammable:isPowering("+side+") = false");
		return false;
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

    public void validate()
    {
    	super.validate();
    	if (instanceID>0)
    		loadInstanceData();
    }

	@Override
	public void attach(IComputerAccess computer)
	{
		BLLog.debug("Attaching new computer id %d at %d, %d, %d", computer.getID(), xCoord, yCoord, zCoord);
		attachedPuters.put(computer.getID(),computer);
		BLLog.debug("Peripheral now has %d computers attached", attachedPuters.size());
		
		int count=0;
		Pair key=new Pair(computer.getID(),this.getClass().getName());
		
		if (computerPeripheralCounts.containsKey(key))
			count=computerPeripheralCounts.get(key);
			
		computerPeripheralCounts.put(key, count+1);
		
		if (count==0)
		{
			computer.mount("rom/programs/biolock", ComputerCraftAPI.createResourceMount(BioLock.class,"biolock", "lua"));
		}
	}

	@Override
	public void detach(IComputerAccess computer) 
	{		
		attachedPuters.remove(computer.getID());
		
		int count=1;
		Pair key=new Pair(computer.getID(),this.getClass().getName());
		
		if (computerPeripheralCounts.containsKey(key))
			count=computerPeripheralCounts.get(key);
		else
			BLLog.severe("[THREATLEVELGAMMA] - detached a peripheral that never attached?! O_o");
		
		if (count<=0)
			BLLog.severe("[THREATLEVELGAMMA] - detached more peripherals than we attached?! o_O");
		computerPeripheralCounts.put(key, count-1);
	}

	@Override	
	public String[] getMethodNames() 	
	{		
		if (methodNames==null)
		{
			//build names array on first call, save on new-ing all the time
			int size=methods.size();
			methodNames=new String[size];
			for (int i=0; i<size; ++i)
				methodNames[i]=methods.get(i).name;
		}
		return methodNames;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int methodIndex, Object[] arguments) throws LuaException, InterruptedException
	{
		try {
			PeripheralMethod method=methods.get(methodIndex);
			if (isLocked)
				if (method.locking)
					return new Object[] { false, "Method not available while peripheral is locked!" };
				else
				{
					int level=getAccessLevel(computer.getID());
					
					int requiredLevel=method.accessLevel;
					if (level<requiredLevel)
						return new Object[] { false, "This computer does not have permission to use this method!" };
					else
						return method.executor.run(this, computer, arguments);
				}
				
			else 
			{
				BLLog.info("unlocked, calling");
				return method.executor.run(this,computer,arguments);							
			}
				
		} catch(Exception ex) {			
			BLLog.severe("exception in callMethod");
			ex.printStackTrace();
			return new Object[] { false, " O_o How'd you do that?" };
		}
	}
	
	@Override
	public boolean equals(IPeripheral other) {
		if(other == null) {
			return false;
		}
		if(this == other) {
			return true;
		}
		if(other instanceof TileEntity) {
			TileEntity tother = (TileEntity) other;
			return tother.getWorldObj().equals(worldObj) && tother.xCoord == this.xCoord && tother.yCoord == this.yCoord && tother.zCoord == this.zCoord;
		}
		return false;
	}
	
}
