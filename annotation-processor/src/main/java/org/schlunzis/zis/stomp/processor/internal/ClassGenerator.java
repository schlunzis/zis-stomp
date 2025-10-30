package org.schlunzis.zis.stomp.processor.internal;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.IOException;

/// Generates Java source files for STOMP client publishers based on the provided [Publisher] metadata.
///
/// @see Publisher
/// @see Subscriber
/// @since 1.0.0
public class ClassGenerator {

    private final Publisher publisher;

    /// Constructs a ClassGenerator with the specified Publisher metadata.
    ///
    /// @param publisher the Publisher metadata
    /// @since 1.0.0
    public ClassGenerator(Publisher publisher) {
        this.publisher = publisher;
    }

    /// Generates the Java source file for the publisher.
    ///
    /// @param processingEnv the processing environment
    /// @throws IOException if an I/O error occurs during file creation
    /// @since 1.0.0
    public void generate(ProcessingEnvironment processingEnv) throws IOException {
        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(publisher.packageName() + "." + publisher.name());

        try (var out = builderFile.openWriter()) {
            out.write("package " + publisher.packageName() + ";\n");
            out.write("\n");
            out.write("import org.schlunzis.zis.stomp.client.StompClient;\n");
            for (String importName : publisher.imports()) {
                out.write("import " + importName + ";\n");
            }
            out.write("\n");
            out.write("public class " + publisher.name() + " implements " + publisher.fullyQualifiedSuperType() + " {\n");
            out.write("\n");
            out.write("    private final StompClient stompClient;\n");
            out.write("\n");
            out.write("    public " + publisher.name() + "(StompClient stompClient) {\n");
            out.write("        this.stompClient = stompClient;\n");
            out.write("    }\n");
            out.write("\n");
            for (Subscriber subscriber : publisher.subscribers()) {
                out.write("    @Override\n");
                out.write("    public void " + subscriber.methodName() + "(" + subscriber.fullyQualifiedParameterType() + " " + subscriber.parameterName() + ") {\n");
                out.write("        stompClient.send(\"" + subscriber.topic() + "\", " + subscriber.parameterName() + ");\n");
                out.write("    }\n");
                out.write("\n");
            }
            out.write("}\n");
        }
    }

}
