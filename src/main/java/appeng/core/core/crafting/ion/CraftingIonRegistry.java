package appeng.core.core.crafting.ion;

import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;

public class CraftingIonRegistry {

	public List<Fluid> ionEnvironmentFluids = new ArrayList<>();

	public void registerEnvironmentFluid(Fluid fluid){
		ionEnvironmentFluids.add(fluid);
	}

	public void onIonEntityItemTick(EntityItem item){

	}

}
