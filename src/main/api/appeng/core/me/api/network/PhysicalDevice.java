package appeng.core.me.api.network;

import appeng.core.me.api.network.block.ConnectUUID;
import appeng.core.me.api.network.block.Connection;
import appeng.core.me.api.network.block.ConnectionPassthrough;
import appeng.core.me.api.parts.PartColor;
import appeng.core.me.api.parts.VoxelPosition;

/**
 * Device entity in-world.<br>
 * Interacts <b>only</b> on the world thread with the world.<br>
 * Does <b>not</b> manage any scheduled tasks, as network counterpart is the one managing all scheduled tasks for both counterparts.
 * <br><br>
 * Implement on already existing in-world structures.
 *
 * @param <N> Network counterpart type
 * @param <P> In-world counterpart type
 *
 * @author Elix_x
 */
public interface PhysicalDevice<N extends NetDevice<N, P>, P extends PhysicalDevice<N, P>> {

	VoxelPosition getPosition();

	N getNetworkCounterpart();

	/**
	 * Persistent, immutable, server-only color used for connectivity checks
	 *
	 * @return color of this passthrough
	 */
	PartColor getColor();

}