// Caminho: server/src/main/java/pt/uevora/sd/server/dto/AverageMetricDTO.java

package pt.uevora.sd.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para retornar médias de temperatura e humidade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageMetricDTO {

    private String level;          // sala, departamento, piso, edificio
    private String id;              // ID da entidade (ex: sala_123)
    private Double avgTemperature;
    private Double avgHumidity;
    private LocalDateTime from;
    private LocalDateTime to;
    private Long count;             // Número de métricas consideradas
}