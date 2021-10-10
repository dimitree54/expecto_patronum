package we.rashchenko.environments

import we.rashchenko.base.Activity
import we.rashchenko.base.HiddenActivity

/**
 * Special kind of environment where all activities split into two groups: inputs and outputs.
 * That split allows to treat them differently.
 * Fot example for classical supervised learning there is inputs and outputs.
 */
interface InputOutputEnvironment : Environment {
    override val activities: Collection<Activity>
        get() = listOf(inputActivities, outputActivities).flatten()
    val inputActivities: Collection<Activity>
    val outputActivities: Collection<HiddenActivity>
}