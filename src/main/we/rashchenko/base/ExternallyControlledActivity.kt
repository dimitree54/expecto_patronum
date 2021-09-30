package we.rashchenko.base

/**
 * [Activity] that can be set from outside.
 *
 * @constructor
 * Create an [ExternallyControlledActivity], not [active] by default.
 */
class ExternallyControlledActivity : Activity {
	override var active: Boolean = false
}