package org.example.kafka;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class KafkaProducer {

	@Outgoing("out")
	public Multi<String> generatePeople() {
        return Multi.createFrom().items("test1", "test2");
    }

}

