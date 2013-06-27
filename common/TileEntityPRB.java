package gopheratl.biolock.common;

import gopheratl.biolock.common.BioLock.INBTAble;

import java.util.HashMap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import dan200.computer.api.IComputerAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;

public class TileEntityPRB extends TileEntityProgrammable {
	
	private interface IPRBInput extends INBTAble {
		public abstract int get();
		
	}
	
	private static class PRBInConst implements IPRBInput {

		int value;
		
		public PRBInConst(int v)
		{
			value=v;
		}
		
		@Override
		public int get() {
			return value;
		}

		@Override
		public boolean readFromNBT(NBTBase nbt) {			
			value=((NBTTagInt)nbt).data;
			return true;
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt, String name) 
		{			
			//System.out.println("[BioLock] [DEBUG] writing const "+value);
			nbt.setInteger(name,value);
		}
		
		
	}
	
	private static PRBInConst inConst[];
	static {
		inConst=new PRBInConst[16];
		for (int i=0; i<16; ++i)
			inConst[i]=new PRBInConst(i);		
	}
	
	private class PRBInSide implements IPRBInput {
		int side;
		
		PRBInSide(int side)
		{
			this.side=side;
		}
		
		@Override
		public int get() {
			return inputs[side];
		}

		@Override
		public boolean readFromNBT(NBTBase nbt) {
			this.side=sideMap.get(((NBTTagString)nbt).data);
			return true;
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt, String name) {
			//System.out.println("[BioLock] [DEBUG] writing side "+sideNames[side]+"("+side+")");
			nbt.setString(name,sideNames[side]);
		}
	}
	
	private PRBInSide[] sideInputs;
	
	private interface IPRBOperation  {
		public abstract int getResult(IPRBInput a, IPRBInput b);
		public abstract String string();
	}
	
	private static class PRBOpAnd implements IPRBOperation {

		@Override
		public int getResult(IPRBInput a, IPRBInput b) {
			return (a.get()>0 && b.get()>0)?15:0;
		}

		@Override
		public String string() {
			return "and";
		}
	}
	
