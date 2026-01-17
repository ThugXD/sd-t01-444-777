// Caminho: client-mqtt/src/main/java/pt/uevora/sd/client/mqtt/MqttClientSimulator.java

package pt.uevora.sd.client.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulador de sensores IoT que enviam dados via MQTT
 */
public class MqttClientSimulator {

    private static final Logger log = LoggerFactory.getLogger(MqttClientSimulator.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    // Configurações MQTT
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String TOPIC_PREFIX = "uevora/sensors/";
    private static final int QOS = 1;

    // Configurações de simulação
    private static final int INTERVAL_SECONDS = 10; // Intervalo entre envios
    private static final int NUM_SENSORS = 3; // Número de sensores a simular

    // Sensores virtuais
    private static final List<SensorConfig> SENSORS = Arrays.asList(
            new SensorConfig("mqtt-sensor-001", "A101", "Informatica", "Piso1", "EdificioII"),
            new SensorConfig("mqtt-sensor-002", "B205", "Matematica", "Piso2", "EdificioI"),
            new SensorConfig("mqtt-sensor-003", "C103", "Fisica", "Piso1", "EdificioIII")
    );

    // Estado dos sensores (para variação gradual)
    private static final Map<String, SensorState> sensorStates = new HashMap<>();

    public static void main(String[] args) {
        log.info("=== Iniciando Simulador MQTT ===");
        log.info("Broker: {}", BROKER_URL);
        log.info("Sensores: {}", NUM_SENSORS);
        log.info("Intervalo: {} segundos", INTERVAL_SECONDS);

        // Inicializar estados dos sensores
        for (SensorConfig sensor : SENSORS) {
            sensorStates.put(sensor.deviceId, new SensorState());
        }

        // Criar clientes MQTT
        List<MqttClient> clients = new ArrayList<>();
        for (int i = 0; i < NUM_SENSORS && i < SENSORS.size(); i++) {
            try {
                SensorConfig config = SENSORS.get(i);
                MqttClient client = createMqttClient(config.deviceId);
                clients.add(client);
                scheduleSensorData(client, config);
                log.info("Sensor {} iniciado", config.deviceId);
            } catch (Exception e) {
                log.error("Erro ao criar cliente MQTT: {}", e.getMessage(), e);
            }
        }

        // Hook para desligar gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Desligando sensores MQTT...");
            for (MqttClient client : clients) {
                try {
                    if (client.isConnected()) {
                        client.disconnect();
                        client.close();
                    }
                } catch (MqttException e) {
                    log.error("Erro ao desconectar cliente", e);
                }
            }
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
     * Cria e conecta um cliente MQTT
     */
    private static MqttClient createMqttClient(String deviceId) throws MqttException {
        MqttClient client = new MqttClient(BROKER_URL, deviceId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        client.connect(options);
        log.info("Cliente MQTT conectado: {}", deviceId);

        return client;
    }

    /**
     * Agenda envio periódico de dados do sensor
     */
    private static void scheduleSensorData(MqttClient client, SensorConfig config) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                sendMetric(client, config);
            } catch (Exception e) {
                log.error("Erro ao enviar métrica do sensor {}: {}", config.deviceId, e.getMessage());
            }
        }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Envia uma métrica via MQTT
     */
    private static void sendMetric(MqttClient client, SensorConfig config) throws Exception {
        SensorState state = sensorStates.get(config.deviceId);

        // Gerar dados sintéticos com variação gradual
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

        // Publicar no tópico MQTT
        String topic = TOPIC_PREFIX + config.deviceId;
        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
        message.setQos(QOS);

        client.publish(topic, message);

        log.info("Métrica enviada - Device: {}, Temp: {}°C, Hum: {}%",
                config.deviceId, payload.get("temperature"), payload.get("humidity"));
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
     * Configuração de um sensor
     */
    static class SensorConfig {
        String deviceId;
        String room;
        String department;
        String floor;
        String building;

        SensorConfig(String deviceId, String room, String department, String floor, String building) {
            this.deviceId = deviceId;
            this.room = room;
            this.department = department;
            this.floor = floor;
            this.building = building;
        }
    }

    /**
     * Estado de um sensor (para variação gradual)
     */
    static class SensorState {
        private static final Random random = new Random();
        private double currentTemp = 20.0 + random.nextDouble() * 5; // 20-25°C
        private double currentHum = 50.0 + random.nextDouble() * 20;  // 50-70%

        double generateTemperature() {
            // Variação gradual: +/- 0.5°C
            currentTemp += (random.nextDouble() - 0.5);
            // Manter entre 15°C e 30°C
            currentTemp = Math.max(15.0, Math.min(30.0, currentTemp));
            return currentTemp;
        }

        double generateHumidity() {
            // Variação gradual: +/- 2%
            currentHum += (random.nextDouble() - 0.5) * 4;
            // Manter entre 30% e 80%
            currentHum = Math.max(30.0, Math.min(80.0, currentHum));
            return currentHum;
        }
    }
}