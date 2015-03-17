package gopheratl.biolock.common;

import gopheratl.GopherCore.GopherCore;
import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.common.network.PacketBiolockScan;
import gopheratl.biolock.common.network.PacketHandler;
import gopheratl.biolock.common.util.BLLog;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import com.google.common.primitives.Bytes;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class TileEntityBioLock extends TileEntityProgrammable {
	//******  static members
	// map of usernames to hashes
	static Map<String,String> playerHashes=null;
	// set of known hashes
    static Set<String> printSet=null;
    // rng
    static Random random=new Random();
    // set of characters used in hashes
    static String printChars="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";

    //the set of instances that are currently animating, iterated over in onTick
	//only updated and maintained client-side, on server should always be empty
	static Set<TileEntityBioLock> animatedInstances=new HashSet<TileEntityBioLock>();
		

	//**** Info about a stored print
	class StoredPrint {
		String name;
		String print;
		byte accessLevel;
		
		public StoredPrint(String name, String print, byte accessLevel)
		{
		  	this.name=name;
		  	this.print=print;
		  	this.accessLevel=accessLevel;
		}
	}
	
	//******* Instance Variables
	//index of the current face texture
	public int faceFrameIndex;
	//index for client use, current face texture
	public int clientFrameIndex;
	//frame tick counter, used in controlling face animation 
	int frameTicks;
	//unique ID of this instance, for associating with internal data; -1 if none
	int instanceID;
	boolean needsLoading;
	// true if is animating; used to fast-exit from updateEntity
	boolean animating;
	// true if the blocks redstone output state has changed
	boolean outputChanged;
	//true if one or more activation programs are running
	boolean programsActive;
	
	//map of the prints stored to this scanner's internal memory and 
	//the details of that print, keyed on the print 
    Map<String,StoredPrint> storedPrints;
    //array of redstone programs, indexed by relative side
    RedstoneProgram[] redstonePrograms;
    
    
    public static String getBaseInstanceFileName()
    {
    	return "biolock";
    }
    
	//Constructor
	public TileEntityBioLock()
	{				
		super();
				
		//now time for my PERSONAL data		
		faceFrameIndex=0;
		clientFrameIndex = 0;
		frameTicks=0;
		instanceID=0;
		
		storedPrints=Collections.synchronizedMap(new HashMap());
		//init on peripheral.program(...), add removeProgram(side) peripheral method 
		redstonePrograms=new RedstoneProgram[] {
				null,
				null,
				null,
				null,
				null,
				null,
		};
		
		addLockingMethod(new MELearn(),"learn");
		addLockingMethod(new MEGetLearnedNames(),"getLearnedNames");
		addLockingMethod(new MEGetAccessLevel(),"getAccessLevel");
		addLockingMethod(new MEGetPrint(),"getPrint");
		addLockingMethod(new MEForgetPrint(),"forget");
		addLockingMethod(new MEProgram(), "program");
		addLockingMethod(new MEForgetProgram(),"forgetProgram");
	}
	
	
	static void loadClassDataForWorld()
	{		
		//load hashes!
		ObjectInputStream input;
		
		BLLog.debug("Loading class data");
		
		try {
			File inFile=new File(loadedWorldDir+"fistPrints.dat");
			BLLog.debug("loadedWorldDir="+loadedWorldDir );
			if(inFile!=null && inFile.exists() && inFile.canRead())
			{
				ObjectInputStream ois=new ObjectInputStream(new FileInputStream(inFile));
				playerHashes=Collections.synchronizedMap((HashMap)ois.readObject());
				Object o=ois.readObject();
				if (o instanceof HashSet)
					printSet=(HashSet)o;
				else
				{						
					printSet=new HashSet();					
				}
				ois.close();
			}
			else
			{
				//first time, init to blank set
				printSet=new HashSet();
				BLLog.warn("The fuuu... couldn't open file "+loadedWorldDir+"fistPrints.dat");
			}
			
		} catch (IOException e) {
			// TODO foo				
			BLLog.warn("IO Exception! couldn't open file "+loadedWorldDir+"fistPrints.dat");
		} catch (ClassNotFoundException e) {
			BLLog.warn("ClassNotFound exception! couldn't open file "+loadedWorldDir+"fistPrints.dat");				// TODO bar				
		}
		finally {
			if (playerHashes==null)
			{
				playerHashes=Collections.synchronizedMap(new HashMap());
				printSet=new HashSet();
			}
		}
			
		
	}
	
		

	public void readInstanceFromNBT(NBTTagCompound nbt)
	{
		
		NBTTagList prints=nbt.getTagList("prnts", 8);
		NBTTagList names=nbt.getTagList("names", 8);
		byte[] accessLevels=nbt.getByteArray("lvls");
		if (prints.tagCount()!=names.tagCount() || prints.tagCount()!=accessLevels.length)
		{
		   System.out.println("[BioBlock] Mismatch in lengths of print, name, and level arrays; not reading NBT data!");
		   return;
		}
		try {
			for (int i=0; i<accessLevels.length; i++)
			{
				String print = prints.getStringTagAt(i);
				String name = names.getStringTagAt(i);
				storedPrints.put(print,new StoredPrint(name,print,accessLevels[i]));
			}
		}
		catch (Exception e)
		{
			System.out.println("[BioLock] Exception in FistPrintScannerPeripheral.readFromNBT: "+e.getLocalizedMessage());
			storedPrints=Collections.synchronizedMap(new HashMap());
		}
		//now read the programs
		NBTTagList programs=nbt.getTagList("redstonePrograms", 10);
		if (programs!=null && programs.tagCount()==6)
			for (int i=0; i<6; ++i)
			{
				NBTTagCompound prog = programs.getCompoundTagAt(i);
				if (prog.hasNoTags())
					redstonePrograms[i]=null;
				else
					redstonePrograms[i]=new RedstoneProgram(prog);
			}		
	}
	
	public void writeInstanceToNBT(NBTTagCompound nbt)
	{		
		System.out.println("[BioLock] writeInstanceToNBT");
		NBTTagList names=new NBTTagList();
		NBTTagList prints=new NBTTagList();
		if (storedPrints==null)
		{
			System.out.println("[BioBlock] Wtf?! Writing my NBT before I've ever read it?");
			return;
		}
		byte[] accessLevels=new byte[storedPrints.size()];
		Iterator<StoredPrint> iter=storedPrints.values().iterator();
		int i=0;
		while (iter.hasNext())
		{
		    StoredPrint cur=iter.next();
		    names.appendTag(new NBTTagString(cur.name));
			prints.appendTag(new NBTTagString(cur.print));
			accessLevels[i++]=cur.accessLevel;
		}
		nbt.setTag("prnts", prints);
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
				

		if (animating)
		{
			animating=!animationTick();
		}
		
		if (outputChanged)
		{
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, BioLock.Blocks.bioLock);
			outputChanged=false;
		}
		
		
	}
		
    public boolean animationTick()
    {
    	boolean finished=false;
    	int oldIndex = faceFrameIndex;
    	
    	if (faceFrameIndex==10)
    	{
    		frameTicks=frameTicks+1;
    		if(frameTicks==5)
    		{
    			faceFrameIndex=0;
    		 	frameTicks=0;
  				finished=true;
    		}
    	}
    	else
        	faceFrameIndex=faceFrameIndex+1;

    	BLLog.debug("Animating step: %d, t: %d", faceFrameIndex, frameTicks);
    	if (faceFrameIndex != oldIndex) {
    		BLLog.debug("Sending packet");
	    	PacketBiolockScan packet = new PacketBiolockScan((short) instanceID, worldObj.provider.dimensionId, xCoord, yCoord, zCoord, faceFrameIndex);
	    	clientFrameIndex = faceFrameIndex;
						
			PacketHandler.INSTANCE.sendToAllAround(packet, new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, (double)xCoord, (double)yCoord, (double)zCoord, 64d));
    	}
		return finished;
    }
    
    
    
	public void invalidate()
	{
		super.invalidate();		
		animating=false;
	}
	
	public void onChunkUnload()
	{
		super.onChunkUnload();
		animating=false;
	}	
	
	static String randomPrint()
	{		
	   String newPrint="";
	   for (int i=1;i<32;++i)
	   {
		   int index=random.nextInt(64);		   
		   newPrint=newPrint+printChars.charAt(index);
	   }
	   return newPrint;
	}
	
	static Object getHandprint(String username) throws IOException
	{
	   if (TileEntityBioLock.playerHashes.containsKey(username)==false)
	   {
		   //generate a new handprint
		   String newPrint;
		   do {
		     newPrint=randomPrint();
		   } while (printSet.contains(newPrint)==true);
		   
		   playerHashes.put(username,newPrint);	
		   printSet.add(newPrint);
		   //save them
		   ObjectOutputStream output;
		   try {
			   output= new ObjectOutputStream(new FileOutputStream(loadedWorldDir +"fistPrints.dat"));
			   HashMap<String,String> temp=new HashMap(playerHashes);
			   output.writeObject(temp);
			   output.writeObject(printSet);
			   output.close();
		   } catch (FileNotFoundException e) {
			   //doesn't exist, create it
			   playerHashes=Collections.synchronizedMap(new HashMap());
			   printSet=new HashSet();
		   }
		   
	   }
	   return playerHashes.get(username);
	}
		
	protected StoredPrint findStoredByName(String name)
	{		
		Iterator<StoredPrint> iter=storedPrints.values().iterator();
		while (iter.hasNext())
		{
			StoredPrint p=iter.next();
			if (p.name.equals(name))
				return p;			
		}
		return null;
	}
	
	
	@Override
	public String getType() 
	{
		return "biolock";
	}

	//****************
	//  Peripheral method implementations
	
	private class MELearn implements IMethodExecutor {

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
			StoredPrint printData=storedPrints.get((String)args[0]);
			if (!storedPrints.containsKey((String)args[1]) && storedPrints.size()>=BioLock.Config.internalMemorySize)
				return new Object[] {false, "Internal Memory Full"};				
			
			int accessLevel=((Double)args[2]).intValue();
			if (accessLevel<1 || accessLevel>5)
			  	return new Object[] {false, "Access Level must be in range 1-5"};
			
			String name=(String)args[0];
			String print=(String)args[1];	
			
			storedPrints.put(print, new StoredPrint(name,print,(byte)accessLevel));
			
			stateChanged=true;
			
			return returnSuccess;		
		}
		
	}
	
	private class MEGetLearnedNames implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
		    Object[] list=new Object[storedPrints.size()];
		    Iterator<StoredPrint> iter=storedPrints.values().iterator();
		    int minLevel=0;
		    if (arguments.length>0)
		    {
		    	if (!(arguments[0] instanceof Double))
		    	{
		    		return new Object[] {false, "Invalid arguments: Expected Number, got "+arguments[0].getClass().getName() };
		    	}
		    	Double d=(Double)arguments[0];
		    	minLevel=d.intValue();
		    }
		    int i=0;
		    while (iter.hasNext())
		    {
		       StoredPrint p=iter.next();
		       if (p.accessLevel>=minLevel)
			       list[i++]=p.name;       
		    }
		    return list;
		}
		
	}
	
	private class MEGetAccessLevel implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
			if (arguments.length<1)
				return new Object[] { false, "Invalid arguments: Expected String" };
			if (!(arguments[0] instanceof String))
				return new Object[] { false, "Invalid arguments: Expected String, got "+arguments[0].getClass().getName() };
			StoredPrint p=findStoredByName((String)arguments[0]);
			if (p==null)
				return new Object[] { false, "Name not stored!" };
			return new Object[] { p.accessLevel };
		}
		
	}

	private class MEGetPrint implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
			if (arguments.length<1)
				return new Object[] { "Invalid arguments: Expected String" };
			if (!(arguments[0] instanceof String))
				return new Object[] { "Invalid arguments: Expected String, got "+arguments[0].getClass().getName() };
			StoredPrint p=findStoredByName((String)arguments[0]);
			if (p==null)
				return new Object[] { false, "Name not stored!" };
			return new Object[] { p.print };
		}
		
	}

	private class MEForgetPrint implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
			if ( arguments.length<1 || !(arguments[0] instanceof String) )
			{
		   		return new Object[] {false, "Invalid arguments"};
			}
			
			String name=(String)arguments[0];
			StoredPrint p=findStoredByName(name);
			if (p!=null)
			{
				storedPrints.remove(p.print);
				stateChanged=true;							
				return new Object[] { true }; 
			}
			
			return new Object[] { false, "Name not stored!" };
		}
		
	}

	private class MEProgram implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
			if (arguments.length<3 ||
					!(arguments[0] instanceof String) ||
					!(arguments[1] instanceof Double) ||
					!(arguments[2] instanceof Double) ||
					(arguments.length>=4 && !(arguments[3] instanceof Boolean)) ||
					(arguments.length>=5 && !(arguments[4] instanceof Boolean)) )				
				return new Object[] { false, "Invalid arugments, expected string side, int accessLevel, int ticks, [boolean reverseOutput=false], [boolean reverseAccess=false]"};
			
			String strSide=((String)arguments[0]).toLowerCase();
			Integer n=sideMap.get(strSide);
			if(n==null)
				return new Object[] { false, "Invalid side" };
			
			redstonePrograms[n]=new RedstoneProgram(
					((Double)arguments[1]).intValue(), 
					((Double)arguments[2]).intValue(), 
					arguments.length>3 ? (Boolean)arguments[3] : false, 
					arguments.length>4 ? (Boolean)arguments[4] : false);
			
			outputChanged=true;
			stateChanged=true;
			return returnSuccess;
		}
		
	}

	private class MEForgetProgram implements IMethodExecutor {

		@Override
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] arguments) 
		{
			if (arguments.length<1 || !(arguments[0] instanceof String))
				return new Object[] { false, "Invalid arguments, expected string" };
			
			String strSide=((String)arguments[0]).toLowerCase();
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

	
	
	//called from block in isProvidingStrongPower to decide 
	@Override
	public boolean isPowering(int side)
	{		
		//convert side to side relative to my facing direction
		int relSide=getRelativeSide(side);
		//System.out.println("[BioLock] [DEBUG] TileEntityProgrammable:isPowering("+side+") => relSide "+relSide);//+"="+redstonePrograms[relSide].getOutput());
		
		return redstonePrograms[relSide]==null ? false : redstonePrograms[relSide].getOutput();
	}
	
	public void doActivation(EntityPlayer player) 
	{
		if (!worldObj.isRemote)
		{
			try {
				String identString=(String)getHandprint(player.getUniqueID().toString());
				
				Double accessLevel=0D;
				StoredPrint printInfo=storedPrints.get(identString);
				String name="";
				if (printInfo!=null)
				{
					name=printInfo.name;
					accessLevel=(double)printInfo.accessLevel;
				}

				queueForAttached("biolock",new Object[] { identString, null , name, accessLevel } );

				for (int i=0; i<6; ++i)
				{
					if (redstonePrograms[i]!=null && redstonePrograms[i].onActivation(accessLevel.intValue()))
					{
						programsActive=true;
						outputChanged=true;
					}
				}

				animating = true;
				faceFrameIndex = 0;
				PacketBiolockScan packet = new PacketBiolockScan((short) instanceID, worldObj.provider.dimensionId, xCoord, yCoord, zCoord, faceFrameIndex);
				
				EntityPlayerMP p=(EntityPlayerMP)player;			
				PacketHandler.INSTANCE.sendToAllAround(packet, new NetworkRegistry.TargetPoint(p.dimension, (double)xCoord, (double)yCoord, (double)zCoord, 64d));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
 		}
	}

	public void doAnimate(int frame) 
	{
		BLLog.debug("Animating frame %d at %d, %d, %d", frame, xCoord, yCoord, zCoord);
		BLLog.debug("Animating, old value was %d, new value is %d", clientFrameIndex, frame);
		clientFrameIndex = frame;
		faceFrameIndex = frame;

	}

}