	private static class PRBOpOr implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get()>0 || b.get()>0)?15:0;
		}
		@Override
		public String string() {
			return "or";
		}
	}

	private static class PRBOpXor implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return ((a.get()>0) != (b.get()>0))?15:0;
		}

		@Override
		public String string() {
			return "xor";
		}
	}

	private static class PRBOpNor implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get()>0 || b.get()>0)?0:15;
		}

		@Override
		public String string() {
			return "nor";
		}
	}

	private static class PRBOpNand implements IPRBOperation {

		@Override
		public int getResult(IPRBInput a, IPRBInput b) {
			return (a.get()>0 && b.get()>0)?0:15;
		}

		@Override
		public String string() {
			return "nand";
		}
	}

	private static class PRBOpSame implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return ((a.get()>0) == (b.get()>0))?15:0;
		}

		@Override
		public String string() {
			return "same";
		}
	}

	private static class PRBOpEqual implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			System.out.println("PRBOpEqual:GetResult - a="+a.get()+", b="+b.get());
			return (a.get() == b.get())?15:0;
		}

		@Override
		public String string() {
			return "==";
		}
	}

	private static class PRBOpNotEqual implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get() != b.get())?15:0;
		}

		@Override
		public String string() {
			return "!=";
		}
	}

	private static class PRBOpGreaterThan implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get() > b.get())?15:0;
		}

		@Override
		public String string() {
			return ">";
		}
	}
	
	private static class PRBOpLessThan implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get() < b.get())?15:0;
		}

		@Override
		public String string() {
			return "<";
		}
	}

	private static class PRBOpGreaterOrEqual implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get() >= b.get())?15:0;
		}

		@Override
		public String string() {
			return ">=";
		}
	}

	private static class PRBOpLessOrEqual implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			return (a.get() <= b.get())?15:0;
		}

		@Override
		public String string() {
			return "<=";
		}
	}
	
	private static class PRBOpMax implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			int ia=a.get();
			int ib=b.get();
			return ia>ib?ia:ib;
		}

		@Override
		public String string() {
			return "max";
		}
	}

	private static class PRBOpMin implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			int ia=a.get();
			int ib=b.get();
			return ia<ib?ia:ib;
		}

		@Override
		public String string() {
			return "min";
		}
	}
	
	private static class PRBOpIf implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			int ia=a.get();
			int ib=b.get();
			return ib>0?ia:0;
		}

		@Override
		public String string() {
			return "if";
		}
	}

	private static class PRBOpUnless implements IPRBOperation {
		@Override
		public int getResult(IPRBInput a, IPRBInput b)
		{
			int ia=a.get();
			int ib=b.get();
			return ib==0?ia:0;
		}

		@Override
		public String string() {
			return "unless";
		}
	}
	
	
	public static HashMap<String,IPRBOperation> opMap;
	
	static {
		opMap=new HashMap<String,IPRBOperation>();
		opMap.put("and", new PRBOpAnd());
		opMap.put("or", new PRBOpOr());
		opMap.put("xor",new PRBOpXor());
		opMap.put("eor",new PRBOpXor());
		opMap.put("nand",new PRBOpNand());
		opMap.put("nor",new PRBOpNor());
		opMap.put("same",new PRBOpSame());
		opMap.put("nxor",new PRBOpSame());
		opMap.put("xnor",new PRBOpSame());
		opMap.put("enor",new PRBOpSame());
		opMap.put("neor",new PRBOpSame());

		opMap.put("==",new PRBOpEqual());
		opMap.put("=",new PRBOpEqual());
		opMap.put("!=",new PRBOpNotEqual());
		opMap.put("~=",new PRBOpNotEqual());
		opMap.put("<>",new PRBOpNotEqual());
		opMap.put(">",new PRBOpGreaterThan());
		opMap.put(">=",new PRBOpGreaterOrEqual());
		opMap.put("<",new PRBOpLessThan());
		opMap.put("<=",new PRBOpLessOrEqual());

		opMap.put("max",new PRBOpMax());
		opMap.put("min",new PRBOpMin());
		opMap.put("if",new PRBOpIf());
		opMap.put("unless",new PRBOpUnless());
		opMap.put("ifnot",new PRBOpUnless());
	}
		
	private IPRBInput objectToInput(Object o) throws Exception
	{
		IPRBInput in=null;
			
		if (o==null)
			in=inConst[0];
		
		else if (o instanceof String)
		{
			String s=((String)o).toLowerCase();
			Integer side=sideMap.get(s);
			if (side!=null)
				in=sideInputs[side];
			else
				throw new Exception("String argument was not a valid side!");
			
		}
		else if (o instanceof Double)
			in=new PRBInConst(((Double)o).intValue());
		else if (o instanceof Integer)
			in=new PRBInConst((Integer)o);
		else if (o instanceof Boolean)
			in=new PRBInConst((Boolean)o?15:0);
		else 
		{
			System.out.println(o.getClass().getName());
			throw new Exception("Invalid argument type!");
		}
		
		return in;
	}
	
	private IPRBInput nbtToInput(NBTBase nbt)
	{
		IPRBInput in=null;
		
		if (nbt==null)
			in=inConst[0];
		else if (nbt instanceof NBTTagString)
		{
			String s=((NBTTagString)nbt).data;
			Integer side=sideMap.get(s);
			if (side!=null)
				in=sideInputs[side];
		}
		else if (nbt instanceof NBTTagInt)
			in=inConst[((NBTTagInt)nbt).data];

		return in;
				
	}
	
	private IPRBOperation stringToOp(String str)
	{
		IPRBOperation op=null;
		op=opMap.get(str);
		return op;			
	}
	
	class PRBProgram {
		public IPRBInput a, b;
		public IPRBOperation op;
		public int output=0;
		
		public PRBProgram()
		{
			a=inConst[0];
			b=inConst[0];
			op=opMap.get("or");
			output=0;
		}
		
		public void reprogram(IPRBInput a, IPRBOperation op, IPRBInput b) throws Exception
		{
			if (a==null)
				throw new Exception("a set to null... WHO'S RESPONSIBLE FOR THIS?!");
			if (b==null)
				throw new Exception("b set to null... WHO'S RESPONSIBLE FOR THIS?!");
			if (op==null)
				throw new Exception("op set to null... WHO'S RESPONSIBLE FOR THIS?!");
			
			this.a=a;
			this.b=b;
			this.op=op;
		}
		
		public boolean update()
		{
			if (op==null || a==null || b==null)
			{
				System.out.println("[BioLock] invalid PRBProgram?!");
				return false;
			}
			int newOutput=op.getResult(a, b);
			if (newOutput!=output)
			{
				output=newOutput;
				return true;
			}		
			
			return false;
		}
	}

	
    public static String getBaseInstanceFileName()
    {
    	return "prb";
    }

    
    
	private PRBProgram[] programs;
	/**
	 * the current input values by side, in LOCAL orientation 
	 */
	private int inputs[];
	private int outputs[];
	private boolean updateNeeded;
	
	public TileEntityPRB()
	{
		super();

		sideInputs=new PRBInSide[6];
		for (int i=0;i<6;++i)
			sideInputs[i]=new PRBInSide(i);
		
		programs=new PRBProgram[] { new PRBProgram(), new PRBProgram(),new PRBProgram(),new PRBProgram(),new PRBProgram(),new PRBProgram(),};
		
		for (int i=0; i<6; ++i)
			programs[i]=new PRBProgram();

		inputs=new int[] {0, 0, 0, 0, 0, 0, };
		outputs=new int[] {0, 0, 0, 0, 0, 0, };
		updateNeeded=true;
		
		addLockingMethod(new MESetOutput(),"setOutput");
		addLockingMethod(new MEGetOutput(),"getOutput");
		addLockingMethod(new MEGetInput(),"getInput");
		
	}

	/**
	 * Sets the redstone input level for a given side. 
	 * Side is in absolute world directions, and will be translated based on block orientation.
	 * @param side
	 * @param value
	 */
	public void setSideInput(int side, int value)
	{
		int relativeSide=getRelativeSide(side);
		if (inputs[relativeSide]!=value)
		{
			//TODO signal something changed, to allow for conditional state update checks
			//queue message
			updateNeeded=true;
			queueForAttached("prb_input", new Object[] { null, sideNames[relativeSide], value } );
		}
		inputs[relativeSide]=value;
	}
	
	@Override
	public boolean isPowering(int side)
	{
		//System.out.println("[BioLock] [DEBUG] ("+this+").isPowering("+side+") -> "+outputs[getRelativeSide(side)]+">0"+" - isRemote="+(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER));		
		return outputs[getRelativeSide(side)]>0;//programs[getRelativeSide(side)].output>0;
	}
	
	@Override
	public int getSideOutput(int side)
	{
		//System.out.println("[BioLock] [DEBUG] ("+this+").getSideOutput("+side+") -> "+programs[getRelativeSide(side)].output+" - isRemote="+(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER));
		return programs[getRelativeSide(side)].output;
	}
	
	static void loadClassDataForWorld() 
	{
		//nothing to do for this class at present...
	}

	@Override
	public void readInstanceFromNBT(NBTTagCompound nbt) 
	{
		//System.out.println("[BioLock] [DEBUG] reading from NBT");
		NBTTagCompound nbtPrograms=nbt.getCompoundTag("programs");
		try {
			for (int i=0; i<6; ++i)
			{
				NBTTagCompound ntc=nbtPrograms.getCompoundTag(sideNames[i]);
				IPRBInput a,b;
				IPRBOperation op;
				a=nbtToInput(ntc.getTag("a"));
				b=nbtToInput(ntc.getTag("b"));
				op=stringToOp(ntc.getString("op"));
				programs[i].reprogram(a, op, b);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("[BioLock] Error loading programs from NBT for PRB with id "+instanceID);
		}
	}

	@Override
	public void writeInstanceToNBT(NBTTagCompound nbt) 
	{
		//System.out.println("[BioLock] [DEBUG] writing to NBT");

		NBTTagCompound nbtPrograms=new NBTTagCompound("programs");
		for (int i=0; i<6; ++i)
		{
			//System.out.println("[BioLock] [DEBUG] writing program for side "+sideNames[i]+"("+i+")");

			NBTTagCompound ntc=new NBTTagCompound(sideNames[i]);
			programs[i].a.writeToNBT(ntc,"a");
			programs[i].b.writeToNBT(ntc,"b");
			//System.out.println("[BioLock] [DEBUG] writing op "+programs[i].op.string());
			ntc.setString("op", programs[i].op.string());
			
			nbtPrograms.setCompoundTag(sideNames[i], ntc);
		}
		nbt.setCompoundTag("programs", nbtPrograms);
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "prb";
	}
	
	@Override
	public void updateEntity()
	{
		boolean outputChanged=false;
		if (updateNeeded)
		{
			for (int side=0; side<6; ++side)
			{
				if (programs[side].update())
				{
					outputChanged=true;				
				}
			}
			updateNeeded=false;
		}
		
		if (outputChanged)
		{
			//build and send packet to client
			worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, BioLock.Config.prbBlockID);
		}		

		super.updateEntity();
	}
			
	@Override 
	public Packet getDescriptionPacket()
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			System.out.println("[BioLock] Server sending PRB description to client...");
			NBTTagCompound nbt=new NBTTagCompound();
			NBTTagList list=new NBTTagList();
			for (int i=0; i<6; ++i)
				list.appendTag(new NBTTagInt(null,programs[i].output));
			nbt.setTag("outputs", list);
			return new Packet132TileEntityData(this.xCoord,this.yCoord,this.zCoord,1,nbt);
		}
		return null;
	}
	
	@Override 
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			System.out.println("[BioLock] ("+this+") Client parsing PRB description from server...");
			NBTTagCompound nbt=pkt.customParam1;
			NBTTagList list=nbt.getTagList("outputs");
			for (int i=0; i<6; ++i)
			{
				int newVal=((NBTTagInt)list.tagAt(i)).data;
				System.out.println("[BioLock] outputs["+i+"] was "+outputs[i]+", now "+newVal);
				outputs[i]=newVal;
			}
			worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);			
		}		
	}
	
	private class MESetOutput implements IMethodExecutor {
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) throws Exception
		{
			IPRBInput a,b;
			IPRBOperation op;
			if (args.length==0)
				throw new Exception("Missing args. Expected String, [String or Number], [String], [String or Number]");

			Integer side=sideMap.get(args[0]);
			if (side==null)
				throw new Exception("Invalid side!");
			
			a=objectToInput(args.length>1 ? args[1] : null);
			op=stringToOp(args.length>2 ? args[2] instanceof String ? (String)(args[2]) : "or" : "or");
			b=objectToInput(args.length>3 ? args[3] : null);

			programs[side].reprogram(a, op, b);
			updateNeeded=true;
			stateChanged=true;
			
			return returnSuccess;
		}
	}

	private class MEGetOutput implements IMethodExecutor {
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) throws Exception
		{
			if (args.length==0)
				throw new Exception("Missing args. Expected String");

			Integer side=sideMap.get(args[0]);
			if (side==null)
				throw new Exception("Invalid side!");
			
			return new Object[] {programs[side].output};
		}
	}

	private class MEGetInput implements IMethodExecutor {
		public Object[] run(TileEntityProgrammable te, IComputerAccess computer, Object[] args) throws Exception
		{
			if (args.length==0)
				throw new Exception("Missing args. Expected String");

			Integer side=sideMap.get(args[0]);
			if (side==null)
				throw new Exception("Invalid side!");

			return new Object[] { inputs[side] };
		}
	}
	
	
}
