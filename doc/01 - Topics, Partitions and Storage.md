# 01 - Topics, Partitions and Storage

Based on https://www.confluent.io/blog/kafka-streams-tables-part-2-topics-partitions-and-storage-fundamentals/

## TOPIC

A topic is an unbounded sequence of serialized events, where each event is represented as an encoded key-value pair or “**message**” (with additional metadata as timestap).
A topic is given a name by its owner such as payments, truck-geolocations, cloud-metrics, or _customer-registrations_.
A topic can storage messages for a time or space limit or undefinetely (https://www.confluent.io/blog/publishing-apache-kafka-new-york-times/).

### Serialization and deserialization

Events are _serialized_ when stored and _deserialized_ when read from the topic. This is resposibility of the 
Kafka client, because events are stored in their key, value binary format (<byte[], byte[]> in Java notation).
Common formats are Apache Avro™ (with the Confluent Schema Registry), Protobuf, and JSON. 
This will free the brokers of concerning about the serialization performance draining technics.

### Storage is partitioned
Kafka topics are partitioned, meaning a topic is spread over a number of “buckets” located on different brokers.
While the topic is a logical concept in Kafka, a partition is the smallest storage unit that holds a subset of records owned by a topic. Each partition is a single log file where records are written to it in an append-only fashion.

This distributed placement of your data is very important for scalability because it allows client applications to read the data from many brokers at the same time.

When creating a topic, you must choose the number of partitions it should contain. Each partition then contains one specific subset of the full data in a topic (see partitioning in databases and partitioning of a set). 
To make your data fault tolerant, every partition can be replicated on multiple brokers, even across geo-regions or datacenters, so that there are always multiple brokers that have a copy of the data just in case things go wrong, you want to do maintenance on the brokers, and so on. 
A common setting in production is a replication factor of 3 for a total of three copies.

#### Partitioning functions

Kafka fully decuples prouducers and consumers: event Producers determine event partitioning —how events will be spread over the various partitions in a topic. 
More specifically, they use a **partitioning function** ![img.png](img/partition_function01.png) to determine which partition number a produced event will be sent to.
On common example is
![img.png](img/partition_function02.png)

so that events are evenly distributed over the available partitions.
The partitioning function actually provides you with further information in addition to the event key for determining the desired target partition, such as the topic name and cluster metadata, but this is not material for the scope of this article.

#### Reading from a topic

Kafka _doesn’t push messages to consumers_. Instead, **consumers have to pull messages off Kafka topic partitions** (avoid **back pressure**). 
A consumer _connects to a partition in a broker_, reads the messages in the order in which they were written.
Kafka keeps the index of the last read event, called **index**, for each registered consumer. This guarantees that, even after a crash, a cunsumer can resume reading events in the right order.
![consumer_offsets.png](img%2Fconsumer_offsets.png)

#### Partitioning order

Produced events are appended to the partition assigned by the partitioning function, so it is important that related messages
are end up in the same partition in order to guarantee a consistent order of processing.
This relationship between events should be expressed by the event key, so that the key based partitioning function will
assign the same key to the same partition.

To give an example of how to partition a topic, consider producers that publish geo-location updates of trucks for a logistics company. 
In this scenario, any events about the same truck should always be sent to one and the same partition. 
This can be achieved by picking a unique identifier for each truck as the event key (e.g., its licensing plate or vehicle identification number), 
in combination with the default partitioning function.

#### Partitioning reordering

So, what are the most common reasons why events with the same event key may end up in different partitions? Two causes stand out:

* **Topic configuration**: someone increased the number of partitions of a topic. In this scenario, the default partitioning function ƒ(event.key, event.value) now assigns different target partitions for at least some of the events because the modulo parameter has changed.
* **Producer configuration**: a producer uses a custom partitioning function.
It’s essential to be careful in these situations because sorting them out requires extra steps. For this reason, we also recommend over-partitioning a topic—using a larger number of partitions than you think you need—to reduce the chance of needing to repartition.

_My tip_: if in doubt, use 30 partitions per topic. 
This is a good number because (a) it is high enough to cover some really high-throughput requirements, (b) it is low enough that you will not hit the limit anytime soon of how many partitions a single broker can handle, even if you create many topics in your Kafka cluster, and (c) it is a highly composite number as it is evenly divisible by 1, 2, 3, 5, 6, 10, 15, and 30. This benefits the processing layer because it results in a more even workload distribution across application instances when horizontally scaling out (adding app instances) and scaling in (removing instances). Since Kafka supports hundreds of thousands of partitions in a cluster, this over-partitioning strategy is a safe approach for most users.

#### Consumer Groups

Many consumers can be linked together by a _group_id_ to form a **consumer group** to consume a specific topic.
This is used to parellelize the read of a topic from many consumers. Kafka implement parallelism with partitions:
Kafka makes sure that each partition is consumed by exactly one consumer in the group. 
![consumer_group.png](img%2Fconsumer_group.png)

**Parallelism is given by the number of partition**: if a groups has more consumers than the number of partitions in the topic, the consumers will be idle until 
one of the assigned consumers is no longer available (**failover**).




https://medium.com/event-driven-utopia/understanding-kafka-topic-partitions-ae40f80552e8




### Event producers determine event partitioning


