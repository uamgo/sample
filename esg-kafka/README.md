# esgyn kafka consumer

## Overview

### it's a tool used to consume kafka message and import into database.

## Configuration
#since we used the manual commit, it should be set as false.
enable.auto.commit=false	 
##this is kafka comsumer group id
group.id=a0 	
#this is the kfaka host name and port num
bootstrap.servers=10.10.12.99:9092
auto.offset.reset=earliest
##this is max poll size, while beyond this amount, it will be store in zookeeper?
max.poll.records=5000
client.id=esg_kafka
##this is the topic name, it should exist in kafka.
topic=topic3
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
partition.assignment.strategy=org.apache.kafka.clients.consumer.RangeAssignor
exclude.internal.topics=false
#session.timeout.ms=60000
##The time window of a fetch from Kafka
#poll.timeout

#### database configuration
## the table will be created automatically if not exist. you could customize the field acording your requirement.
create_table_ddl=create table if not exists seabase.k_monitor(MetricsName varchar(100),MetricsValue int,MetricsTimestamp varchar(40),MetricsTimestamp_ts timestamp,container_base_image varchar(500),container_name varchar(100),host_id varchar(50),hostname varchar(50),labels varchar(500),namespace_id varchar(100),namespace_name varchar(100),nodename varchar(100),pod_id varchar(100),pod_name varchar(100),pod_namespace varchar(100),type varchar(100))
#table name
table_to_insert=seabase.k_monitor
#insert columns
insert_columns=MetricsName,MetricsValue,MetricsTimestamp,MetricsTimestamp_ts,container_base_image,container_name,host_id,hostname,labels,namespace_id,namespace_name,nodename,pod_id,pod_name,pod_namespace,type
##insert column length limit
insert_col_len_limit=100,20,80,80,500,100,50,50,500,100,100,100,100,100,100,100

## connection pool configuration https://github.com/brettwooldridge/HikariCP
##the key should start with db, the value coud be changed following your requirement.
db.driverClassName=driver class name
db.username= databaes user name
db.password=database password
db.jdbcUrl=jdbc url
##test jdbc connection query.
db.connectionTestQuery=validation query
##connection pool size.
db.maximumPoolSize= connection number.

#log4j.xml
###you can change the log level here.
	<root>
			<level value="DEBUG" />
		</root>

# Build
1>cd project
2>mvn install
3>deploy project_name.zip
4>unzip project_name.zip
5>cd /project_name/bin
6>./start.sh

# backup mechanism 
###if the insert failed, it will be backup in the biz.log file. it used the log4j customize log level.
###if the kafka manual commit failed, the offset will be stored in the offset.properties.

#TODO
### in future the consumer could be deployed in multiple nodes, we need to store the offset.properties in zookeeepr or hdfs. so all the consumer could share the backup file.