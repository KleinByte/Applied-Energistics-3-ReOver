package appeng.core.skyfall.skyobject;

import appeng.core.AppEng;
import appeng.core.lib.capability.SingleCapabilityProvider;
import appeng.core.skyfall.AppEngSkyfall;
import appeng.core.skyfall.api.skyobject.Skyobject;
import appeng.core.skyfall.api.skyobject.SkyobjectProvider;
import appeng.core.skyfall.api.skyobject.SkyobjectsManager;
import appeng.core.skyfall.net.SkyobjectSpawnMessage;
import appeng.core.skyfall.net.SkyobjectsSyncMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = AppEng.MODID)
public class SkyobjectsManagerImpl implements SkyobjectsManager {

	protected static final ExecutorService GENERATORSERVICE = Executors.newCachedThreadPool();

	@SubscribeEvent
	public static void attachToWorld(AttachCapabilitiesEvent<World> event){
		event.addCapability(new ResourceLocation(AppEng.MODID, "skyobjects_manager"), new SingleCapabilityProvider.Serializeable<>(AppEngSkyfall.skyobjectsManagerCapability, new SkyobjectsManagerImpl()));
	}

	@SubscribeEvent
	public static void tickSkyobjects(TickEvent.WorldTickEvent event){
		if(event.phase == TickEvent.Phase.END) event.world.getCapability(AppEngSkyfall.skyobjectsManagerCapability, null).tick(event.world);
	}

	@SubscribeEvent
	public static void syncWithNewPlayer(EntityJoinWorldEvent event){
		if(!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) ((SkyobjectsManagerImpl) event.getWorld().getCapability(AppEngSkyfall.skyobjectsManagerCapability, null)).syncAllWith((EntityPlayerMP) event.getEntity());
	}

	protected World world;
	protected Supplier<Double> spawner;
	protected Map<UUID, Skyobject> skyobjects = new HashMap<>();
	protected Queue<Skyobject> toSpawn = new LinkedList<>();

	@Override
	public void tick(World world){
		if(this.world == null){
			this.world = world;
			this.spawner = AppEngSkyfall.INSTANCE.config.skyobjectFallingSupplierForWorld(world);
		}

		skyobjects.forEach((uuid, skyobject) -> skyobject.tick(world));

		if(!world.isRemote){
			//if(world.rand.nextDouble() < spawner.get()) spawn();
			//FIXME During skyrains, this will cause massive lag!
			if(skyobjects.values().removeIf(Skyobject::isDead)) syncAll();
			while(toSpawn.peek() != null){
				Skyobject skyobject = toSpawn.poll();
				skyobject.onSpawn(world);
				UUID uuid = UUID.randomUUID();
				skyobjects.put(uuid, skyobject);
				AppEngSkyfall.INSTANCE.net.sendToDimension(new SkyobjectSpawnMessage(uuid, skyobject), world.provider.getDimension());
			}
		}
	}

	@Override
	public Stream<Skyobject> getAllSkyobjects(){
		return skyobjects.values().stream();
	}

	public void receiveClientSkyobject(UUID uuid, Skyobject skyobject){
		skyobjects.put(uuid, skyobject);
	}

	@Override
	public void killall(){
		this.skyobjects.clear();
		syncAll();
		AppEngSkyfall.logger.info("Killed all skyobjects");
	}

	@Override
	public void spawn(){
		SkyobjectProvider provider = AppEngSkyfall.INSTANCE.config.getNextWeightedSkyobjectProvider(world.rand);
		GENERATORSERVICE.submit(() -> toSpawn.add(provider.generate(world.rand.nextLong())));
	}

	/*
	 * Sync
	 */

	protected void syncAll(){
		AppEngSkyfall.INSTANCE.net.sendToDimension(new SkyobjectsSyncMessage(serializeNBT()), world.provider.getDimension());
	}

	protected void syncAllWith(EntityPlayerMP player){
		AppEngSkyfall.INSTANCE.net.sendTo(new SkyobjectsSyncMessage(serializeNBT()), player);
	}

	protected void syncSpawn(){

	}

	/*
	 * NBT
	 */

	@Override
	public NBTTagCompound serializeNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList skyobjects = new NBTTagList();
		this.skyobjects.forEach((uuid, skyobject) -> skyobjects.appendTag(serializeSkyobject(uuid, skyobject)));
		nbt.setTag("skyobjects", skyobjects);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt){
		this.skyobjects.clear();
		NBTTagList skyobjects = nbt.getTagList("skyobjects", 10);
		skyobjects.forEach(tag -> {
			Pair<UUID, Skyobject> skyobject = (Pair) deserializeSkyobject((NBTTagCompound) tag);
			this.skyobjects.put(skyobject.getKey(), skyobject.getValue());
		});
	}

	public static NBTTagCompound serializeSkyobject(UUID uuid, Skyobject skyobject){
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("uuid", NBTUtil.createUUIDTag(uuid));
		tag.setString("id", skyobject.getProvider().getRegistryName().toString());
		tag.setTag("data", skyobject.getProvider().serializeNBT(skyobject));
		return tag;
	}

	public static <S extends Skyobject<S, P>, P extends SkyobjectProvider<S, P>> Pair<UUID, S> deserializeSkyobject(NBTTagCompound tag){
		return new ImmutablePair<>(NBTUtil.getUUIDFromTag(tag.getCompoundTag("uuid")), AppEngSkyfall.INSTANCE.<S, P>getSkyobjectProvidersRegistry().getValue(new ResourceLocation(tag.getString("id"))).deserializeNBT(tag.getCompoundTag("data")));
	}

}