package we.rashchenko.utils

fun hemmingDistance(target: List<Boolean>, prediction: List<Boolean>): Double =
	target.zip(prediction).count { it.first != it.second }.toDouble()