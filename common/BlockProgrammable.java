package gopheratl.biolock.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

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
		this.setStepSound(soundStoneFootstep);
		this.setLightValue(.25F);
    }

	public BlockProgrammable(int id, Class teClass, Material mat)
	{
		super(id,mat);
		this.teClass=teClass;
		initCommonStuff();
	}
	
	public BlockProgrammable(int id, Class teClass)
	{
		super(id,Material.iron);
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
    public TileEntity createNewTileEntity(World world)
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
    public void breakBlock(World world, int x, int y, int z, int blockID, int metadata)
    {    	
    	//grab a ref to my tile entity while I still can
        TileEntityProgrammable tep=(TileEntityProgrammable)world.getBlockTileEntity(x,y,z);            
    	
        super.breakBlock(world, x, y, z, blockID, metadata);
    	
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
            EntityItem dropItem = new EntityItem(world, x + xOff, y + yOff, z + zOff, new ItemStack(this.blockID,1,tep==null?0:tep.getInstanceID()));
            dropItem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(dropItem);
        }
    }

    
    public boolean isBlockNormalCube(World world, int x, int y, int z)
    {
    	return false;
    }
    
    public boolean isBlockSolidOnSide(World world, int i, int j, int k, ForgeDirection side)
    {
      return true;
    }

    public boolean canProvidePower()
    {
        return true;
    }
    
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	return isProvidingWeakPower(blockAccess,x,y,z,side);
    }
    
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	boolean res;
    	TileEntityProgrammable tep=(TileEntityProgrammable)blockAccess.getBlockTileEntity(x,y,z);    	
		//System.out.println("[BioLock] [DEBUG] isProvidingWeakPower "+tep.getSideOutput(side));
    	return tep.getSideOutput(side^1);
    }
     
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
            int southBlock = world.getBlockId(x, y, z + 1);
            int northBlock = world.getBlockId(x, y, z - 1);
            int westBlock = world.getBlockId(x - 1, y, z);
            int eastBlock = world.getBlockId(x + 1, y, z);
            byte dir = 3;
            
            if (Block.opaqueCubeLookup[northBlock] && !Block.opaqueCubeLookup[southBlock])            
                dir = 2;
            else if (Block.opaqueCubeLookup[southBlock] && !Block.opaqueCubeLookup[northBlock])            
                dir = 3;            
            else if (Block.opaqueCubeLookup[westBlock] && !Block.opaqueCubeLookup[eastBlock])          
                dir = 4;            
            else if (Block.opaqueCubeLookup[eastBlock] && !Block.opaqueCubeLookup[westBlock])            
                dir = 5;            

            world.setBlockMetadataWithNotify(x, y, z, dir,3);
        }    
    }

    //replace default direction with a proper one when we can
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack par6ItemStack)
    {
        int facing = MathHelper.floor_double((double)(entity.rotationYaw / 90.0f) + 0.5D) & 3;
        float angle=facing*-90f;
        world.setBlockMetadataWithNotify(x,y,z,facingToSideMap[facing],7);
    }
    
    
}
