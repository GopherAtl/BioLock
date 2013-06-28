package gopheratl.biolock.common;

import gopheratl.GopherCore.GopherCore;
import gopheratl.GopherCore.InstanceDataManager;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;



import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="BioLock", name="BioLock", version="2.0b")
@NetworkMod(clientSideRequired=true, serverSideRequired=true)
public class BioLock
{
	public int[] foo={1,2,3};
	
	public static Block bioLock;
	public static Block prb;
	public static Block keypadLock;
	
	private static HashMap<String,InstanceDataManager> instanceManagers=new HashMap<String,InstanceDataManager>();	
	
	public static InstanceDataManager getInstanceManager(Class c)
	{
		String className=c.getSimpleName();
		if (!instanceManagers.containsKey(className))
			try {
				instanceManagers.put(className, new InstanceDataManager("BioLocks", (String)c.getMethod("getBaseInstanceFileName").invoke(null) ));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("[BioLock] Failed to create InstanceManager for class "+c.getName()+"! is getBaseInstanceFileName() defined?");
				e.printStackTrace();
			}			

		return instanceManagers.get(className);			
	}
	@SidedProxy(clientSide = "gopheratl.biolock.client.ProxyBioLockClient", serverSide= "gopheratl.biolock.server.ProxyBioLockServer")
    public static ProxyBioLock proxy;
	
	public static class Config
	{
		public static int bioLockBlockID;
		public static int prbBlockID;
		public static int keypadLockBlockID;
		public static boolean enableKnowsNames;
		public static int internalMemorySize;
	}
	
	public static interface INBTAble {
		public boolean readFromNBT(NBTBase nbt);
		public void writeToNBT(NBTTagCompound nbt, String name);
	}

	public static void resetInstanceManagers()
	{
		//System.out.println("[BioLock] [DEBUG] Resetting instance managers..");
		instanceManagers.put(TileEntityBioLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityBioLock.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityPRB.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityPRB.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityKeypadLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityKeypadLock.getBaseInstanceFileName()));

		TileEntityBioLock.loadClassDataForWorld();

	}
	
	@Mod.PreInit
	public void preInit( FMLPreInitializationEvent evt )
	{
		Configuration configFile = new Configuration(evt.getSuggestedConfigurationFile());
		
		Property prop = configFile.getBlock("bioLock_BlockID", 735);
		prop.comment = "Block ID for the BioLock peripheral";
		Config.bioLockBlockID = prop.getInt();

		prop = configFile.getBlock("PRB_BlockID", 736);
		prop.comment = "Block ID for the Programmable Redstone Block peripheral";
		Config.prbBlockID = prop.getInt();

		prop = configFile.getBlock("Keypad_BlockID",737);
		prop.comment = "Block ID for the Keypad lock peripheral";
		Config.keypadLockBlockID = prop.getInt();
		
		prop = configFile.get("general", "bioLock_GivesUsernames", false);
		prop.comment = "If true, BioLock's events give a username instead of a unique random string.";
		Config.enableKnowsNames = prop.getBoolean(false);
		
		prop = configFile.get( "general", "bioLock_InternalMemorySize", 16);
		prop.comment = "Max number of prints that can be stored in BioLock's internal memory";
		Config.internalMemorySize=prop.getInt(16);
		
		configFile.save();
	}

	@Init
	public void load(FMLInitializationEvent event)
	{				
		bioLock=new BlockBioLock(Config.bioLockBlockID).setUnlocalizedName("BioLockBlock");
		
		GameRegistry.registerBlock(bioLock,ItemBlockProgrammable.class,"blockBioLock");
		GameRegistry.registerTileEntity(TileEntityBioLock.class, "BioLockPeripheral"); 
		GameRegistry.addRecipe(new ItemStack(bioLock), new Object[] { "SGS","SRS","SGS",'S',Block.stone,'R',Item.redstone,'G',Block.thinGlass});
		GameRegistry.addRecipe(new RecipeResetProgrammable(bioLock));
		
		LanguageRegistry.addName(bioLock,"Biometric Lock");

		prb=new BlockPRB(Config.prbBlockID).setUnlocalizedName("PRB");
		
		GameRegistry.registerBlock(prb,ItemBlockProgrammable.class,"blockPRB");
		GameRegistry.registerTileEntity(TileEntityPRB.class, "PRBPeripheral");
		GameRegistry.addRecipe(new ItemStack(prb),new Object[] { "SRS","RBR","SRS",'S',Block.stone,'R',Item.redstone,'B',Block.blockRedstone});		
		GameRegistry.addRecipe(new RecipeResetProgrammable(prb));
		
		LanguageRegistry.addName(prb,"PRB");
		
		keypadLock=new BlockKeypadLock(Config.keypadLockBlockID).setUnlocalizedName("KeypadLockBlock");
		
		GameRegistry.registerBlock(keypadLock,ItemBlockProgrammable.class,"blockKeypadLockBlock");
		GameRegistry.registerTileEntity(TileEntityKeypadLock.class,"KeypadLockPeripheral");
		GameRegistry.addRecipe(new ItemStack(keypadLock), new Object[] { "BBB", "BBB", "BBB", 'B', Block.stoneButton});
		GameRegistry.addRecipe(new RecipeResetProgrammable(keypadLock));
		
		LanguageRegistry.addName(keypadLock,"Keypad Lock");
		
		proxy.registerRenderInformation();		
	}	
	
	@ServerStarted
	public void serverStarted(FMLServerStartedEvent event)
	{
	}

	
	
	
}
