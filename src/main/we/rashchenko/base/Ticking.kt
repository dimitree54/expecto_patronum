package we.rashchenko.base

/**
 * Interface for "alive" instances that change their state in time.
 * That instances notified that it is time to change their state by [tick] function.
 */
interface Ticking {
	/**
	 * The function to notify the instance that it is time to change its state.
	 */
	fun tick()

	/**
	 * Number of [tick]s that happened with the instance.
	 */
	val timeStep: Long
}