// Caminho: server/src/main/java/pt/uevora/sd/server/controller/MetricController.java

package pt.uevora.sd.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uevora.sd.server.dto.AverageMetricDTO;
import pt.uevora.sd.server.dto.MetricDTO;
import pt.uevora.sd.server.model.Metric;
import pt.uevora.sd.server.service.MetricService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para consulta de métricas e ingestão via REST
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricController {

    private final MetricService metricService;

    /**
     * POST /api/metrics/ingest - Receber métricas via REST
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestMetric(@Valid @RequestBody MetricDTO metricDTO) {
        log.info("POST /api/metrics/ingest - Dispositivo: {}", metricDTO.getDeviceId());

        try {
            metricService.processMetric(metricDTO);
            return ResponseEntity.ok("Métrica recebida com sucesso");
        } catch (IllegalArgumentException e) {
            log.error("Erro ao processar métrica: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: " + e.getMessage());
        }
    }

    /**
     * GET /api/metrics/average - Consultar médias agregadas
     * Parâmetros:
     * - level: sala, departamento, piso, edificio
     * - id: ID da entidade
     * - from: data início (opcional)
     * - to: data fim (opcional)
     */
    @GetMapping("/average")
    public ResponseEntity<AverageMetricDTO> getAverageMetrics(
            @RequestParam(name = "level") String level,
            @RequestParam(name = "id") String id,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("GET /api/metrics/average - Level: {}, ID: {}, Period: {} to {}", level, id, from, to);

        try {
            AverageMetricDTO averages = metricService.getAverageMetrics(level, id, from, to);
            return ResponseEntity.ok(averages);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao calcular médias: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/metrics/raw - Consultar métricas brutas de um dispositivo
     * Parâmetros:
     * - deviceId: ID do dispositivo
     * - from: data início (opcional)
     * - to: data fim (opcional)
     */
    @GetMapping("/raw")
    public ResponseEntity<List<MetricDTO>> getRawMetrics(
            @RequestParam(name = "deviceId") String deviceId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("GET /api/metrics/raw - DeviceId: {}, Period: {} to {}", deviceId, from, to);

        List<MetricDTO> metrics = metricService.getRawMetrics(deviceId, from, to)
                .stream()
                .map(MetricDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(metrics);
    }
}