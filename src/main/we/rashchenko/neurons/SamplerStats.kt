package we.rashchenko.neurons

import java.lang.Integer.max

class SamplerStats(val score: Double, val chooseProbability: Double, val population: Int)

fun getSummary(stats: Map<NeuronsSampler, SamplerStats>): String{
    val totalPopulation = max(1, stats.values.sumOf { it.population }).toDouble()
    val summary = StringBuilder("Neurons manager summary:")
    stats.forEach { (sampler, stats) ->
        summary.append(
            "\n\t${sampler.name} of ${sampler.author} has score ${"%.${2}f".format(stats.score)}, " +
                    "takes ${stats.population / totalPopulation} of a total population " +
                    "and have ${"%.${2}f".format(stats.chooseProbability)} probability to be chosen next time"
        )
    }
    return summary.toString()
}
