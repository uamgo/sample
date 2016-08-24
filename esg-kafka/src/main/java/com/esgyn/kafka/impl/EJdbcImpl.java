package com.esgyn.kafka.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.service.jdbc.EJdbc;
import com.esgyn.service.jdbc.EsgDatasource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EJdbcImpl implements EJdbc {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(EJdbcImpl.class);
	private Connection con;
	private ObjectMapper mapper;
	private String table_to_insert;
	private String insert_columns;
	private String[] columns;
	private String insertString;
	private Map tempMap = new HashMap();
	private Map<String, Integer> lenMap = new HashMap<String, Integer>();
	private SimpleDateFormat df;
	private long offset = -1;
	private List<String> offsetList = new ArrayList<String>();
	@Override
	public List<String> getOffsetList() {
		return offsetList;
	}
	public EJdbcImpl(Properties config) throws ConfigurationException {
		EsgDatasource.addConfig(config);
		String create_table_ddl = config.getProperty("create_table_ddl");
		Connection conn = null;
		Statement st = null;
		try {
			log.info("Initialize table...\n" + create_table_ddl);
			conn = EsgDatasource.getConn();
			st = conn.createStatement();
			st.execute(create_table_ddl);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
		}
		log.info("Done for table initialization.");

		this.insert_columns = config.getProperty("insert_columns").replaceFirst("^\\s*,?(.*?),?\\s*", "$1");
		this.columns = this.insert_columns.split(",");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.columns.length; i++) {
			sb.append("?");
			if (i != this.columns.length - 1) {
				sb.append(",");
			}
		}
		this.table_to_insert = config.getProperty("table_to_insert");
		this.insertString = "upsert using load into " + this.table_to_insert + "(" + this.insert_columns + ")"
				+ " values(" + sb.toString() + ")";
		String[] insert_col_len_limit = config.getProperty("insert_col_len_limit").split(",");
		for (int i = 0; i < this.columns.length; i++) {
			this.lenMap.put(this.columns[i], Integer.valueOf(insert_col_len_limit[i]));
		}
		mapper = new ObjectMapper();
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		log.debug("insertString:" + insertString);
	}

	@Override
	public void open() throws SQLException {
		con = EsgDatasource.getConn();
	}

	@Override
	public void insert(ConsumerRecords<String, String> records, long savedOffset,Logger logger) throws Exception {
		if (records.isEmpty())
			return;
		log.info("inserting ...");
		this.offset = -1;
		PreparedStatement ps = this.con.prepareStatement(this.insertString);
		for (ConsumerRecord<String, String> r : records) {
			if (savedOffset >= r.offset()) {
				continue;
			}
			try {
				this.tempMap.clear();
				JsonNode root = null;
				try {
					root = mapper.readTree(r.value());
					log.info("[" + r.offset() + "]" + r.value());
				} catch (Exception e) {
					offsetList.add(String.valueOf(r.offset()));
					BizLog.customerLog(logger, r.value().trim());
					log.error("[Message]" + r.value(), e);
					continue;
				}
				// MetricsName,MetricsValue,MetricsTimestamp,MetricsTimestamp_ts_,container_base_image,host_id,hostname,labels,namespace_id,namespace_name,nodename,pod_id,pod_name,pod_namespace,type

				String MetricsName = massage("MetricsName", readNode(root, "MetricsName"));
				JsonNode MetricsValueNode = root.get("MetricsValue");
				String MetricsValue = null;
				if (MetricsValueNode != null) {
					MetricsValue = readNode(MetricsValueNode, "value");
				}
				String MetricsTimestamp = readNode(root, "MetricsTimestamp");
				Timestamp MetricsTimestamp_ts = null;
				if (MetricsTimestamp != null)
					MetricsTimestamp_ts = toTimestamp(MetricsTimestamp);
				JsonNode MetricsTagsNode = root.get("MetricsTags");
				String container_base_image = null;
				String container_name = null;
				String host_id = null;
				String hostname = null;
				String labels = null;
				String namespace_id = null;
				String namespace_name = null;
				String nodename = null;
				String pod_id = null;
				String pod_name = null;
				String pod_namespace = null;
				String type = null;
				if (MetricsTagsNode != null) {
					container_base_image = massage("container_base_image",
							readNode(MetricsTagsNode, "container_base_image"));
					container_name = massage("container_name", readNode(MetricsTagsNode, "container_name"));
					host_id = massage("host_id", readNode(MetricsTagsNode, "host_id "));
					hostname = massage("hostname", readNode(MetricsTagsNode, "hostname"));
					labels = massage("labels", readNode(MetricsTagsNode, "labels"));
					namespace_id = massage("namespace_id", readNode(MetricsTagsNode, "namespace_id"));
					namespace_name = massage("namespace_name", readNode(MetricsTagsNode, "namespace_name"));
					nodename = massage("nodename", readNode(MetricsTagsNode, "nodename"));
					pod_id = massage("pod_id", readNode(MetricsTagsNode, "pod_id"));
					pod_name = massage("pod_name", readNode(MetricsTagsNode, "pod_name"));
					pod_namespace = massage("pod_namespace", readNode(MetricsTagsNode, "pod_namespace"));
					type = massage("type", readNode(MetricsTagsNode, "type"));
				}
				this.tempMap.put("MetricsName", MetricsName);
				this.tempMap.put("MetricsValue", MetricsValue);
				this.tempMap.put("MetricsTimestamp", MetricsTimestamp);
				this.tempMap.put("MetricsTimestamp_ts", MetricsTimestamp_ts);
				this.tempMap.put("container_base_image", container_base_image);
				this.tempMap.put("container_name", container_name);
				this.tempMap.put("host_id", host_id);
				this.tempMap.put("hostname", hostname);
				this.tempMap.put("labels", labels);
				this.tempMap.put("namespace_id", namespace_id);
				this.tempMap.put("namespace_name", namespace_name);
				this.tempMap.put("nodename", nodename);
				this.tempMap.put("pod_id", pod_id);
				this.tempMap.put("pod_name", pod_name);
				this.tempMap.put("pod_namespace", pod_namespace);
				this.tempMap.put("type", type);

				for (int i = 0; i < columns.length; i++) {
					ps.setObject(i + 1, this.tempMap.get(columns[i]));
				}
				ps.addBatch();
			} catch (Exception e) {
				log.error("[Message]" + r.value(), e);
				continue;
			}
			offset = r.offset();
		}
		ps.executeBatch();
		try {
			ps.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.debug("Done for insertion.");

	}

	@Override
	public long getCurrentOffset() {
		return this.offset;
	}

	private Timestamp toTimestamp(String timeString) throws ParseException {
		Date date = df.parse(timeString);
		return new Timestamp(date.getTime());
	}

	private String massage(String col, String value) {
		log.debug("column:" + col + ", value:" + value);
		if (value == null)
			return null;
		if (value.length() > this.lenMap.get(col)) {
			return value.substring(0, this.lenMap.get(col) - 1);
		}
		return value;
	}

	private String readNode(JsonNode parent, String nodeName) {
		JsonNode node = parent.get(nodeName);
		if (node != null) {
			return node.asText();
		}
		return null;
	}

	@Override
	public void close() throws SQLException {
		this.con.close();
	}

}
