package appeng.core.worldgen.definitions;

import appeng.api.bootstrap.DefinitionFactory;
import appeng.api.definitions.IItemDefinition;
import appeng.core.lib.definitions.Definitions;
import appeng.core.worldgen.api.definitions.IWorldGenItemDefinitions;
import net.minecraft.item.Item;

public class WorldGenItemDefinitions extends Definitions<Item, IItemDefinition<Item>> implements IWorldGenItemDefinitions {

	public WorldGenItemDefinitions(DefinitionFactory registry){
		init(/*registry.buildDefaultItemBlocks()*/);
	}

}
