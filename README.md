# ski-data-system

A Java system for ingesting high-volume skier lift-ride events. A REST API on Tomcat accepts events and hands them to RabbitMQ, a consumer processes them separately, and a multithreaded client generates the load.

See [docs/architecture.md](docs/architecture.md) for the design and a diagram.

## Tech stack

Java 17, Maven, Servlet API on Tomcat, RabbitMQ, DynamoDB (AWS SDK), deployed on AWS EC2 in the original project.

| Module | What it does |
| --- | --- |
| `server/` | REST servlet API that receives lift-ride events and publishes them to RabbitMQ |
| `client/` | Multithreaded load generator that sends 200,000 requests and reports latency and throughput |
| `consumer/` | RabbitMQ consumer that batches events into DynamoDB |
| `api-client/` | Swagger-generated API client and the shared `LiftRide` model |
| `benchmarks/` | Load-test script and results table |

## How to run

Build everything (Java 17+ and Maven required):

```bash
git clone https://github.com/yanghansin/ski-data-system.git
cd ski-data-system
mvn -q package
```

Start RabbitMQ, for example with Docker:

```bash
docker run -d --name rabbitmq -p 5672:5672 rabbitmq:3
```

Deploy `server/target/server.war` to Tomcat 9. Tomcat 10+ won't work because the server uses the `javax.servlet` API.

Start the consumer, which needs AWS credentials and an existing `SkierLiftRideTable` table in DynamoDB:

```bash
java -jar consumer/target/consumer.jar
```

Run the load test:

```bash
SKI_API_BASE_PATH=http://localhost:8080/server java -jar client/target/client.jar
```

### Configuration

All connection details come from environment variables. None are stored in the code.

| Variable | Used by | Default |
| --- | --- | --- |
| `RABBITMQ_HOST` | server, consumer | `localhost` |
| `RABBITMQ_USER` | server, consumer | `guest` |
| `RABBITMQ_PASS` | server, consumer | `guest` |
| `AWS_REGION` | consumer | `us-west-2` |
| `SKI_API_BASE_PATH` | client | none (required) |

## Performance

Reported from the original project: throughput of ~2,000 → ~4,000 req/sec. These figures have not been re-measured from this repository.

New numbers go in [benchmarks/results.md](benchmarks/results.md) only after a real run.

## Project status

The code is imported from the original project, with hardcoded hosts and credentials replaced by environment variables and a few bug fixes. It compiles and packages with Maven. It has not been run end to end since the import, and it has no automated tests yet.

## Roadmap

- Run the load test against a deployed server and fill in the results table.
- Acknowledge messages after the DynamoDB write so failures can't lose events.
- Retry unprocessed DynamoDB batch items.
- Add unit tests for request validation.

## License

[MIT](LICENSE)
