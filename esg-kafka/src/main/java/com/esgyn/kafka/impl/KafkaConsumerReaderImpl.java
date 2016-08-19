package com.esgyn.kafka.impl;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import esgyn.kafka.KafkaConsumerReader;

public class KafkaConsumerReaderImpl implements KafkaConsumerReader {
	public static void main(String args[]) {
		KafkaConsumerReader reader = new KafkaConsumerReaderImpl();
		String topic = args[0];
		int partition = Integer.parseInt(args[1]);
		List<String> seeds = new ArrayList<String>();
		seeds.add(args[2]);
		int port = Integer.parseInt(args[3]);
		try {
			reader.readJsonFromKafka(topic, partition, seeds, port);
		} catch (Exception e) {
			System.out.println("Oops:" + e);
			e.printStackTrace();
		}
	}

	private List<String> m_replicaBrokers = new ArrayList<String>();

	public KafkaConsumerReaderImpl() {
		m_replicaBrokers = new ArrayList<String>();
	}

	public void readJsonFromKafka(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port)
			throws Exception {
		// find the meta data about the topic and partition we are interested in
		//
		PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
		if (metadata == null) {
			System.out.println("Can't find metadata for Topic and Partition. Exiting");
			return;
		}
		if (metadata.leader() == null) {
			System.out.println("Can't find Leader for Topic and Partition. Exiting");
			return;
		}
		String leadBroker = metadata.leader().host();
		String clientName = "Client_" + a_topic + "_" + a_partition;

		SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
		long readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(),
				clientName);

		int numErrors = 0;
		while (true) {
			if (consumer == null) {
				consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
			}
			FetchRequest req = new FetchRequestBuilder()
					.clientId(clientName)
					.addFetch(a_topic, a_partition, readOffset, 100000) // Note:
																		// this
																		// fetchSize
																		// of
																		// 100000
																		// might
																		// need
																		// to be
																		// increased
																		// if
																		// large
																		// batches
																		// are
																		// written
																		// to
																		// Kafka
					.build();
			FetchResponse fetchResponse = consumer.fetch(req);

			if (fetchResponse.hasError()) {
				numErrors++;
				// Something went wrong!
				short code = fetchResponse.errorCode(a_topic, a_partition);
				System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
				if (numErrors > 5)
					break;
				if (code == ErrorMapping.OffsetOutOfRangeCode()) {
					// We asked for an invalid offset. For simple case ask for
					// the last element to reset
					readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(),
							clientName);
					continue;
				}
				consumer.close();
				consumer = null;
				leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
				continue;
			}
			numErrors = 0;

			long numRead = 0;
			String messge = "";
			for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
				long currentOffset = messageAndOffset.offset();
				if (currentOffset < readOffset) {
					System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
					continue;
				}
				readOffset = messageAndOffset.nextOffset();
				ByteBuffer payload = messageAndOffset.message().payload();

				byte[] bytes = new byte[payload.limit()];
				payload.get(bytes);
				System.out.println("print received message here: " + String.valueOf(messageAndOffset.offset()) + ": "
						+ new String(bytes, "UTF-8"));
				numRead++;
				messge = messge + new String(bytes);
			}
			if (numRead == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			} else {
				JSONTokener tokener = new JSONTokener(messge);
				JSONObject obj = new JSONObject(tokener);
				insertTrafodion(obj);
				System.out.println("print jsonObj MetricsName value here: " + obj.get("MetricsTimestamp") + "; " + obj.get("MetricsName"));
			}
		}
		if (consumer != null)
			consumer.close();
	}

	public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
			long whichTime, String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
				requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);

		if (response.hasError()) {
			System.out.println(
					"Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}

	private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
		for (int i = 0; i < 3; i++) {
			boolean goToSleep = false;
			PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port, a_topic, a_partition);
			if (metadata == null) {
				goToSleep = true;
			} else if (metadata.leader() == null) {
				goToSleep = true;
			} else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
				// first time through if the leader hasn't changed give
				// ZooKeeper a second to recover
				// second time, assume the broker did recover before failover,
				// or it was a non-Broker issue
				//
				goToSleep = true;
			} else {
				return metadata.leader().host();
			}
			if (goToSleep) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}
		System.out.println("Unable to find new leader after Broker failure. Exiting");
		throw new Exception("Unable to find new leader after Broker failure. Exiting");
	}

	private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
		PartitionMetadata returnMetaData = null;
		loop: for (String seed : a_seedBrokers) {
			SimpleConsumer consumer = null;
			try {
				consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
				List<String> topics = Collections.singletonList(a_topic);
				TopicMetadataRequest req = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

				List<TopicMetadata> metaData = resp.topicsMetadata();
				for (TopicMetadata item : metaData) {
					for (PartitionMetadata part : item.partitionsMetadata()) {
						if (part.partitionId() == a_partition) {
							returnMetaData = part;
							break loop;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
						+ ", " + a_partition + "] Reason: " + e);
			} finally {
				if (consumer != null)
					consumer.close();
			}
		}
		if (returnMetaData != null) {
			m_replicaBrokers.clear();
//			for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
//				m_replicaBrokers.add(replica.host());
//			}
		}
		return returnMetaData;
	}

	public void insertTrafodion(JSONObject obj) {
		// TODO Auto-generated method stub
		Connection cnn=null;
		PreparedStatement stmt=null;
		try {
				Class.forName("org.trafodion.jdbc.t4.T4Driver");
				cnn=DriverManager.getConnection("jdbc:t4jdbc://10.10.10.8:23400/:","trafodion","traf123");
				stmt=cnn.prepareStatement("Insert into alex.metrics values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				stmt.setString(1, obj.getString("MetricsName"));
				stmt.setInt(2, obj.getJSONObject("MetricsValue").getInt("value"));
				stmt.setString(3, obj.getString("MetricsTimestamp"));
				JSONObject MetricsTags=obj.getJSONObject("MetricsTags");
				stmt.setString(4, MetricsTags.getString("container_base_image"));
				stmt.setString(5, MetricsTags.getString("container_name"));
				stmt.setString(6, MetricsTags.getString("host_id"));
				stmt.setString(7, MetricsTags.getString("hostname"));
				stmt.setString(8, MetricsTags.getString("labels"));
				stmt.setString(9, MetricsTags.getString("namespace_id"));
				stmt.setString(10, MetricsTags.getString("namespace_name"));
				stmt.setString(11, MetricsTags.getString("nodename"));
				stmt.setString(12, MetricsTags.getString("pod_id"));
				stmt.setString(13, MetricsTags.getString("pod_name"));
				stmt.setString(14, MetricsTags.getString("pod_namespace"));
				stmt.setString(15, MetricsTags.getString("type"));
				stmt.execute();
				System.out.println("the json object inserted into trafodion successfully!");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}