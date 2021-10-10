package we.rashchenko.base

/**
 * [HiddenActivity] that can be set from outside.
 *
 * @constructor
 * Create an [ExternallyControlledHiddenActivity], not [active] by default.
 */
class ExternallyControlledHiddenActivity : HiddenActivity {
	override var hidden: Boolean = false
	override var active: Boolean = false
}