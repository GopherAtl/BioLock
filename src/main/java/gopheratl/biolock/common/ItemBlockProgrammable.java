package gopheratl.biolock.common;

import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.common.util.BLLog;
import gopheratl.biolock.server.ProxyBioLockServer;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemBlockProgrammable extends ItemBlock {

	public ItemBlockProgrammable(Block block)
	{
		super(block);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
	
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
		boolean result=super.placeBlockAt(stack, player, world, x, y, z, side, hitX,hitY, hitZ, metadata);
		if (result && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
        {
        	TileEntity te=world.getTileEntity(x,y,z);
        	if (te==null)
        		BLLog.debug("[PRB] TE is null!");
        	
        	if (te instanceof TileEntityProgrammable)
        	{
        		TileEntityProgrammable tep=(TileEntityProgrammable)te;
        		if (stack.getItemDamage()!=0)
        			tep.setInstanceID(stack.getItemDamage());
        		else
        		{
        			InstanceDataManager instanceManager=BioLock.getInstanceManager(te.getClass());
        			if (instanceManager==null)
        				BLLog.debug("[PRB] instanceManager is null!");
        			else
        				tep.setInstanceID(instanceManager.getNextID());
        		}
        	}
        }
        return result;
    }

	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tipList, boolean par4)
    {
		int dmg=itemStack.getItemDamage();
		if (dmg>0)
		{
			tipList.add("#"+dmg);
		}
    }


}
