# Local Apache Kafka + Kafka UI (macOS, Docker)

A reproducible, single-node **Apache Kafka 4.4.0** development environment using **KRaft** (no ZooKeeper), a persistent Docker volume, and **Provectus Kafka UI**.

> **Scope:** Local development only. This setup uses plaintext connections, no authentication, and one broker. Do not expose it to untrusted networks or use it as-is in production.

## Overview

| Component | Value |
| --- | --- |
| Kafka image | `apache/kafka:4.4.0` |
| Kafka UI image | `provectuslabs/kafka-ui:latest` |
| Kafka host bootstrap address | `localhost:9092` |
| Kafka address for containers on the Docker network | `kafka:29092` |
| Kafka UI | <http://localhost:8085> |
| Docker network | `kafka-network` |
| Persistent Docker volume | `kafka-data` |
| Kafka data directory | `/var/lib/kafka/data` |
| Example topic | `customers` |

**Connection layout:**

```text
Spring Boot / CLI on Mac ── localhost:9092 ──┐
                                              │
                                    Kafka container
                                    kafka:29092
                                              │
                          Docker network: kafka-network
                                              │
                                    Kafka UI container
                                    localhost:8085 (browser)

Kafka container ── /var/lib/kafka/data ── Docker volume: kafka-data
```

Kafka uses two advertised listeners because `localhost` inside a container refers to **that container**, not the Mac or another container. Host applications use `localhost:9092`; Kafka UI uses `kafka:29092`.

## Prerequisites

1. Install and launch **Docker Desktop** on macOS.
2. Verify the Docker engine is running:

```bash
docker --version
docker info
```

## 1. Download Kafka

```bash
docker pull apache/kafka:4.4.0
```

## 2. Create the network and persistent volume

Run these **once** for a fresh setup:

```bash
docker network create kafka-network
docker volume create kafka-data
```

Check them with:

```bash
docker network ls
docker volume ls
```

> If Docker says the network or volume already exists, reuse it. **Do not delete `kafka-data`** if you want to keep existing topics and messages.

## 3. Start Kafka

Copy the complete command:

```bash
docker run -d \
  --name kafka \
  --network kafka-network \
  -p 127.0.0.1:9092:9092 \
  -v kafka-data:/var/lib/kafka/data \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_LISTENERS=PLAINTEXT_HOST://:9092,PLAINTEXT_DOCKER://:29092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT_HOST://localhost:9092,PLAINTEXT_DOCKER://kafka:29092 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT_HOST:PLAINTEXT,PLAINTEXT_DOCKER:PLAINTEXT,CONTROLLER:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT_DOCKER \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LOG_DIRS=/var/lib/kafka/data \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:4.4.0
```

Key settings:

- `KAFKA_PROCESS_ROLES=broker,controller`: single-node KRaft broker and controller.
- `KAFKA_ADVERTISED_LISTENERS`: tells host clients to use `localhost:9092` and Docker-network clients to use `kafka:29092`.
- `KAFKA_LOG_DIRS=/var/lib/kafka/data`: **critical for persistence**. The mounted volume must match Kafka's actual log directory.
- `-p 127.0.0.1:9092:9092`: binds the broker's host port to loopback only.

> The `-e` lines are options to `docker run`, not separate shell commands. If you enter `-e ...` by itself, zsh reports `command not found: -e`.

### Verify Kafka

```bash
docker ps --filter 'name=kafka'
docker exec kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
docker exec kafka ls -la /var/lib/kafka/data
```

The topic list may initially be empty. In the data directory, look for `meta.properties` and `__cluster_metadata-0`. If Kafka is not healthy:

```bash
docker logs kafka --tail 100
```

## 4. Create the `customers` topic

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --create \
  --topic customers \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

Expected response: `Created topic customers.`

To list and inspect topics:

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --describe --topic customers --bootstrap-server localhost:9092
```

If `customers` already exists, skip the create command.

## 5. Download and start Kafka UI

```bash
docker pull provectuslabs/kafka-ui:latest

docker run -d \
  --name kafka-ui \
  --network kafka-network \
  -p 127.0.0.1:8085:8080 \
  -e KAFKA_CLUSTERS_0_NAME=local \
  -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:29092 \
  provectuslabs/kafka-ui:latest
```

Open **<http://localhost:8085>**.

Navigate to **local → Topics → customers**. The working setup showed the topic with one partition, replication factor one, and initially zero messages.

If the UI does not connect:

```bash
docker logs kafka-ui --tail 100
```

## 6. Produce sample messages

Start the console producer:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic customers
```

