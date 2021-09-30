package we.rashchenko.environments

import we.rashchenko.base.ObservableActivities
import we.rashchenko.base.Ticking
import we.rashchenko.networks.NeuralNetwork

/**
 * Interface to represent "the world" outside the [NeuralNetwork].
 * It is alive ([Ticking]) and has some public [ObservableActivities]
 */
interface Environment : Ticking, ObservableActivities