package tterrag.core.common.enchant;

import java.lang.reflect.Method;
import java.util.Map;

import lombok.SneakyThrows;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import tterrag.core.TTCore;
import tterrag.core.api.common.enchant.IAdvancedEnchant;
import tterrag.core.common.Handlers.Handler;
import tterrag.core.common.Handlers.Handler.HandlerType;
import tterrag.core.common.config.ConfigHandler;
import tterrag.core.common.util.Scheduler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Handler(HandlerType.FORGE)
public class EnchantXPBoost extends Enchantment implements IAdvancedEnchant
{
    public static final EnchantXPBoost INSTANCE = new EnchantXPBoost(ConfigHandler.enchantIDXPBoost);
    
    private EnchantXPBoost(int id)
    {
        super(id, 2, EnumEnchantmentType.weapon);
    }

    @Override
    public int getMaxEnchantability(int level)
    {
        return super.getMaxEnchantability(level) + 30;
    }

    @Override
    public int getMinEnchantability(int level)
    {
        return super.getMinEnchantability(level);
    }

    @Override
    public int getMaxLevel()
    {
        return 3;
    }

    @Override
    public String getName()
    {
        return "enchantment.xpboost";
    }
    
    @Override
    public boolean isAllowedOnBooks()
    {
        return ConfigHandler.allowXPBoost;
    }

    @Override
    public String[] getTooltipDetails(ItemStack stack)
    {
        return new String[] { TTCore.lang.localize("enchantment.xpboost.tooltip", false) };
    }
    
    private static final Method getExperiencePoints = ReflectionHelper.findMethod(EntityLivingBase.class, null, new String[] {"e", "func_70693_a", "getExperiencePoints"}, EntityPlayer.class);
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    @SneakyThrows
    public void onLivingDeath(final LivingDeathEvent event)
    {
        final EntityLivingBase entity = event.entityLiving;
        Entity possiblePlayer = event.source.getSourceOfDamage();
        
        if (!entity.worldObj.isRemote && possiblePlayer != null && possiblePlayer instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) possiblePlayer;
            ItemStack held = player.getCurrentEquippedItem();
            
            if (held != null)
            {
                Map<Integer, Integer> enchants = EnchantmentHelper.getEnchantments(held);
                for (int i : enchants.keySet())
                {
                    Enchantment enchant = Enchantment.enchantmentsList[i];
                    int level = enchants.get(i);
                    
                    if (enchant == EnchantXPBoost.INSTANCE)
                    {
                        int xp = (Integer) getExperiencePoints.invoke(entity, player);

                        final int boost = Math.round(xp * ((float) Math.log10(level + 1) * 2));

                        Scheduler.instance().schedule(20, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                entity.worldObj.spawnEntityInWorld(new EntityXPOrb(entity.worldObj, entity.posX, entity.posY, entity.posZ, boost));
                            }
                        });

                        break; // save a bit of processing, not needed
                    }
                }
            }
        }
    }
    
    public void register()
    {
        if (ConfigHandler.allowXPBoost)
        {
            FMLInterModComms.sendMessage("EnderIO", "recipe:enchanter",
                    "<enchantment name=\"enchantment.xpboost\" costPerLevel=\"4\">\n<itemStack oreDictionary=\"ingotGold\" number=\"16\"/>\n</enchantment>"
            );
        }
        else
        {
            Enchantment.enchantmentsList[this.effectId] = null;
        }
    }
}
