# zis-stomp

A STOMP client written in Java and built on Jakarta WebSockets.

This project is part of [Ze Impressive Schwifty](https://github.com/schlunzis/Ze-Impressive-Schwifty).

## Usage

### Maven

Currently, this library is not published to Maven Central. It will be published, when it is in a ready enough state. You
can use Jitpack to include it in your project.

#### Jitpack

To use Jitpack, add the following to your `pom.xml`:

<!-- @formatter:off -->
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
<!-- @formatter:on -->

Then, add the dependency:

<!-- @formatter:off -->
```xml
<dependency>
    <groupId>com.github.schlunzis.zis-stomp</groupId>
    <artifactId>client</artifactId>
    <version>COMMIT_HASH_OR_TAG</version>
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

This part is still a work in progress and not in a complete state. However, here is a small example of how it will look
like.

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

We are still working on the best way to register these subscribers with the `StompClient`.

#### Publishers

You can also use the annotation processor to generate publisher classes.

<!-- @formatter:off -->
```java
@StompPublisher
public interface MainPublisher {

    @Topic("/app/hello")
    void sendGreeting(String message);

    @Topic("/app/model")
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

To build the project, make sure you have Java 17 or higher installed. We are using Java 25 to build, but are compatible
with Java 17 and higher.

You can build the project using Maven:

```bash
    ./mvnw clean verify
```

## License

zis-stomp is released under the GNU General Public License v3.0
