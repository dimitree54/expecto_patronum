package we.rashchenko.base


/**
 * Interface for instances that can be either [active] or not.
 */
interface Activity {
	/**
	 * Is instance active (excited) or not.
	 */
	val active: Boolean
}