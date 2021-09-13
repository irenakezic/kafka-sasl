package org.example.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.example.kafka.containers.KafkaContainer;
import org.example.kafka.containers.KerberosContainer;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KafkaSaslTestResource implements QuarkusTestResourceLifecycleManager {

	private final Logger log = Logger.getLogger(KafkaSaslTestResource.class);
	private static final Integer INFINISPAN_PORT = 11222;

	private KafkaContainer kafka;
	private GenericContainer infinispan;
	private KerberosContainer kerberos;


	@Override
	public Map<String, String> start() {

		Map<String, String> properties = new HashMap<>();

		//Start kerberos container
		kerberos = new KerberosContainer("gcavalcante8808/krb5-server");
		kerberos.start();
		log.info(kerberos.getLogs());
		kerberos.createTestPrincipals();
		kerberos.createKrb5File();
		properties.put("java.security.krb5.conf", "src/test/resources/krb5.conf");

		//Start infinispan container
		infinispan =
				new GenericContainer("infinispan/server:12.1.7.Final")
				.waitingFor(new LogMessageWaitStrategy().withRegEx(".*Infinispan Server.*started in.*\\s"))
				.withStartupTimeout(Duration.ofMillis(20000))
				.withEnv("USER","admin")
				.withEnv("PASS","password");
		infinispan.start();
		String infinispanHost = infinispan.getContainerIpAddress() + ":" + infinispan.getMappedPort(INFINISPAN_PORT);
		properties.put("quarkus.infinispan-client.server-list", infinispanHost);

		//Start kafka container
		kafka = new KafkaContainer();
		kafka.start();
		log.info(kafka.getLogs());
		properties.put("kafka.bootstrap.servers", kafka.getBootstrapServers());

		return properties;
	}

	@Override
	public void stop() {

		if(infinispan != null) {
			infinispan.close();
			infinispan.stop();
		}

		if (kafka != null) {
			kafka.close();
			kafka.stop();
		}

		if(kerberos != null) {
			kerberos.close();
			kerberos.stop();
		}

	}
}
