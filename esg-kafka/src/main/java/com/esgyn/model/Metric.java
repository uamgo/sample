package com.esgyn.model;

import java.sql.Timestamp;

public class Metric {
	private String metricsName;
	private MetricsValue metricsValue;
	private String MetricsTimestamp;
	private Timestamp MetricsTimestampTs;
	private MetricsTags metricsTags;
	public String getMetricsName() {
		return metricsName;
	}
	public void setMetricsName(String metricsName) {
		this.metricsName = metricsName;
	}
	public MetricsValue getMetricsValue() {
		return metricsValue;
	}
	public void setMetricsValue(MetricsValue metricsValue) {
		this.metricsValue = metricsValue;
	}
	public String getMetricsTimestamp() {
		return MetricsTimestamp;
	}
	public void setMetricsTimestamp(String metricsTimestamp) {
		MetricsTimestamp = metricsTimestamp;
	}
	public Timestamp getMetricsTimestampTs() {
		return MetricsTimestampTs;
	}
	public void setMetricsTimestampTs(Timestamp metricsTimestampTs) {
		MetricsTimestampTs = metricsTimestampTs;
	}
	public MetricsTags getMetricsTags() {
		return metricsTags;
	}
	public void setMetricsTags(MetricsTags metricsTags) {
		this.metricsTags = metricsTags;
	}
	
}