Enter one JSON record per line (press **Enter** after each):

```json
{"id":1,"name":"Rahul","email":"rahul@example.com"}
{"id":2,"name":"Priya","email":"priya@example.com"}
{"id":3,"name":"Amit","email":"amit@example.com"}
```

Press **Ctrl+C** to exit the producer.

## 7. Consume sample messages

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic customers \
  --from-beginning
```

The consumer should print the sample records. Press **Ctrl+C** to stop it.

In Kafka UI, open **local → Topics → customers → Messages**, select the earliest offset if necessary, and load the records.

## 8. Connect a Spring Boot application

For an application running **directly on your Mac**, add `spring-kafka` to your Maven dependencies:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

Configure `src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: customers-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

This example uses string keys and values; JSON can be sent as a string. Java-object JSON serialization requires additional configuration.

If Spring Boot runs **inside a container connected to `kafka-network`**, use `kafka:29092` as its bootstrap server instead.

## 9. Everyday commands

### Check status

```bash
docker ps --filter 'name=kafka'
```

### Stop both containers

```bash
docker stop kafka-ui kafka
```

### Start both existing containers

```bash
docker start kafka
docker start kafka-ui
```

### Restart a service

```bash
docker restart kafka
docker restart kafka-ui
```

### Read logs

```bash
docker logs kafka --tail 50
docker logs kafka-ui --tail 50
```

You only need `docker run` when creating a container. For subsequent sessions, start the existing containers instead.

## 10. Troubleshooting

| Symptom | Likely cause | Resolution |
| --- | --- | --- |
| Kafka UI logs mention `localhost:9092` connection failures | Broker advertises a host-only address to the UI container | Use the dual-listener Kafka configuration and `kafka:29092` for UI |
| UI cannot resolve or connect to `kafka` | Containers are not on the same Docker network | Attach/recreate both on `kafka-network` |
| Topics disappear after container removal | Kafka wrote outside the mounted persistent volume | Set `KAFKA_LOG_DIRS=/var/lib/kafka/data` before creating new data |
| `zsh: command not found: -e` | An environment option was run separately | Include all `-e` flags in the `docker run` command |
| Topic list is empty | No topics created yet | Create `customers` |
| Container name is already in use | Existing container has the same name | Use `docker start` or deliberately recreate it |

### What went wrong in the original attempt?

The initial Kafka container mounted `kafka-data` at `/var/lib/kafka/data`, but Kafka actually stored its metadata and logs in `/tmp/kafka-logs`. As a result, the original `customers` topic was not preserved when that container was removed.

We verified the actual directory by inspecting `/tmp/kafka-logs`, which contained `meta.properties` and `__cluster_metadata-0`. Before recreating the later broker, we copied its current metadata into the mounted volume:

```bash
docker exec kafka sh -c 'cp -a /tmp/kafka-logs/. /var/lib/kafka/data/'
```

We then stopped and removed that container and recreated Kafka with `KAFKA_LOG_DIRS=/var/lib/kafka/data`. **This copy/migration step is historical and is not required for a fresh setup.** Do not repeat it on a running, correctly configured broker; copying live Kafka data is not a safe general backup procedure.

For the UI, the original configuration used `host.docker.internal:9092`, but the broker advertised `localhost:9092`, leading to connection failures. Recreating the UI on `kafka-network` with `kafka:29092` fixed the connection.

### Recreate only Kafka UI (when changing its settings)

```bash
docker stop kafka-ui
docker rm kafka-ui

docker run -d \
  --name kafka-ui \
  --network kafka-network \
  -p 127.0.0.1:8085:8080 \
  -e KAFKA_CLUSTERS_0_NAME=local \
  -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:29092 \
  provectuslabs/kafka-ui:latest
```

Recreating **Kafka UI** does not remove the broker's data. Avoid deleting the `kafka-data` volume unless you explicitly want to erase Kafka's stored data.

## Final checklist

- [ ] Docker Desktop is running
- [ ] `kafka-network` exists
- [ ] `kafka-data` exists
- [ ] Kafka is running and responds at `localhost:9092`
- [ ] Kafka data is stored under `/var/lib/kafka/data`
- [ ] `customers` topic exists
- [ ] Kafka UI is running at <http://localhost:8085>
- [ ] Sample messages can be produced and consumed
- [ ] Spring Boot connects to the correct listener

---

**Result:** A local, persistent, single-node Kafka 4.4.0 broker with a browser-based Kafka UI, ready for development and testing.
