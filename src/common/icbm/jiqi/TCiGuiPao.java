package icbm.jiqi;

import icbm.ICBMCommonProxy;
import icbm.ItZiDan;
import icbm.ParticleSpawner;
import icbm.ZhuYao;
import icbm.zhapin.EZhaPin;
import icbm.zhapin.ex.ExHongSu;

import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import universalelectricity.BasicComponents;
import universalelectricity.Ticker;
import universalelectricity.basiccomponents.multiblock.IMultiBlock;
import universalelectricity.basiccomponents.multiblock.TileEntityMulti;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IJouleStorage;
import universalelectricity.implement.IRedstoneReceptor;
import universalelectricity.network.IPacketReceiver;
import universalelectricity.network.PacketManager;
import universalelectricity.prefab.TileEntityElectricityReceiver;
import universalelectricity.prefab.Vector3;

import com.google.common.io.ByteArrayDataInput;

public class TCiGuiPao extends TileEntityElectricityReceiver implements IJouleStorage, IPacketReceiver, IRedstoneReceptor, IMultiBlock, ISidedInventory
{	
	public float rotationYaw = 0;
	public float rotationPitch = 0;
	
	public float displayRotationYaw = 0;
	public float displayRotationPitch = 0;

	private float rotationSpeed;
	
    private double wattHourStored = 0;
	
	private EntityPlayer mountedPlayer = null;

	private EFake entityFake = null;
	
	private int gunChargingTicks = 0;
	
    private ItemStack[] containingItems = new ItemStack[4];
    
	private boolean redstonePowerOn = false;

	private boolean isAntimatter;

	private float explosionSize;

	private int explosionDepth;
	
	private int shiYongZhe = 0;
	private boolean packetGengXin = true;
	    
    public TCiGuiPao()
    {
		super();
    }
    
