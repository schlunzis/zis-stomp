package org.schlunzis.zis.stomp.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonMessageConverterTest {

    @ValueSource(classes = {
            Jackson2MessageConverter.class,
            Jackson3MessageConverter.class,
            //AvajeJsonbMessageConverter.class, TODO find out how to use in test environment only
    })
    @ParameterizedTest
    void test(Class<MessageConverter> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MessageConverter messageConverter = clazz.getConstructor().newInstance();

        RecordModel recordModel = new RecordModel("asdf", 42);
        String recordModelJson = "{\"id\":\"asdf\",\"a\":42}";

        String json = messageConverter.convertToString(recordModel);
        assertEquals(recordModelJson, json);

        RecordModel model = messageConverter.convertToType(recordModelJson, RecordModel.class);
        assertEquals(recordModel, model);
    }

    public record RecordModel(
            String id,
            int a
    ) {
    }

}
