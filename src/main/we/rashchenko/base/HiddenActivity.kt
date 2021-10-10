package we.rashchenko.base

import we.rashchenko.environments.Environment


/**
 * Activity with special flag specifying that activity not available now.
 *  For example, it may be useful if some [ObservableActivities] in [Environment] not always available.
 */
interface HiddenActivity : Activity {
    /**
     *  If [hidden] set to true it means that [active] value may be invalid and if possible should be ignored.
     */
    var hidden: Boolean
}