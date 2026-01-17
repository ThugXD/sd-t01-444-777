// Caminho: server/src/main/java/pt/uevora/sd/server/ServerApplication.java

package pt.uevora.sd.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do servidor de monitorização ambiental
 */
@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
		System.out.println("========================================");
		System.out.println("Servidor de Monitorização Ambiental");
		System.out.println("Universidade de Évora");
		System.out.println("========================================");
		System.out.println("REST API: http://localhost:8080");
		System.out.println("gRPC Server: localhost:9090");
		System.out.println("MQTT Broker: tcp://localhost:1883");
		System.out.println("========================================");
	}
}