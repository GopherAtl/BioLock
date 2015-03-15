package gopheratl.biolock.common;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class ItemBlockBioLock extends ItemBlockProgrammable 
{
	public ItemBlockBioLock(Block block)
	{		
		super(block);
	}
}
