package com.example.sadjoum.transformer;

import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.messaging.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class NewlineSplitter extends AbstractMessageSplitter {

    @Override
    protected Object splitMessage(Message<?> message) {
        File file = (File) message.getPayload();
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
