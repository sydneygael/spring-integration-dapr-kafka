package com.example.sadjoum.transformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.messaging.Message;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public class NewlineSplitter extends AbstractMessageSplitter {
    @Override
    protected Object splitMessage(Message<?> message) {
        // Récupérer le contenu du message
        String payload = (String) message.getPayload();

        // Séparer le contenu par les sauts de ligne
        String[] lines = payload.split("\\r?\\n");

        /* Retourner une liste de lignes */
       var list = Arrays.asList(lines);
        return new ArrayList<>(list);
    }
}
