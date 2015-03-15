package gopheratl.biolock.common;

import net.minecraft.nbt.NBTTagCompound;

public class RedstoneProgram {
	//*** members defining the behavior of this program
	//access level required to activate
	int activationLevel;
	//number of ticks to remain activated
	int activationTicks;
	//if true, inverts signal on this side, so on unless activated
	boolean invertOutput;
	//if true, inverts level range - activates for any LESS THAN accessLevel
	boolean invertAccess;
	
	//*** members defining the current state of this program
	//am I active now?
	boolean active; 
	//how many ticks left til I deactivate?
	int ticksRemaining;
	
	public RedstoneProgram()
	{
		invertOutput=false;
		activationLevel=6;			
		invertAccess=false;
		activationTicks=10;
					
		active=false;
		ticksRemaining=0;
	}
	
	public RedstoneProgram(int level, int duration, boolean invertOut, boolean invertLvl)
	{
		activationLevel=level;
		activationTicks=duration;
		invertOutput=invertOut;
		invertAccess=invertLvl;
	}
	
	public static Object buildFromObjArr(Object[] arguments)
	{
		if (arguments.length<3 ||
				!(arguments[0] instanceof String) ||
				!(arguments[1] instanceof Double) ||
				!(arguments[2] instanceof Double) ||
				(arguments.length>=4 && !(arguments[3] instanceof Boolean)) ||
				(arguments.length>=5 && !(arguments[4] instanceof Boolean)) )				
			return new Object[] { false, "Invalid arugments, expected string side, int accessLevel, int ticks, [boolean reverseOutput=false], [boolean reverseAccess=false]"};
					
		return new RedstoneProgram(
				((Double)arguments[1]).intValue(), 
				((Double)arguments[2]).intValue(), 
				arguments.length>3 ? (Boolean)arguments[3] : false, 
				arguments.length>4 ? (Boolean)arguments[4] : false);
		
		
	}
	//create from an NBTTagCompound
	public RedstoneProgram(NBTTagCompound nbt)
	{
		activationLevel=nbt.getInteger("level");
		activationTicks=nbt.getInteger("ticks");
		invertOutput=nbt.getBoolean("invertOut");
		invertAccess=nbt.getBoolean("invertLevel");
	}
	
	public NBTTagCompound getNBT()
	{
		NBTTagCompound nbt=new NBTTagCompound();
		nbt.setInteger("level", activationLevel);
		nbt.setInteger("ticks",activationTicks);
		nbt.setBoolean("invertOut",invertOutput);
		nbt.setBoolean("invertLevel", invertAccess);
		return nbt;
	}
	//
	public void setProgram(int level, int duration, boolean invertOut, boolean invertLvl)
	{
		activationLevel=level;
		activationTicks=duration;
		invertOutput=invertOut;
		invertAccess=invertLvl;
	}
	
	//only called if active
	public void tick()
	{
		ticksRemaining--;
		if (ticksRemaining==0)
			active=false;			
	}
	
	//returns true if activation state just changed
	public boolean onActivation(int accessLevel)
	{
		if (invertAccess == (accessLevel<activationLevel))
		{
			//reset tick counter regardless
			ticksRemaining=activationTicks;
			//if not already active, activate and return true for state change
			if (!active)
			{
				active=true;
				return true;
			}
		}
		return false;
	}
	
	public boolean getOutput()
	{			
		if (!active)
			return invertOutput;
		
		return !invertOutput;			
	}
}
