/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.metrics.types

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.result.message.{ KO, OK, Status }

import org.HdrHistogram.{ IntCountsHistogram, AbstractHistogram }

class HistogramRequestMetricsBuffer(configuration: GatlingConfiguration) extends RequestMetricsBuffer {

  private val percentile1 = configuration.charting.indicators.percentile1
  private val percentile2 = configuration.charting.indicators.percentile2
  private val percentile3 = configuration.charting.indicators.percentile3
  private val percentile4 = configuration.charting.indicators.percentile4

  private val okHistogram: AbstractHistogram = new IntCountsHistogram(2)
  private val koHistogram: AbstractHistogram = new IntCountsHistogram(2)
  private val allHistogram: AbstractHistogram = new IntCountsHistogram(2)

  override def add(status: Status, time: Long): Unit = {
    val recordableTime = time.max(1L)

    allHistogram.recordValue(recordableTime)
    status match {
      case OK => okHistogram.recordValue(recordableTime)
      case KO => koHistogram.recordValue(recordableTime)
    }
  }

  override def clear(): Unit = {
    okHistogram.reset()
    koHistogram.reset()
    allHistogram.reset()
  }

  override def metricsByStatus: MetricByStatus =
    MetricByStatus(metricsOfHistogram(okHistogram), metricsOfHistogram(koHistogram), metricsOfHistogram(allHistogram))

  private def metricsOfHistogram(histogram: AbstractHistogram): Option[Metrics] = {
    val count = histogram.getTotalCount
    if (count > 0) {
      Some(Metrics(
        count,
        histogram.getMinValue.toInt,
        histogram.getMaxValue.toInt,
        histogram.getValueAtPercentile(percentile1).toInt,
        histogram.getValueAtPercentile(percentile2).toInt,
        histogram.getValueAtPercentile(percentile3).toInt,
        histogram.getValueAtPercentile(percentile4).toInt))
    } else
      None
  }
}