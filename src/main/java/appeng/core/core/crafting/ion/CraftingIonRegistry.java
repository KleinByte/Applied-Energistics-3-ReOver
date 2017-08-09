package appeng.core.core.crafting.ion;

import appeng.core.AppEng;
import appeng.core.core.AppEngCore;
import appeng.core.core.api.crafting.ion.*;
import appeng.core.core.api.crafting.ion.IonEnvironment;
import appeng.core.core.api.definitions.ICoreIonDefinitions;
import appeng.core.lib.capability.SingleCapabilityProvider;
import code.elix_x.excomms.color.RGBA;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.reflect.InheritanceUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CraftingIonRegistry {

	public CraftingIonRegistry(){
		MinecraftForge.EVENT_BUS.register(this);
	}

	private ICoreIonDefinitions ionDefinitions(){
		return AppEngCore.INSTANCE.definitions(Ion.class);
	}

	@SubscribeEvent
	public void attachProviderCapsToVanilla(AttachCapabilitiesEvent<ItemStack> event){
		Item item = event.getObject().getItem();
		MutableObject<IonProvider> ionProvider = new MutableObject<>();
		if(item == Items.QUARTZ) ionDefinitions().quartz().maybe().ifPresent(ion -> ionProvider.setValue(new IonProviderImpl(ion, 1)));
		else if(item == Items.REDSTONE) ionDefinitions().redstone().maybe().ifPresent(ion -> ionProvider.setValue(new IonProviderImpl(ion, 1)));
		else if(item == Items.GUNPOWDER) ionDefinitions().sulfur().maybe().ifPresent(ion -> ionProvider.setValue(new IonProviderImpl(ion, 1)));
		else if(item == Items.ENDER_PEARL) ionDefinitions().ender().maybe().ifPresent(ion -> ionProvider.setValue(new IonProviderImpl(ion, 1)));
		if(ionProvider.getValue() != null) event.addCapability(new ResourceLocation(AppEng.MODID, "ion_provider"), new SingleCapabilityProvider<>(AppEngCore.ionProviderCapability, ionProvider.getValue()));
	}

	public BiMap<Fluid, Fluid> normal2ionized = HashBiMap.create();
	public BiMap<Fluid, Fluid> ionized2normal = normal2ionized.inverse();

	public void registerEnvironmentFluid(@Nonnull Fluid fluid){
		normal2ionized.put(fluid, fluid);
	}

	public void registerIonVariant(Fluid original, Fluid ionized){
		normal2ionized.put(original, ionized);
	}

	public void onIonEntityItemTick(EntityItem item, IonProvider ionProvider){
		//TODO Expand to bounding box collision logic
		World world = item.world;
		BlockPos pos = new BlockPos(item.posX, item.posY, item.posZ);
		IBlockState block = world.getBlockState(pos);
		Fluid fluid = FluidRegistry.lookupFluidForBlock(block.getBlock());
		if(normal2ionized.containsKey(fluid) && ionProvider.isReactive(fluid)){
			Fluid ionized = normal2ionized.get(fluid);
			world.setBlockToAir(pos);
			FluidUtil.tryPlaceFluid(null, world, pos, new FluidTank(ionized, Fluid.BUCKET_VOLUME, Fluid.BUCKET_VOLUME), new FluidStack(ionized, Fluid.BUCKET_VOLUME));
			world.getTileEntity(pos).getCapability(AppEngCore.ionEnvironmentCapability, null).addIons(ionProvider);
			world.markBlockRangeForRenderUpdate(pos, pos);
			item.setDead();
		}
	}

	public void onIonEntityItemEnterEnvironment(World world, BlockPos pos, EntityItem item, IonProvider ionProvider){
		IBlockState block = world.getBlockState(pos);
		Fluid fluid = FluidRegistry.lookupFluidForBlock(block.getBlock());
		if(ionized2normal.containsKey(fluid)){
			world.getTileEntity(pos).getCapability(AppEngCore.ionEnvironmentCapability, null).addIons(ionProvider);
			world.markBlockRangeForRenderUpdate(pos, pos);
			item.setDead();
		}
	}

	public RGBA getColor(IonEnvironment environment, RGBA original){
		/*MutableObject<RGBA> color = new MutableObject<>(original);
		environment.getIons().forEach(ion -> color.setValue(blend(color.getValue(), ion.getColorModifier(), amount2mul(environment.getAmount(ion)))));
		return color.getValue();*/
		Set<RGBA> colors = new HashSet<>();
		float aSum = 0.1f + (float) environment.getIons().values().stream().mapToDouble(this::amount2mul).sum();
		colors.add(new RGBA(original.getRF(), original.getGF(), original.getBF(), 0.1f * original.getAF() / aSum));
		environment.getIons().forEach((ion, amount) -> colors.add(new RGBA(ion.getColorModifier().getRF(), ion.getColorModifier().getGF(), ion.getColorModifier().getBF(), amount2mul(amount) / aSum)));
		return blend(colors);
	}

	public RGBA blend(Set<RGBA> colors){
		double r = colors.stream().mapToDouble(color -> color.getRF() * color.getAF()).sum();
		double g = colors.stream().mapToDouble(color -> color.getGF() * color.getAF()).sum();
		double b = colors.stream().mapToDouble(color -> color.getBF() * color.getAF()).sum();
		return new RGBA((float) r, (float) g, (float) b, 1f);
	}

	public float amount2mul(int amount){
		return -1f/(2*amount) + 1f;
	}

	public Map<Class, IonEnvironmentProductConsumer> consumers = new HashMap<>();

	public <T> void registerProductConsumer(Class<T> type, IonEnvironmentProductConsumer<T> consumer){
		consumers.put(type, consumer);
	}

	public List<Pair<Class, Consumer>> compileProductConsumersL(IonEnvironmentContext context){
		return consumers.entrySet().stream().map(entry -> new ImmutablePair<>(entry.getKey(), entry.getValue().createConsumer(context))).collect(Collectors.toList());
	}

	public Function<Class, Optional<Consumer>> compileProductConsumersF(IonEnvironmentContext context){
		List<Pair<Class, Consumer>> consumers = compileProductConsumersL(context);
		return clas -> consumers.stream().filter(consumer -> consumer.getLeft().isAssignableFrom(clas)).sorted(Comparator.comparingInt(consumer -> InheritanceUtils.distance(clas, consumer.getLeft()))).findFirst().map(Pair::getRight);
	}

	public Consumer compileProductConsumersC(IonEnvironmentContext context){
		Function<Class, Optional<Consumer>> consumers = compileProductConsumersF(context);
		return product -> consumers.apply(product.getClass()).ifPresent(consumer -> consumer.accept(product));
	}

}
