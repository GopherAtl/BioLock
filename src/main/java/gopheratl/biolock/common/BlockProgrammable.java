package gopheratl.biolock.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class BlockProgrammable extends BlockContainer {

	//2==north, 3==west, 4=east, 5=south, 
	static int[] facingToSideMap={2,5,3,4};

	private Class teClass;
	
	public Class getTEClass()
	{
		return teClass;
	}

	
	private void initCommonStuff()
	{
		this.blockHardness=.75F;
		this.blockResistance=15.0F;		
		this.setStepSound(soundTypeStone);
		this.setLightLevel(.25F);
    }

	public BlockProgrammable(Class teClass, Material mat)
	{
		super(mat);
		this.teClass=teClass;
		initCommonStuff();
	}
	
	public BlockProgrammable(Class teClass)
	{
		super(Material.iron);
		this.teClass=teClass;
		initCommonStuff();
	}

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }
    
    @Override 
    public TileEntity createNewTileEntity(World world, int metadata)
    {
    	try {
    		return (TileEntity)teClass.newInstance();
    	} catch (InstantiationException e) {
			System.out.println("[BioLock] InstantiationException - Failed instantiating TE \""+teClass.getName()+"\"!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("[BioLock] IllegalAccessException - Failed instantiating TE \""+teClass.getName()+"\"!");
			e.printStackTrace();
		}
    	finally { }
    	
    	return null;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
    {    	
    	//grab a ref to my tile entity while I still can
        TileEntityProgrammable tep=(TileEntityProgrammable)world.getTileEntity(x,y,z);            
    	
        super.breakBlock(world, x, y, z, block, metadata);
    	
        if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
        {
    		//how wide an area the random drops will drop in
            float rndRange = 0.7F;
            //blocks are 1 wide, calc half padding to center rndRange 
            float centerOffset = (1.0F-rndRange)*.5F;
            //pick a random spit in range adjusted by center
            float xOff = world.rand.nextFloat() * rndRange + centerOffset;
            float yOff = world.rand.nextFloat() * rndRange + centerOffset;
            float zOff = world.rand.nextFloat() * rndRange + centerOffset;
            //aaand drop!
            EntityItem dropItem = new EntityItem(world, x + xOff, y + yOff, z + zOff, new ItemStack(block,1,tep==null?0:tep.getInstanceID()));
            dropItem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(dropItem);
        }
    }

    
    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
    {
    	return false;
    }
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int i, int j, int k, ForgeDirection side)
    {
      return true;
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }
    
    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	return isProvidingWeakPower(blockAccess,x,y,z,side);
    }
    
    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	boolean res;
    	TileEntityProgrammable tep=(TileEntityProgrammable)blockAccess.getTileEntity(x,y,z);    	
		//System.out.println("[BioLock] [DEBUG] isProvidingWeakPower "+tep.getSideOutput(side));
    	return tep.getSideOutput(side^1);
    }
    
    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world,x,y,z);
        this.setDefaultDirection(world,x,y,z);
    }

    //default direction to south, west, north, or east, in that order
    private void setDefaultDirection(World world, int x, int y, int z)
    {
        if (!world.isRemote)
        {
            Block southBlock = world.getBlock(x, y, z + 1);
            Block northBlock = world.getBlock(x, y, z - 1);
            Block westBlock = world.getBlock(x - 1, y, z);
            Block eastBlock = world.getBlock(x + 1, y, z);
            byte dir = 3;
            
            if (northBlock.isOpaqueCube() && !southBlock.isOpaqueCube())            
                dir = 2;
            else if (southBlock.isOpaqueCube() && !northBlock.isOpaqueCube())            
                dir = 3;            
            else if (westBlock.isOpaqueCube() && !eastBlock.isOpaqueCube())          
                dir = 4;            
            else if (eastBlock.isOpaqueCube() && !westBlock.isOpaqueCube())            
                dir = 5;            

            world.setBlockMetadataWithNotify(x, y, z, dir,3);
        }    
    }

    //replace default direction with a proper one when we can
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack par6ItemStack)
    {
        int facing = MathHelper.floor_double((double)(entity.rotationYaw / 90.0f) + 0.5D) & 3;
        float angle=facing*-90f;
        //System.out.println("[BioLock] onBlockPlacedBy, facing="+facing+", to metadata = "+facingToSideMap[facing]);
        world.setBlockMetadataWithNotify(x,y,z,facingToSideMap[facing],7);
    }
    
    
}
