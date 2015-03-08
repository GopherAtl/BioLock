package gopheratl.biolock.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.ProxyBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;

public class ProxyBioLockClient extends ProxyBioLock 
{
	
	@Override
    public void registerRenderInformation()
    {
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
		
		ClientRegistry.bindTileEntitySpecialRenderer( TileEntityKeypadLock.class, new TileEntityRendererKeypad());
    }
	
}
