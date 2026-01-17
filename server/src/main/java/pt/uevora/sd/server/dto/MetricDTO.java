// Caminho: server/src/main/java/pt/uevora/sd/server/dto/MetricDTO.java

package pt.uevora.sd.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.uevora.sd.server.model.Metric;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de métricas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricDTO {

    @NotBlank(message = "ID do dispositivo é obrigatório")
    private String deviceId;

    @NotNull(message = "Temperatura é obrigatória")
    private Double temperature;

    @NotNull(message = "Humidade é obrigatória")
    private Double humidity;

    @NotNull(message = "Timestamp é obrigatório")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Converte entidade Metric para DTO
     */
    public static MetricDTO fromEntity(Metric metric) {
        return MetricDTO.builder()
                .deviceId(metric.getDeviceId())
                .temperature(metric.getTemperature())
                .humidity(metric.getHumidity())
                .timestamp(metric.getTimestamp())
                .build();
    }

    /**
     * Converte DTO para entidade Metric (sem informações de localização)
     */
    public Metric toEntity() {
        return Metric.builder()
                .deviceId(this.deviceId)
                .temperature(this.temperature)
                .humidity(this.humidity)
                .timestamp(this.timestamp)
                .build();
    }
}