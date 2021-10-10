package we.rashchenko.base

import we.rashchenko.environments.Environment
import we.rashchenko.networks.NeuralNetwork

/**
 * Collection of public (observable) [Activities][Activity].
 * If some Collection<[Activity]> (for example [NeuralNetwork] or [Environment]) wants to make some of its activities
 *  public it can implement this interface.
 * That will allow other instances to observe these public activities.
 */
interface ObservableActivities {
    /**
     * Public (observable) [Activities][Activity] of the instance.
     */
    val activities: List<Activity>
}