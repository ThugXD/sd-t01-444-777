// Caminho: server/src/main/java/pt/uevora/sd/server/config/GrpcConfig.java

package pt.uevora.sd.server.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.uevora.sd.server.grpc.MetricsServiceImpl;

import java.io.IOException;

/**
 * Configuração do servidor gRPC
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcConfig {

    @Value("${grpc.server.port}")
    private int grpcPort;

    private final MetricsServiceImpl metricsService;

    /**
     * Inicia o servidor gRPC quando a aplicação Spring Boot iniciar
     */
    @Bean
    public CommandLineRunner startGrpcServer() {
        return args -> {
            Server server = ServerBuilder.forPort(grpcPort)
                    .addService(metricsService)
                    .build()
                    .start();

            log.info("Servidor gRPC iniciado na porta: {}", grpcPort);

            // Adiciona hook para desligar o servidor gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Desligando servidor gRPC...");
                server.shutdown();
                log.info("Servidor gRPC desligado");
            }));

            // Mantém o servidor rodando
            new Thread(() -> {
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                    log.error("Erro ao aguardar término do servidor gRPC", e);
                    Thread.currentThread().interrupt();
                }
            }).start();
        };
    }
}