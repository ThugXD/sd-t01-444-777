// Caminho: client-grpc/src/main/java/pt/uevora/sd/client/grpc/GrpcClientSimulator.java

package pt.uevora.sd.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.uevora.sd.grpc.MetricData;
import pt.uevora.sd.grpc.MetricResponse;
import pt.uevora.sd.grpc.MetricsServiceGrpc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulador de dispositivos edge/gateways que enviam dados via gRPC
 */
public class GrpcClientSimulator {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientSimulator.class);

    // Configurações gRPC
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;

    // Configurações de simulação
    private static final int INTERVAL_SECONDS = 15; // Intervalo entre envios
    private static final int NUM_DEVICES = 2; // Número de dispositivos a simular

    // Dispositivos virtuais
    private static final List<DeviceConfig> DEVICES = Arrays.asList(
            new DeviceConfig("grpc-gateway-001", "D301", "Engenharia", "Piso3", "EdificioIV"),
            new DeviceConfig("grpc-gateway-002", "E102", "Quimica", "Piso1", "EdificioV")
    );

    // Estado dos dispositivos
    private static final Map<String, DeviceState> deviceStates = new HashMap<>();

    public static void main(String[] args) {
        log.info("=== Iniciando Simulador gRPC ===");
        log.info("Servidor: {}:{}", SERVER_HOST, SERVER_PORT);
        log.info("Dispositivos: {}", NUM_DEVICES);
        log.info("Intervalo: {} segundos", INTERVAL_SECONDS);

        // Inicializar estados
        for (DeviceConfig device : DEVICES) {
            deviceStates.put(device.deviceId, new DeviceState());
        }

        // Criar canal gRPC
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        MetricsServiceGrpc.MetricsServiceBlockingStub stub =
                MetricsServiceGrpc.newBlockingStub(channel);

        log.info("Canal gRPC criado");

        // Agendar envio de dados
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_DEVICES);

        for (int i = 0; i < NUM_DEVICES && i < DEVICES.size(); i++) {
            DeviceConfig config = DEVICES.get(i);
            executor.scheduleAtFixedRate(() -> {
                try {
                    sendMetric(stub, config);
                } catch (Exception e) {
                    log.error("Erro ao enviar métrica do dispositivo {}: {}",
                            config.deviceId, e.getMessage());
                }
            }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);

            log.info("Dispositivo {} iniciado", config.deviceId);
        }

        // Hook para desligar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Desligando cliente gRPC...");
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor.shutdown();
        }));

        // Manter aplicação rodando
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.error("Aplicação interrompida", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Envia uma métrica via gRPC
     */
    private static void sendMetric(MetricsServiceGrpc.MetricsServiceBlockingStub stub,
                                   DeviceConfig config) {
        try {
            DeviceState state = deviceStates.get(config.deviceId);

            // Gerar dados sintéticos
            double temperature = state.generateTemperature();
            double humidity = state.generateHumidity();
            LocalDateTime timestamp = LocalDateTime.now();

            // Criar mensagem gRPC
            MetricData metricData = MetricData.newBuilder()
                    .setDeviceId(config.deviceId)
                    .setTemperature(Math.round(temperature * 100.0) / 100.0)
                    .setHumidity(Math.round(humidity * 100.0) / 100.0)
                    .setTimestamp(timestamp.format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

            // Enviar para o servidor (chamada síncrona)
            MetricResponse response = stub.sendMetric(metricData);

            if (response.getSuccess()) {
                log.info("Métrica enviada com sucesso - Device: {}, Temp: {}°C, Hum: {}%",
                        config.deviceId, metricData.getTemperature(), metricData.getHumidity());
            } else {
                log.warn("Métrica rejeitada - Device: {}, Mensagem: {}",
                        config.deviceId, response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC ao enviar métrica do dispositivo {}: {}",
                    config.deviceId, e.getStatus());
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar métrica: {}", e.getMessage(), e);
        }
    }

    /**
     * Configuração de um dispositivo
     */
    static class DeviceConfig {
        String deviceId;
        String room;
        String department;
        String floor;
        String building;

        DeviceConfig(String deviceId, String room, String department, String floor, String building) {
            this.deviceId = deviceId;
            this.room = room;
            this.department = department;
            this.floor = floor;
            this.building = building;
        }
    }

    /**
     * Estado de um dispositivo (para variação gradual)
     */
    static class DeviceState {
        private static final Random random = new Random();
        private double currentTemp = 18.0 + random.nextDouble() * 7; // 18-25°C
        private double currentHum = 40.0 + random.nextDouble() * 30;  // 40-70%

        double generateTemperature() {
            currentTemp += (random.nextDouble() - 0.5);
            currentTemp = Math.max(15.0, Math.min(30.0, currentTemp));
            return currentTemp;
        }

        double generateHumidity() {
            currentHum += (random.nextDouble() - 0.5) * 4;
            currentHum = Math.max(30.0, Math.min(80.0, currentHum));
            return currentHum;
        }
    }
}