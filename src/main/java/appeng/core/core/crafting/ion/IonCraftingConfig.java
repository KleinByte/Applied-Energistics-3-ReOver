package appeng.core.core.crafting.ion;

import appeng.api.bootstrap.InitializationComponent;
import appeng.api.config.ConfigCompilable;
import appeng.core.AppEng;
import appeng.core.core.AppEngCore;
import appeng.core.core.api.crafting.ion.Ion;
import appeng.core.core.api.crafting.ion.IonEnvironmentContext;
import appeng.core.core.api.crafting.ion.NativeEnvironmentChange;
import com.google.common.collect.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class IonCraftingConfig implements ConfigCompilable, InitializationComponent.Init  {

	private Map<String, List<MutablePair<ResourceLocation, Integer>>> oreDict2Ions = new HashMap<>();
	public transient Multimap<String, Pair<Ion, Integer>> oreDict2IonsC = HashMultimap.create();

	private Map<String, Reactivity> oreDict2Reactivity = new HashMap<>();
	public transient Map<String, Reactivity.Compiled> oreDict2ReactivityC = new HashMap<>();

	private Map<ResourceLocation, List<Recipe>> recipes = new HashMap<>();
	public transient Multimap<IonEnvironmentContext.Change, Recipe.Compiled> recipesC = HashMultimap.create();

	public IonCraftingConfig(){
		oreDict2Ions.put("gemQuartz", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "quartz"), 1)));
		oreDict2Ions.put("dustRedstone", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "redstone"), 1)));
		oreDict2Ions.put("gunpowder", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "sulfur"), 1)));
		oreDict2Ions.put("dustSulfur", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "sulfur"), 1)));
		oreDict2Ions.put("enderpearl", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "ender"), 1)));

		oreDict2Ions.put("certusQuartz", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "quartz"), 3)));
		oreDict2Ions.put("certusRedstone", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "redstone"), 3)));
		oreDict2Ions.put("certusSulfur", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "sulfur"), 2)));
		oreDict2Ions.put("certusEnderium", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "ender"), 1)));
		oreDict2Ions.put("supersolidCertus", Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 11)));


		oreDict2Reactivity.put("certusQuartz", new Reactivity(false, Sets.newHashSet("water")));
		oreDict2Reactivity.put("certusRedstone", new Reactivity(false, Sets.newHashSet("water")));
		oreDict2Reactivity.put("certusSulfur", new Reactivity(false, Sets.newHashSet("water")));
		oreDict2Reactivity.put("certusEnderium", new Reactivity(false, Sets.newHashSet("water")));
		oreDict2Reactivity.put("supersolidCertus", new Reactivity(true, Sets.newHashSet()));

		recipes.put(new ResourceLocation(AppEng.MODID, NativeEnvironmentChange.HEATING.name().toLowerCase()), Lists.newArrayList(
			new Recipe(Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "quartz"), 3)), Lists.newArrayList(new Recipe.Result(new ResourceLocation("minecraft:item"), new ResourceLocation(AppEng.MODID, "certus_quartz"), 1))),
			new Recipe(Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "redstone"), 3)), Lists.newArrayList(new Recipe.Result(new ResourceLocation("minecraft:item"), new ResourceLocation(AppEng.MODID, "certus_redstone"), 1))),
			new Recipe(Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "sulfur"), 2)), Lists.newArrayList(new Recipe.Result(new ResourceLocation("minecraft:item"), new ResourceLocation(AppEng.MODID, "certus_sulfur"), 1))),
			new Recipe(Lists.newArrayList(new MutablePair<>(new ResourceLocation(AppEng.MODID, "certus"), 1), new MutablePair<>(new ResourceLocation(AppEng.MODID, "ender"), 1)), Lists.newArrayList(new Recipe.Result(new ResourceLocation("minecraft:item"), new ResourceLocation(AppEng.MODID, "certus_enderium"), 1)))
		));
	}

	@Override
	public void compile(){

	}

	@Override
	public void init(){
		oreDict2IonsC.clear();
		oreDict2Ions.forEach((ore, ions) -> ions.forEach(ionr -> Optional.ofNullable(AppEngCore.INSTANCE.getIonRegistry().getValue(ionr.getKey())).ifPresent(ion -> oreDict2IonsC.put(ore, new ImmutablePair<>(ion, ionr.getRight())))));

		oreDict2ReactivityC.clear();
		oreDict2ReactivityC.putAll(Maps.transformValues(oreDict2Reactivity, Reactivity::compile));

		recipesC.clear();
		recipes.forEach((change, srecipes) -> recipesC.putAll(AppEngCore.INSTANCE.getCraftingIonRegistry().getChange(change), Lists.transform(srecipes, Recipe::compile)));
	}

	@Override
	public void decompile(){
		oreDict2Ions.clear();
		oreDict2IonsC.keySet().forEach(ore -> oreDict2Ions.put(ore, oreDict2IonsC.get(ore).stream().map(ion -> new MutablePair<>(ion.getLeft().getRegistryName(), ion.getRight())).collect(Collectors.toList())));

		oreDict2Reactivity.clear();
		oreDict2Reactivity.putAll(Maps.transformValues(oreDict2ReactivityC, Reactivity.Compiled::decompile));

		recipes.clear();
		recipesC.keySet().forEach(change -> recipes.put(AppEngCore.INSTANCE.getCraftingIonRegistry().getChangeName(change), recipesC.get(change).stream().map(Recipe.Compiled::decompile).collect(Collectors.toList())));
	}

	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof IonCraftingConfig)) return false;

		IonCraftingConfig that = (IonCraftingConfig) o;

		if(!oreDict2Ions.equals(that.oreDict2Ions)) return false;
		if(!oreDict2Reactivity.equals(that.oreDict2Reactivity)) return false;
		return recipes.equals(that.recipes);
	}

	@Override
	public int hashCode(){
		int result = oreDict2Ions.hashCode();
		result = 31 * result + oreDict2Reactivity.hashCode();
		result = 31 * result + recipes.hashCode();
		return result;
	}

	public static class Reactivity {

		public boolean def = false;
		public Set<String> fluids = new HashSet<>();

		public Reactivity(){

		}

		public Reactivity(boolean def, Set<String> fluids){
			this.def = def;
			this.fluids = fluids;
		}

		Compiled compile(){
			return new Compiled(def, fluids.stream().map(FluidRegistry::getFluid).collect(Collectors.toSet()));
		}

		@Override
		public boolean equals(Object o){
			if(this == o) return true;
			if(!(o instanceof Reactivity)) return false;

			Reactivity that = (Reactivity) o;

			if(def != that.def) return false;
			return fluids.equals(that.fluids);
		}

		@Override
		public int hashCode(){
			int result = (def ? 1 : 0);
			result = 31 * result + fluids.hashCode();
			return result;
		}

		public static class Compiled {

			public boolean def = false;
			public Set<Fluid> fluids = new HashSet<>();

			public Compiled(){

			}

			public Compiled(boolean def, Set<Fluid> fluids){
				this.def = def;
				this.fluids = fluids;
			}

			Reactivity decompile(){
				return new Reactivity(def, fluids.stream().map(Fluid::getName).collect(Collectors.toSet()));
			}

		}

	}

	public static class Recipe {

		public List<MutablePair<ResourceLocation, Integer>> ions = new ArrayList<>();
		public List<Result> results = new ArrayList<>();

		public Recipe(){
		}

		public Recipe(List<MutablePair<ResourceLocation, Integer>> ions, List<Result> results){
			this.ions = ions;
			this.results = results;
		}

		public Recipe.Compiled compile(){
			return new Compiled(Lists.transform(ions, pair -> new ImmutablePair<>(AppEngCore.INSTANCE.getIonRegistry().getValue(pair.getLeft()), pair.getRight())), Lists.transform(results, Result::compile));
		}

		@Override
		public boolean equals(Object o){
			if(this == o) return true;
			if(!(o instanceof Recipe)) return false;

			Recipe recipe = (Recipe) o;

			if(!ions.equals(recipe.ions)) return false;
			return results.equals(recipe.results);
		}

		@Override
		public int hashCode(){
			int result = ions.hashCode();
			result = 31 * result + results.hashCode();
			return result;
		}

		public static class Result {

			public ResourceLocation type;
			public ResourceLocation result;
			public int amount;

			public Result(){
			}

			public Result(ResourceLocation type, ResourceLocation result, int amount){
				this.type = type;
				this.result = result;
				this.amount = amount;
			}

			public <T> Result.Compiled<T> compile(){
				return new Compiled<>(type, AppEngCore.INSTANCE.getCraftingIonRegistry().deserializeResult(type, result), amount);
			}

			@Override
			public boolean equals(Object o){
				if(this == o) return true;
				if(!(o instanceof Result)) return false;

				Result result1 = (Result) o;

				if(amount != result1.amount) return false;
				if(!type.equals(result1.type)) return false;
				return result.equals(result1.result);
			}

			@Override
			public int hashCode(){
				int result1 = type.hashCode();
				result1 = 31 * result1 + result.hashCode();
				result1 = 31 * result1 + amount;
				return result1;
			}

			public static class Compiled<T> {

				public ResourceLocation type;
				public T result;
				public int amount;

				public Compiled(ResourceLocation type, T result, int amount){
					this.type = type;
					this.result = result;
					this.amount = amount;
				}

				public Result decompile(){
					return new Result(type, AppEngCore.INSTANCE.getCraftingIonRegistry().serializeResult(type, result), amount);
				}

			}

		}

		public static class Compiled {

			public List<Pair<Ion, Integer>> ions = new ArrayList<>();
			public List<Result.Compiled> results = new ArrayList<>();

			public Compiled(List<Pair<Ion, Integer>> ions, List<Result.Compiled> results){
				this.ions = ions;
				this.results = results;
			}

			public Recipe decompile(){
				return new Recipe(Lists.transform(ions, pair -> new MutablePair<>(pair.getLeft().getRegistryName(), pair.getRight())), Lists.transform(results, Result.Compiled::decompile));
			}

		}

	}

}