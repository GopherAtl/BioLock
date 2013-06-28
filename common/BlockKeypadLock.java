package gopheratl.biolock.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockKeypadLock extends BlockProgrammable {

	static Icon tempIcon;
	
	public BlockKeypadLock(int id)
	{
		super(id, TileEntityKeypadLock.class);
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		tempIcon=iconRegister.registerIcon("BioLock:biolock_side");
				
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
		System.out.println("[BioLock] [DEBUG] pos="+x+","+y+","+z);
        return tempIcon;
    }

    //called when rendering as block in inventory
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int par2)
    {
        return tempIcon;
    }
    
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		return false;
		/*
		if (side>1)
		{
			int mx=x, my=y, mz=z;
		    switch(side)
			{
  			case 2: mz++; break;
			case 3: mz--; break;
			case 4: mx++; break;
			case 5: mx--; break;
			}			

		    int facing=blockAccess.getBlockMetadata(mx, my, mz);
			if (facing==side)
				return false;			
		}
			
		return super.shouldSideBeRendered(blockAccess,x,y,z,side);
		/**/
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
}
