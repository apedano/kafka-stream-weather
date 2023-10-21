# 02 - Streams and tables

# quarkus-kafka-stream
Based on https://quarkus.io/guides/kafka-streams
https://www.baeldung.com/java-kafka-streams-vs-kafka-consumer

https://www.confluent.io/blog/kafka-streams-tables-part-1-event-streaming


Let us start with the basics: What is Apache Kafka?
Kafka is an event streaming platform. As such it provides, next to many other features, three key functionalities in a scalable, fault-tolerant, and reliable manner:

1. It lets you publish and subscribe to events
2. It lets you store events for as long as you want
3. It lets you process and analyze events

## Events

An event records **the fact that “something happened” in the world**.
Conceptually, an event has a key, value, and timestamp:

```xml
Event key: “Alice”
Event value: “Has arrived in Rome”
Event timestamp: “Dec. 3, 2019 at 9:06 a.m.”
```

## Streams

**Events are captured by an event streaming platform into event streams**.

**An event stream records the history of what has happened in the world as a sequence of events**.

An example stream is a sales ledger or the sequence of moves in a chess match.
With Kafka, such a stream may record the history of your business for hundreds of years. This history is an ordered sequence or chain of events, so we know which event happened before another event to infer causality (e.g., “White moved the e2 pawn to e4, then Black moved the e7 pawn to e5”). A stream thus represents both the past and the present: as we go from today to tomorrow—or from one millisecond to the next—new events are constantly being appended to the history.

## Table

Compared to an event stream, a **table** _represents the state of the world at a particular point in time_, typically “**now**.”
An example table is total sales or the current state of the board in a chess match. **A table is a view of an event stream**, **and this view is continuously being updated whenever a new event is captured**.


![kafka-streams-and-tables](img%2Fkafka-streams-and-tables.gif)

|               Streams               |         Tables                      |
|:-----------------------------------:|:-----------------------------------:|
|      _Immutable keyed events_       |           _Mutable data_            |
|            Only inserts             |    Inserts, updates and deletes     |
| Persistent, durable, fault tolerant | Persistent, durable, fault tolerant |



|                                           | Stream | Table     |
|-------------------------------------------|--------|-----------|
| First event with key bob arrives	         | Insert | Insert    | 
| Another event with key bob arrives        | Insert | Insert    |
| Event with key bob and value null arrives | Insert | Delete    |
| Event with key null arrives               | Insert | <ignored> |


## Stream table duality

Table and stream are two sides of the same coin:

### Stram to Table - **Aggregation**
A stream can be converted into a table by aggregating the events with a certain function. For every event a different table status will be generated

Here is an example using ``ksqlDB``
```sql
-- Continuously aggregating a stream into a table with a ksqlDB push query.
CREATE STREAM locationUpdatesStream ...;

CREATE TABLE locationsPerUser AS
   SELECT username, COUNT(*)
   FROM locationUpdatesStream
   GROUP BY username
   EMIT CHANGES;
```

and another one using `KTable`

```java
// Continuously aggregating a KStream into a KTable.
KStream<String, String> locationUpdatesStream = ...;

KTable<String, Long> locationsPerUser
   = locationUpdatesStream
       .groupBy((k, v) -> v.username)
       .count();
```

Here is how the stream, the table and the changelog are linked together

![changelog.gif](img%2Fchangelog.gif)

### Table to Stream - **CDC**
Change data capture, by capturing all changes to the table we can create a _change stream_ or a _chengelog_. The example is the _binary log_ of an RDBMS (all CUD queries to the database).

![stram_table_duality.png](img%2Fstram_table_duality.png)

