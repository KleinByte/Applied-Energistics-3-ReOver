package appeng.core.me.api.network.block;

import appeng.core.me.api.network.NetBlock;
import appeng.core.me.api.parts.PartColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

import static appeng.core.me.api.parts.container.GlobalVoxelsInfo.VOXELSIZED;

public interface ConnectionPassthrough extends Predicate<Connection> {

	/**
	 * Persistent, serialized, immutable, server-only UUID used for connection path finding <i>through this component</i> inside network blocks.
	 *
	 * @return UUID for connection <i>through this component</i>
	 */
	ConnectUUID getUUIDForConnectionPassthrough();

	/**
	 * Persistent, immutable, server-only color used for connectivity checks
	 *
	 * @return color of this passthrough
	 */
	PartColor getColor();

	/**
	 * Returns passthrough's connection parameters for given connection type.<br>
	 * Immutable, persistent.
	 *
	 * @param connection connection
	 * @param <Param>    connection parameter type
	 * @return requirements for given connection
	 */
	<Param extends Comparable<Param>> Param getPassthroughConnectionParameter(Connection<Param, ?> connection);

	/**
	 * Abstract length of this passthrough in meters (blocks) for simulating signal decay.<br>
	 * Can be negative, in which case the passthrough is a repeater(/booster).
	 *
	 * @return abstract length of this passthrough
	 */
	default double getLength(){
		return VOXELSIZED;
	}

	@Nonnull
	Optional<NetBlock> getAssignedNetBlock();

	void assignNetBlock(@Nullable NetBlock netBlock);

}