// Caminho: server/src/main/java/pt/uevora/sd/server/mqtt/MqttMessageHandler.java

package pt.uevora.sd.server.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import pt.uevora.sd.server.dto.MetricDTO;
import pt.uevora.sd.server.service.MetricService;

/**
 * Handler para processar mensagens MQTT recebidas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttMessageHandler {

    private final MetricService metricService;
    private final ObjectMapper objectMapper;

    /**
     * Processa mensagens recebidas via MQTT
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            log.debug("Mensagem MQTT recebida: {}", payload);

            // Converter JSON para MetricDTO
            MetricDTO metricDTO = objectMapper.readValue(payload, MetricDTO.class);

            // Processar e armazenar métrica
            metricService.processMetric(metricDTO);

            log.info("Métrica MQTT processada com sucesso - Device: {}", metricDTO.getDeviceId());

        } catch (Exception e) {
            log.error("Erro ao processar mensagem MQTT: {}", e.getMessage(), e);
        }
    }
}