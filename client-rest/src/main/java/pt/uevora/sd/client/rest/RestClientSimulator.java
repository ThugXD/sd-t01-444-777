// Caminho: client-rest/src/main/java/pt/uevora/sd/client/rest/RestClientSimulator.java

package pt.uevora.sd.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulador de dispositivos com suporte HTTP que enviam dados via REST
 */
public class RestClientSimulator {

    private static final Logger log = LoggerFactory.getLogger(RestClientSimulator.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    // Configurações REST
    private static final String SERVER_URL = "http://localhost:8080/api/metrics/ingest";

    // Configurações de simulação
    private static final int INTERVAL_SECONDS = 12; // Intervalo entre envios
    private static final int NUM_DEVICES = 3; // Número de dispositivos a simular
    private static final int MAX_RETRIES = 3; // Número máximo de tentativas

    // Dispositivos virtuais
    private static final List<DeviceConfig> DEVICES = Arrays.asList(
            new DeviceConfig("rest-device-001", "F204", "Biologia", "Piso2", "EdificioVI"),
            new DeviceConfig("rest-device-002", "G101", "Biblioteca", "Piso1", "EdificioVII"),
            new DeviceConfig("rest-device-003", "H305", "Laboratorio", "Piso3", "EdificioVIII")
    );

    // Estado dos dispositivos
    private static final Map<String, DeviceState> deviceStates = new HashMap<>();

    public static void main(String[] args) {
        log.info("=== Iniciando Simulador REST ===");
        log.info("Servidor: {}", SERVER_URL);
        log.info("Dispositivos: {}", NUM_DEVICES);
        log.info("Intervalo: {} segundos", INTERVAL_SECONDS);

        // Inicializar estados
        for (DeviceConfig device : DEVICES) {
            deviceStates.put(device.deviceId, new DeviceState());
        }

        // Criar cliente HTTP
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Agendar envio de dados
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_DEVICES);

        for (int i = 0; i < NUM_DEVICES && i < DEVICES.size(); i++) {
            DeviceConfig config = DEVICES.get(i);
            executor.scheduleAtFixedRate(() -> {
                try {
                    sendMetric(httpClient, config);
                } catch (Exception e) {
                    log.error("Erro ao enviar métrica do dispositivo {}: {}",
                            config.deviceId, e.getMessage());
                }
            }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);

            log.info("Dispositivo {} iniciado", config.deviceId);
        }

        // Hook para desligar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Desligando cliente REST...");
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error("Erro ao fechar cliente HTTP", e);
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
     * Envia uma métrica via REST com retry logic
     */
    private static void sendMetric(CloseableHttpClient httpClient, DeviceConfig config) {
        int retries = 0;
        boolean success = false;

        while (retries < MAX_RETRIES && !success) {
            try {
                DeviceState state = deviceStates.get(config.deviceId);

                // Gerar dados sintéticos
                double temperature = state.generateTemperature();
                double humidity = state.generateHumidity();
                LocalDateTime timestamp = LocalDateTime.now();

                // Criar payload JSON
                Map<String, Object> payload = new HashMap<>();
                payload.put("deviceId", config.deviceId);
                payload.put("temperature", Math.round(temperature * 100.0) / 100.0);
                payload.put("humidity", Math.round(humidity * 100.0) / 100.0);
                payload.put("timestamp", timestamp.format(DateTimeFormatter.ISO_DATE_TIME));

                String jsonPayload = objectMapper.writeValueAsString(payload);

                // Criar requisição HTTP POST
                HttpPost httpPost = new HttpPost(SERVER_URL);
                httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

                // Enviar requisição
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getCode();

                    if (statusCode == 200) {
                        log.info("Métrica enviada com sucesso - Device: {}, Temp: {}°C, Hum: {}%",
                                config.deviceId, payload.get("temperature"), payload.get("humidity"));
                        success = true;
                    } else if (statusCode == 400) {
                        log.warn("Métrica rejeitada (400) - Device: {}", config.deviceId);
                        success = true; // Não tentar novamente para erros 400
                    } else {
                        log.warn("Resposta HTTP {} - Device: {} (tentativa {}/{})",
                                statusCode, config.deviceId, retries + 1, MAX_RETRIES);
                        retries++;
                    }
                }

            } catch (Exception e) {
                retries++;
                log.error("Erro ao enviar métrica do dispositivo {} (tentativa {}/{}): {}",
                        config.deviceId, retries, MAX_RETRIES, e.getMessage());

                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000 * retries); // Backoff exponencial
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            log.error("Falha ao enviar métrica após {} tentativas - Device: {}",
                    MAX_RETRIES, config.deviceId);
        }
    }

    /**
     * Cria ObjectMapper configurado
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
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
        private double currentTemp = 19.0 + random.nextDouble() * 6; // 19-25°C
        private double currentHum = 45.0 + random.nextDouble() * 25;  // 45-70%

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