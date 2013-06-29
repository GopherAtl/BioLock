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
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		System.out.println("[BioLock] [DEBUG] Activate with hit at "+hitX+","+hitY+","+hitZ);
		if (player.isSneaking())
			return false;
		
		//if it wasn't the face, false
		int facing=world.getBlockMetadata(x, y, z);
		if (facing!=side)
		{
			System.out.println("[BioLock] [DEBUG] wrong side.");
			return false;
		}
		System.out.println("[BioLock] [DEBUG] side="+side);
		float relX=0f,relY=hitY*16f;
		//normalize face-relative "x" pixel position
		switch(facing)
		{
		case 2: relX=hitX*16f; break;
		case 3: relX=(1f-hitX)*16f; break;
		case 4: relX=(1f-hitZ)*16f; break;
		case 5: relX=hitZ*16f; break;		
		}
		
		//figure out what, if any, button was hit?
		if (relX<4f || relX>12 || relY<2.5f || relY>13.5f)
		{
			System.out.println("[BioLock] [DEBUG] outside button area.");			
			//completely outside area of buttons, return
			return false;
		}
		int col=(int)((relX-4f)/3f);
		float colOff=(relX-4f)%3f;
		int row=(int)((relY-2.5f)/3f);
		float rowOff=(relY-2.5f)%3f;
		//check and return if between buttons
		if (colOff>2f || rowOff>2f)
		{
			System.out.println("[BioLock] [DEBUG] between buttons.");
			return false;
		}		
		
		//ok! hit a button!
		System.out.println("[BioLock] [DEBUG] Hit button on row "+row+" in col "+col);
		TileEntityKeypadLock te=(TileEntityKeypadLock)world.getBlockTileEntity(x,y,z);
		te.pressedButton(player,(2-col)+3*(3-row));
		return true;
	
	}
}
