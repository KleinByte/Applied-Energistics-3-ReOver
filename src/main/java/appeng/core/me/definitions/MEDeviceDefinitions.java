package appeng.core.me.definitions;

import appeng.api.bootstrap.DefinitionFactory;
import appeng.core.AppEng;
import appeng.core.lib.definitions.Definitions;
import appeng.core.me.api.bootstrap.IDeviceBuilder;
import appeng.core.me.api.definition.IDeviceDefinition;
import appeng.core.me.api.definitions.IMEDeviceDefinitions;
import appeng.core.me.api.network.NetDevice;
import appeng.core.me.api.network.PhysicalDevice;
import appeng.core.me.api.network.device.DeviceRegistryEntry;
import appeng.core.me.network.device.NetDeviceBaseLoader;
import appeng.core.me.parts.part.device.Controller;
import appeng.core.me.parts.part.device.ImportBus;
import appeng.core.me.parts.part.device.SimplyPanel;
import net.minecraft.util.ResourceLocation;

public class MEDeviceDefinitions<N extends NetDevice<N, P>, P extends PhysicalDevice<N, P>> extends Definitions<DeviceRegistryEntry<N, P>, IDeviceDefinition<N, P>> implements IMEDeviceDefinitions<N, P> {

	private final IDeviceDefinition controller;

	private final IDeviceDefinition importBus;
	private final IDeviceDefinition simplyPanel;

	public MEDeviceDefinitions(DefinitionFactory registry){
		this.controller = registry.<DeviceRegistryEntry<Controller.Network, Controller.Physical>, IDeviceDefinition<Controller.Network, Controller.Physical>, IDeviceBuilder<Controller.Network, Controller.Physical, ?>, Void>definitionBuilder(new ResourceLocation(AppEng.MODID, "controller"), ih()).deserializer(re -> new NetDeviceBaseLoader<>((uuid, netBlock) -> new Controller.Network(re, uuid, netBlock))).build();

		this.importBus = registry.<DeviceRegistryEntry<ImportBus.Network, ImportBus.Physical>, IDeviceDefinition<ImportBus.Network, ImportBus.Physical>, IDeviceBuilder<ImportBus.Network, ImportBus.Physical, ?>, Void>definitionBuilder(new ResourceLocation(AppEng.MODID, "import_bus"), ih()).deserializer(re -> new NetDeviceBaseLoader<>((uuid, netBlock) -> new ImportBus.Network(re, uuid, netBlock))).build();
		this.simplyPanel = registry.<DeviceRegistryEntry<SimplyPanel.Network, SimplyPanel.Physical>, IDeviceDefinition<SimplyPanel.Network, SimplyPanel.Physical>, IDeviceBuilder<SimplyPanel.Network, SimplyPanel.Physical, ?>, Void>definitionBuilder(new ResourceLocation(AppEng.MODID, "panel"), ih()).deserializer(re -> new NetDeviceBaseLoader<>((uuid, netBlock) -> new SimplyPanel.Network(re, uuid, netBlock))).build();
	}

	private <Nn extends NetDevice<Nn, Pp>, Pp extends PhysicalDevice<Nn, Pp>> DefinitionFactory.InputHandler<DeviceRegistryEntry<Nn, Pp>, Void> ih(){
		return new DefinitionFactory.InputHandler<DeviceRegistryEntry<Nn, Pp>, Void>(null) {};
	}

}