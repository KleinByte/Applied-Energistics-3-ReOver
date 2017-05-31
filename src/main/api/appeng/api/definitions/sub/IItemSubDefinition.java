package appeng.api.definitions.sub;

import appeng.api.item.IStateItem;
import appeng.api.item.IStateItemState;
import net.minecraft.item.Item;

/**
 * Default interface for item sub-definitions. For use with type parameters. All AE item sub definitions implement this. And all AE items with states implement {@link IStateItem}.
 *
 * @param <S> Item IStateItemState
 * @param <I> Item
 * @author Elix_x
 */
public interface IItemSubDefinition<S extends IStateItemState<I>, I extends Item & IStateItem<I>> extends ISubDefinition<S, I, IItemSubDefinition<S, I>> {

}
