package icbm.zhapin.po;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.DamageSource;
import universalelectricity.prefab.potion.CustomPotion;

public class PDaDu extends CustomPotion
{
	public static final PDaDu INSTANCE = new PDaDu(21, true, 5149489, "Toxin");

	public PDaDu(int id, boolean isBadEffect, int color, String name)
	{
		super(id, isBadEffect, color, name);
		this.setIconIndex(6, 0);
	}

	@Override
	public void performEffect(EntityLiving par1EntityLiving, int amplifier)
	{
		if (!(par1EntityLiving instanceof EntityZombie) && !(par1EntityLiving instanceof EntityPigZombie))
		{
			par1EntityLiving.attackEntityFrom(DamageSource.magic, 1);
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier)
	{
		if (duration % (20 * 2) == 0)
		{
			return true;
		}

		return false;
	}
}