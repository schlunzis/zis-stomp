# zis-stomp

A STOMP client written in Java and built on Jakarta WebSockets.
This library aims to provide a STOMP 1.2 client for Java applications, with support for programmatic and
annotation-driven publishers and subscribers.
It was created due to a need for a STOMP client with a real `module-info` and JLink support.

Currently, the following features are implemented:

- Basic STOMP protocol support (CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, DISCONNECT)
- Receipts
- Annotation-driven publishers and subscribers
- Message conversion using Jackson
- Authentication

Parts of the protocol that are not implemented yet:

- Acknowledgements
- Transactions
- Heartbeats

This project is part of [Ze Impressive Schwifty](https://github.com/schlunzis/Ze-Impressive-Schwifty).

| Version  | Documentation                                                                                                                         |
|----------|---------------------------------------------------------------------------------------------------------------------------------------|
| snapshot | [Site](https://docs.schlunzis.org/zis-stomp/snapshot/site/) \| [JavaDoc](https://docs.schlunzis.org/zis-stomp/snapshot/site/apidocs/) |

## Usage

### Maven

This library is not published to Maven Central at this time.
It will be published, when it is in a ready enough state.
You can use the snapshot build to include it in your project.

#### Snapshot

To use the snapshot build, add the following to your `pom.xml`:

<!-- @formatter:off -->
```xml
<repositories>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
<!-- @formatter:on -->

Then, add the dependency:

<!-- @formatter:off -->
```xml
<dependency>
    <groupId>org.schlunzis.zis.stomp</groupId>
    <artifactId>client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
<!-- @formatter:on -->

### Programmatic Usage

Here is a simple example of how to use the STOMP client:

```java
void main() {
    StompClient client = StompClient.builder()
            .endpoint(new URI("ws://localhost:8080/ws"))
            .build();
    client.connect();

    client.send("/app/hello", "Hello, World!");
    Subscription sub = client.subscribe("/topic/greetings", String.class,
            message -> log.info("Received: " + message));
    // Do other stuff and listen for messages...
    client.unsubscribe(sub);

    client.close();
}
```

### Annotation Driven Usage

#### Subscribers

You can create subscriber classes using the `@StompSubscriber` annotation. Methods can be annotated with `@Topic` to
listen to specific topics.

<!-- @formatter:off -->
```java
@StompSubscriber(destinationPrefix = "/topic")
public class MainController {

    @Topic("/greetings")
    public void onMessage(String message) {
        log.info("Received message in Controller: {}", message);
    }

    @Topic("/model")
    public void onModelMessage(Model model) {
        log.info("Received model in Controller: {}", model);
    }

}
```
<!-- @formatter:on -->

Then you can register the subscriber with the client:

```java
void main() {
    StompClient client = StompClient.builder()
            .endpoint(new URI("ws://localhost:8080/ws"))
            .build();
    client.connect();

    MainController controller = new MainController();
    client.subscribe(controller);
    // Do other stuff and listen for messages...
    client.unsubscribe(controller);
}
```

Here, you do not get a subscription object back, as the client manages the subscriptions for you and you can simply
unsubscribe the using the controller instance.

#### Publishers

You can also use the annotation processor to generate publisher classes.

<!-- @formatter:off -->
```java
@StompPublisher(destinationPrefix = "/app")
public interface MainPublisher {

    @Topic("/hello")
    void sendGreeting(String message);

    @Topic("/model")
    void sendModel(Model model);

}
```
<!-- @formatter:on -->

This will generate a class `MainPublisherImpl`, which you can use to send messages.

```java
void main() {
    StompClient client = StompClient.builder()
            .endpoint(new URI("ws://localhost:8080/ws"))
            .build();
    client.connect();

    MainPublisher publisher = new MainPublisherImpl(client);
    publisher.sendGreeting("Hello, World!");
    publisher.sendModel(new Model("example"));
}
```

This allows for a clean definition of topics to send messages to and receive messages from.

## Building

To build the project, make sure you have Java 25 or higher installed.
If you do not skip the tests, you have the mock server running in the background.

You can build the project using Maven:

```bash
./mvnw clean install
```

### Tests

To run the integration tests, you can use the following Maven command:

```bash
./mvnw clean verify -P integration-test
```

### Reproducible Builds

Maven is configured to produce reproducible builds.
If you want to verify that the build you have is correct, you have to set the `project.build.outputTimestamp` property
to the time of the commit you want to check against and of course check out the correct commit.
See [here](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) for more information.

## License

zis-stomp is released under the GNU General Public License v3.0
