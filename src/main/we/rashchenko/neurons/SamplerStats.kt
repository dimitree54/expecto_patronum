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

fun getResultsMarkdownTable(stats: Map<NeuronsSampler, SamplerStats>): String{
    val totalPopulation = max(1, stats.values.sumOf { it.population }).toDouble()
    val summary = StringBuilder(
        "# Results" +
                "\n| Rating position | Sampler name | Author | Score | Population rate |" +
                "\n| --------------- | ------------ | -------| ----- | --------------- |"
    )
    stats.toList().sortedByDescending { it.second.score }.forEachIndexed{ place, (sampler, stats) ->
        summary.append("\n| ${place + 1} | ${sampler.name} | ${sampler.author} |  " +
                "${"%.${2}f".format(stats.score)} | ${"%.${2}f".format(stats.population / totalPopulation)} |"
        )
    }
    return summary.toString()
}