	@Override
	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side)
	{		
		if(!this.isDisabled())
    	{
			this.setJoules(this.wattHourStored+ElectricInfo.getJoules(amps, voltage));
    	}
	}
	
	public void updateEntity()
	{
		super.updateEntity();
		
		if(!this.isDisabled())
    	{
    		if(mountedPlayer != null)
    		{
    			if(mountedPlayer.rotationPitch > 30) mountedPlayer.rotationPitch = 30;
    			if(mountedPlayer.rotationPitch < -45) mountedPlayer.rotationPitch = -45;
    			
    			this.rotationPitch = mountedPlayer.rotationPitch;
    			this.rotationYaw = mountedPlayer.rotationYaw;
    			
    			this.displayRotationPitch = this.rotationPitch*0.0175f;
    			this.displayRotationYaw = this.rotationYaw*0.0175f;
    		}
    		else if(this.entityFake != null)
    		{
    			this.entityFake.setDead();
    			this.entityFake = null;
    		}
    		
    		if(this.redstonePowerOn && this.canFire() && this.gunChargingTicks == 0)
			{
				this.gunChargingTicks = 1;
				this.redstonePowerOn = false;
				this.isAntimatter = false;
				
				this.worldObj.playSoundEffect((int)this.xCoord, (int)this.yCoord, (int)this.zCoord, "icbm.railgun", 5F, 1F);
				
				for(int ii = 0; ii < this.containingItems.length; ii++)
				{
					ItemStack itemStack = this.containingItems[ii];
					
					if(itemStack != null)
					{
						if(itemStack.getItem() instanceof ItZiDan)
						{
							if(itemStack.getItemDamage() == 1)
							{
								this.isAntimatter = true;
							}
							
							itemStack.stackSize --;
							this.setInventorySlotContents(ii, itemStack);
							
							if(itemStack.stackSize == 0)
							{
								this.setInventorySlotContents(ii, null);
							}
							
							break;
						}		
					}
				}
				
				this.setJoules(0);
		        
		        this.explosionSize = 5f;
		        this.explosionDepth = 5;
		        
		        if(isAntimatter)
		        {
		        	explosionSize = 8f;
		        	explosionDepth = 10;
		        }
			}
    		
    		if(this.gunChargingTicks > 0)
    		{
    			this.gunChargingTicks ++;
    			
    			if(this.gunChargingTicks >= 70)
    			{
    				while(this.explosionDepth > 0)
    				{
        				MovingObjectPosition objectMouseOver = this.rayTrace(1500);
        				
        				if(objectMouseOver != null)
        		        {
        					if(objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        					{
	        					if(isAntimatter)
	        					{
		        					int radius = ExHongSu.BAN_JING;
		        					AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(objectMouseOver.blockX - radius, objectMouseOver.blockY - radius, objectMouseOver.blockZ - radius, objectMouseOver.blockX + radius, objectMouseOver.blockY + radius, objectMouseOver.blockZ + radius);
		        			        List<EZhaPin> missilesNearby = worldObj.getEntitiesWithinAABB(EZhaPin.class, bounds);
		
		        			        for(EZhaPin entity : missilesNearby)
		        			        {
		        			        	entity.endExplosion();
		        			        }
	        					}
        			        
	        			        if(this.worldObj.getBlockId(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ) != Block.bedrock.blockID)
	        			        {
	        			        	this.worldObj.setBlockWithNotify(objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ, 0);
	        			        }
	        			        
	        			        this.worldObj.newExplosion(mountedPlayer, objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ, explosionSize, true);
        					}
        		        }
        				
        				this.explosionDepth --;
    				}
    				
    				if(!this.worldObj.isRemote)
    				{
    					PacketManager.sendPacketToClients(PacketManager.getPacket(ZhuYao.CHANNEL, this, (int)3), this.worldObj, Vector3.get(this), 50);
    				}
    				
    				this.gunChargingTicks = 0;
    			}
    		}
    	}
	
		if(!this.worldObj.isRemote)
		{
			if(Ticker.inGameTicks % 20 == 0)
			{
				if(this.mountedPlayer != null)
				{
					this.worldObj.markBlockNeedsUpdate(this.xCoord, this.yCoord, this.zCoord);
					PacketManager.sendPacketToClients(PacketManager.getPacket(ZhuYao.CHANNEL, this, (int)1, this.wattHourStored, this.disabledTicks), this.worldObj, Vector3.get(this), 15);
				}
				
				if(this.shiYongZhe > 0)
				{
					PacketManager.sendPacketToClients(PacketManager.getPacket(ZhuYao.CHANNEL, this, (int)4, this.wattHourStored, this.disabledTicks), this.worldObj, Vector3.get(this), 15);
				}
			}
			
			if(this.packetGengXin)
			{
				this.worldObj.markBlockNeedsUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.packetGengXin = false;
			}
		}
	}
	
	@Override
	public void handlePacketData(NetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
	{
		try
	    {
	        int ID = dataStream.readInt();

	        if(ID == 1)
	        {
		        this.displayRotationYaw = dataStream.readFloat();
		        this.displayRotationPitch = dataStream.readFloat();
	        }
	        else if(ID == 2)
	        {
	        	this.mount(player);
	        }
	        else if(ID == 3)
	        {
	        	if(this.worldObj.isRemote)
				{
    				Vector3 muzzilePosition = this.getMuzzle();
    				ParticleSpawner.spawnParticle("smoke", this.worldObj, muzzilePosition, 0, 0, 0, 2f, 1.5f);
    				ParticleSpawner.spawnParticle("flame", this.worldObj, muzzilePosition, 0, 0, 0, 10f, 1.5f);
    				this.worldObj.spawnParticle("flame", muzzilePosition.x, muzzilePosition.y, muzzilePosition.z, 0, 0, 0);
				}
				
	        }
	        else if(ID == 4)
	        {
		        this.wattHourStored = dataStream.readDouble();
		        this.disabledTicks = dataStream.readInt();
	        }
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	}
	
	@Override
    public Packet getDescriptionPacket()
    {
        return PacketManager.getPacket(ZhuYao.CHANNEL, this, (int)1, this.displayRotationYaw, this.displayRotationPitch);
    }
	
	@Override
    public void openChest()
    {
    	if(!this.worldObj.isRemote)
        {
			PacketManager.sendPacketToClients(PacketManager.getPacket(ZhuYao.CHANNEL, this, (int)4, this.wattHourStored, this.disabledTicks), this.worldObj, Vector3.get(this), 15);
        }
    	
    	this.shiYongZhe  ++;
    }
    
    @Override
    public void closeChest()
    {
    	this.shiYongZhe --;
    }


	private boolean canFire()
	{
		for(int i = 0; i < this.containingItems.length; i++)
		{
			ItemStack itemStack = this.containingItems[i];
			
			if(itemStack != null)
			{
				if(itemStack.getItem() instanceof ItZiDan)
				{
					break;
				}		
			}
						
			if(i == this.containingItems.length-1)
			{
				return false;
			}
		}
		
		if(this.wattHourStored < this.getMaxJoules())
		{
			return false;
		}
		
		return true;
	}

	@Override
	public boolean canReceiveFromSide(ForgeDirection side)
	{
		return true;
	}

	@Override
	public double getVoltage()
	{
		return 220;
	}
	
	@Override
	public void onDestroy(TileEntity callingBlock)
	{
		this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord, this.zCoord, 0);
		this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord+1, this.zCoord, 0);
	}

	@Override
	public boolean onActivated(EntityPlayer entityPlayer)
	{
		if(this.mountedPlayer != null && entityPlayer == this.mountedPlayer)
		{
			this.mountedPlayer =  null;
			entityPlayer.mountEntity(null);
			
			if(this.entityFake != null)
			{
				this.entityFake.setDead();
				this.entityFake = null;
			}
			entityPlayer.moveEntity(0, 3, 0);
		}
		else
		{
			entityPlayer.openGui(ZhuYao.instance, ICBMCommonProxy.GUI_RAIL_GUN, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}
		
		return true;
	}
	
	public void mount(EntityPlayer entityPlayer)
	{
		//Creates a fake entity to be mounted on
		if(this.mountedPlayer == null)
		{
			if(!this.worldObj.isRemote)
			{
				this.entityFake  = new EFake(this.worldObj, new Vector3(this.xCoord+0.5D, this.yCoord, this.zCoord+0.5D), this);
				this.worldObj.spawnEntityInWorld(entityFake);
				entityPlayer.mountEntity(entityFake);
			}
			
			mountedPlayer = entityPlayer;
			entityPlayer.rotationYaw = 0;
			entityPlayer.rotationPitch = 0;
		}
	}

	@Override
	public void onCreate(Vector3 position)
	{
		this.worldObj.setBlockWithNotify(position.intX(), position.intY()+1, position.intZ(), BasicComponents.blockMulti.blockID);
		((TileEntityMulti)this.worldObj.getBlockTileEntity(position.intX(), position.intY()+1, position.intZ())).setMainBlock(position);
	}
	
	/**
     * Performs a ray trace for the distance specified and using the partial tick time. Args: distance, partialTickTime
     */
    public MovingObjectPosition rayTrace(double distance)
    {
        Vector3 muzzlePosition = getMuzzle();
        Vector3 lookDistance = ZhuYao.getLook(this.rotationYaw, this.rotationPitch);
        Vector3 var6 = Vector3.add(muzzlePosition, Vector3.multiply(lookDistance, distance));
        return this.worldObj.rayTraceBlocks(muzzlePosition.toVec3(), var6.toVec3());
    }
    
    public Vector3 getMuzzle()
    {
    	Vector3 position = new Vector3(this.xCoord+0.5, this.yCoord+1, this.zCoord+0.5);
    	return Vector3.add(position, Vector3.multiply(ZhuYao.getLook(this.rotationYaw, this.rotationPitch), 2.2));
    }

    @Override
    public int getSizeInventory()
    {
        return this.containingItems.length;
    }
    @Override
    public ItemStack getStackInSlot(int par1)
    {
        return this.containingItems[par1];
    }
    @Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.containingItems[par1] != null)
        {
            ItemStack var3;

            if (this.containingItems[par1].stackSize <= par2)
            {
                var3 = this.containingItems[par1];
                this.containingItems[par1] = null;
                return var3;
            }
            else
            {
                var3 = this.containingItems[par1].splitStack(par2);

                if (this.containingItems[par1].stackSize == 0)
                {
                    this.containingItems[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (this.containingItems[par1] != null)
        {
            ItemStack var2 = this.containingItems[par1];
            this.containingItems[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }
    @Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        this.containingItems[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }
    }
    @Override
    public String getInvName()
    {
        return "Railgun";
    }
    @Override
    public int getInventoryStackLimit()
    {
        return 16;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return true;
	}
	@Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
    	
        this.rotationYaw = par1NBTTagCompound.getFloat("rotationYaw");
        this.rotationPitch = par1NBTTagCompound.getFloat("rotationPitch");
        
        this.displayRotationPitch = this.rotationPitch*0.0175f;
		this.displayRotationYaw = this.rotationYaw*0.0175f;
    	
        this.wattHourStored = par1NBTTagCompound.getDouble("electricityStored");
        NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
        this.containingItems = new ItemStack[this.getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < this.containingItems.length)
            {
                this.containingItems[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
    }
    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        
        par1NBTTagCompound.setFloat("rotationYaw", this.rotationYaw);
        par1NBTTagCompound.setFloat("rotationPitch", this.rotationPitch);
        
        par1NBTTagCompound.setDouble("electricityStored", this.wattHourStored);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < this.containingItems.length; ++var3)
        {
            if (this.containingItems[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.containingItems[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        par1NBTTagCompound.setTag("Items", var2);
    }

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		for(int i = 0; i < this.containingItems.length; i++)
		{
			ItemStack stack = this.containingItems[i];
		
			if(stack.stackSize < stack.getMaxStackSize())
			{
				return i;
			}
		}
		
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return this.containingItems.length;
	}

	@Override
	public void onPowerOn() { this.redstonePowerOn = true;}

	@Override
	public void onPowerOff() {this.redstonePowerOn = false;}
	
	@Override
    public double wattRequest()
    {
        if (!this.isDisabled())
        {
            return Math.ceil(ElectricInfo.getWatts(this.getMaxJoules()) - ElectricInfo.getWatts(this.wattHourStored));
        }

        return 0;
    }
	
	@Override
	public double getJoules(Object... data)
	{
		return this.wattHourStored;
	}

	@Override
	public void setJoules(double wattHours, Object... data)
	{
		this.wattHourStored = Math.max(Math.min(wattHours, this.getMaxJoules()), 0);
	}

	@Override
	public double getMaxJoules()
	{
		return 300;
	}
}