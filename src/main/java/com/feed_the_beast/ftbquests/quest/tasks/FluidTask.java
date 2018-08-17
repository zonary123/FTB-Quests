package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigFluid;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigLong;
import com.feed_the_beast.ftblib.lib.config.ConfigNBT;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class FluidTask extends QuestTask
{
	public static final String ID = "fluid";
	private static final ResourceLocation TANK_TEXTURE = new ResourceLocation(FTBQuests.MOD_ID, "textures/tasks/tank.png");

	public final ConfigFluid fluid;
	public final ConfigNBT fluidNBT;
	public final ConfigLong amount;

	public FluidTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);

		fluid = new ConfigFluid(FluidRegistry.getFluid(nbt.getString("fluid")), FluidRegistry.WATER);
		fluidNBT = new ConfigNBT(nbt.hasKey("nbt") ? nbt.getCompoundTag("nbt") : null);
		amount = new ConfigLong(nbt.hasKey("amount") ? nbt.getLong("amount") : 1000, 1, Long.MAX_VALUE);
	}

	@Override
	public long getMaxProgress()
	{
		return amount.getLong();
	}

	@Override
	public String getMaxProgressString()
	{
		return getVolumeString(amount.getLong());
	}

	@Override
	public String getName()
	{
		return ID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("type", "fluid");
		nbt.setString("fluid", fluid.getString());

		if (amount.getLong() != 1000)
		{
			nbt.setLong("amount", amount.getLong());
		}

		if (fluidNBT.getNBT() != null)
		{
			nbt.setTag("nbt", fluidNBT.getNBT());
		}
	}

	@Override
	public Icon getIcon()
	{
		FluidStack fluidStack = createFluidStack(Fluid.BUCKET_VOLUME);
		ItemStack stack = FluidUtil.getFilledBucket(fluidStack);

		if (stack.isEmpty())
		{
			return Icon.getIcon(fluid.getFluid().getStill(fluidStack).toString());
		}
		else
		{
			return ItemIcon.getItemIcon(stack);
		}
	}

	public FluidStack createFluidStack(int amount)
	{
		return new FluidStack(fluid.getFluid(), amount, fluidNBT.getNBT());
	}

	public static String getVolumeString(long a)
	{
		StringBuilder builder = new StringBuilder();

		if (a >= Fluid.BUCKET_VOLUME)
		{
			if (a % Fluid.BUCKET_VOLUME != 0L)
			{
				builder.append(a / (double) Fluid.BUCKET_VOLUME);
			}
			else
			{
				builder.append(a / Fluid.BUCKET_VOLUME);
			}
		}
		else
		{
			builder.append(a % Fluid.BUCKET_VOLUME);
		}

		builder.append(' ');

		if (a < Fluid.BUCKET_VOLUME)
		{
			builder.append('m');
		}

		builder.append('B');
		return builder.toString();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(getVolumeString(amount.getLong())).appendText(" of ").appendSibling(fluid.getStringForGUI());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("fluid", fluid, new ConfigFluid(FluidRegistry.WATER, FluidRegistry.WATER));
		group.add("fluid_nbt", fluidNBT, new ConfigNBT(null));
		group.add("amount", amount, new ConfigInt(1));
	}

	@Override
	public boolean canInsertItem()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderOnScreen(@Nullable QuestTaskData data)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		ClientUtils.MC.getTextureManager().bindTexture(TANK_TEXTURE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		double x = -0.5;
		double y = -0.5 - 1D / 32D;
		double w = 1;
		double h = 33D / 32D;
		double z = 0;
		buffer.pos(x, y + h, z).tex(0, 1).endVertex();
		buffer.pos(x + w, y + h, z).tex(1, 1).endVertex();
		buffer.pos(x + w, y, z).tex(1, 0).endVertex();
		buffer.pos(x, y, z).tex(0, 0).endVertex();
		tessellator.draw();

		if (data != null && getMaxProgress() > 0L && data.getProgress() > 0L)
		{
			double r = data.getRelativeProgress();
			x += 1D / 128D;
			w -= 1D / 64D;

			h = r * 31D / 32D;
			y = 1D - h;

			h -= 1D / 64D;
			y -= 0.5D;
			z = 0.003D;

			FluidStack stack = createFluidStack(Fluid.BUCKET_VOLUME);
			TextureAtlasSprite sprite = ClientUtils.MC.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill(stack).toString());
			int color = stack.getFluid().getColor(stack);
			int alpha = (color >> 24) & 0xFF;
			int red = (color >> 16) & 0xFF;
			int green = (color >> 8) & 0xFF;
			int blue = color & 0xFF;
			double u0 = sprite.getMinU();
			double v0 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (1D - r);
			double u1 = sprite.getMaxU();
			double v1 = sprite.getMaxV();

			ClientUtils.MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			buffer.pos(x, y + h, z).tex(u0, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y + h, z).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x + w, y, z).tex(u1, v0).color(red, green, blue, alpha).endVertex();
			buffer.pos(x, y, z).tex(u0, v0).color(red, green, blue, alpha).endVertex();
			tessellator.draw();
			ClientUtils.MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
	}

	@Override
	public QuestTaskData createData(IProgressData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<FluidTask> implements IFluidHandler, IItemHandler
	{
		private final IFluidTankProperties[] properties;

		private Data(FluidTask t, IProgressData data)
		{
			super(t, data);
			properties = new IFluidTankProperties[1];
			properties[0] = new FluidTankProperties(task.createFluidStack(0), 0, true, false);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
		{
			return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public String getProgressString()
		{
			return getVolumeString(progress);
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return properties;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			FluidStack fluidStack = task.createFluidStack(Fluid.BUCKET_VOLUME);

			if (progress < task.amount.getLong() && fluidStack.isFluidEqual(resource))
			{
				long add = Math.min(100000000000L, Math.min(resource.amount, task.amount.getLong() - progress));

				if (add > 0)
				{
					if (doFill)
					{
						progress += add;
						data.syncTask(this);
					}

					return (int) add;
				}
			}

			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			return null;
		}

		@Override
		public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate)
		{
			if (stack.getCount() != 1)
			{
				return stack;
			}

			IFluidHandlerItem item = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			if (item != null)
			{
				int drain = (int) Math.min(1000000000, task.amount.getLong() - progress);

				if (singleItem && drain > 1000)
				{
					drain = 1000;
				}

				FluidStack fluidStack = item.drain(task.createFluidStack(drain), false);

				if (fluidStack == null || fluidStack.amount <= 0)
				{
					return stack;
				}

				if (!simulate)
				{
					item.drain(fluidStack, true);
					progress += fluidStack.amount;
					data.syncTask(this);
				}

				return item.getContainer();
			}

			return stack;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}
	}
}