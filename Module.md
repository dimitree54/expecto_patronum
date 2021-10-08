# Module chnn-library
Chaotic neural network library. Classes and helper functions to build evolving non-layered neural network. Initially it
 is developed for [competition](https://dimitree54.github.io/ChNN/), but can be adapted for other usages.

# Package we.rashchenko.base
Base interfaces describing main concepts of the chaotic neural network.

# Package we.rashchenko.environments
Classes describing environment outside the neural network. The environment that neural network communicates with.

# Package we.rashchenko.networks
Classes for chaotic neural networks. Describes how neurons in the network communicate with each other, but not how
 new neurons created and wired (check networks builders for that). So far the main implementation is [StochasticNeuralNetwork
] developed for efficient inference of the chaotic neural network with sparse activations.

# Package we.rashchenko.networks.builders
Classes describing how chaotic neural network built. Contains logic of the evolution (proliferation of the best neurons,
 pruning of the worst neurons). Also, builders determine how newly created neurons wired with existing ones.

# Package we.rashchenko.networks.controllers
Classes for calculation external feedback for neurons. Except internal collaborative feedback which neurons assign to 
each other, there is some qualities that we want to encourage in neurons that can not be estimated during internal
feedbacks exchange. For example neurons which work faster should be extra rewarded. For implementing such external 
intuitions what good neuron is there is network controllers.

# Package we.rashchenko.neurons
Base classes for the main network node - the neuron. Contains [Neuron] interface and some helper classes to sample 
more successful neurons and connect them to controllers.

# Package we.rashchenko.neurons.inputs
Special type of the neuron that works as neural network interface to communicate with the environment or other networks.
That package contains several input neuron implementations with different training strategy.

# Package we.rashchenko.neurons.zoo
Exemplar implementation of the neuron interface. Shows how to implement custom neuron + it is used in neural network
tests.

# Package we.rashchenko.utils
Helper functions and classes used during ChNN development.

# Package we.rashchenko.utils.collections
Helper collections empowering evolution natural selection.
