package org.schlunzis.zis.stomp.processor;

import org.schlunzis.zis.stomp.client.StompPublisher;
import org.schlunzis.zis.stomp.client.Topic;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor9;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "org.schlunzis.zis.stomp.client.StompPublisher"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StompClientPublisherProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() > 1) {
            throw new IllegalStateException();
        }
        if (annotations.isEmpty()) {
            return false;
        }

        TypeElement typeElement = annotations.iterator().next();
        roundEnv.getElementsAnnotatedWith(typeElement)
                .forEach(this::processStompPublisher);
        return true;
    }

    private void processStompPublisher(Element element) {
        Publisher publisher = createPublisher(element);
        ClassGenerator classGenerator = new ClassGenerator(publisher);
        try {
            classGenerator.generate(processingEnv);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Publisher createPublisher(Element element) {
        final TypeMirror typeMirror = element.asType();
        final StompPublisher sp = element.getAnnotation(StompPublisher.class);
        final String destinationPrefix = sp.destinationPrefix();
        String mirrorPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        final String packageName = sp.packageName().isEmpty() ? mirrorPackageName : sp.packageName();
        final String typeName = sp.typeName().isEmpty()
                ? element.getSimpleName() + "Impl"
                : sp.typeName();

        List<Subscriber> subscribers = element.getEnclosedElements().stream()
                .filter(e -> e.getAnnotation(Topic.class) != null)
                .map(e -> {
                    final TypeMirror em = e.asType();

                    System.out.println("Processing method: " + e.getSimpleName() + " with type: " + em);
                    Topic topicAnnotation = e.getAnnotation(Topic.class);
                    System.out.println("Found Topic annotation with value: " + topicAnnotation.value());
                    final String topic = destinationPrefix + topicAnnotation.value();
                    final String methodName = e.getSimpleName().toString();
                    final String parameterType = em.accept(new SimpleTypeVisitor9<String, Void>() {
                        @Override
                        public String visitExecutable(ExecutableType t, Void p) {
                            List<? extends TypeMirror> parameterTypes = t.getParameterTypes();
                            if (parameterTypes.size() != 1) {
                                throw new IllegalStateException("Method must have exactly one parameter");
                            }
                            return parameterTypes.get(0).toString();
                        }
                    }, null);

                    return new Subscriber(
                            topic,
                            methodName,
                            parameterType,
                            "arg0"
                    );
                })
                .toList();

        return new Publisher(
                packageName,
                List.of(),
                typeName,
                typeMirror.toString(),
                subscribers
        );
    }

}
