package we.rashchenko.utils.collections

import we.rashchenko.base.Feedback

class WorstNNeuronIDs(n: Int) : WorstN<Pair<Int, Feedback>>(n, Comparator { o1, o2 -> o1.second.compareTo(o2.second) })
