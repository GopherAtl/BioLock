package gopheratl.biolock.client;

import java.io.File;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.ProxyBioLock;
import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.util.BLLog;

public class ProxyBioLockClient extends ProxyBioLock 
{
	
	@Override
	public File getBase() {
		FMLClientHandler.instance().getClient();
		return Minecraft.getMinecraft().mcDataDir;
	}
	
	@Override
    protected void registerRenderInformation()
    {
		BLLog.debug("Registering rendering information");
		CreativeTabs computerTab=CreativeTabs.tabAllSearch;
		CreativeTabs[] tabs=CreativeTabs.creativeTabArray;
		for(int i=0; i<tabs.length; ++i)
		{
			if (tabs[i].getTabLabel()=="ComputerCraft")
			{
				computerTab=tabs[i];
				break;
			}			
		}
						
		BioLock.Blocks.bioLock.setCreativeTab(computerTab);		
		BioLock.Blocks.prb.setCreativeTab(computerTab);		
		BioLock.Blocks.keypadLock.setCreativeTab(computerTab);
		
		TileEntityRendererKeypad terk = new TileEntityRendererKeypad();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypadLock.class, terk);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBioLock.class, new TileEntityRendererBiolock());
		MinecraftForgeClient.registerItemRenderer(new ItemStack(BioLock.Blocks.keypadLock).getItem(), terk);
    }
	
	@Override
	public World getWorld(int dimId) {
		World world = Minecraft.getMinecraft().theWorld;
		if (world.provider.dimensionId == dimId) {
			return world;
		}
		return null;
	}
}
