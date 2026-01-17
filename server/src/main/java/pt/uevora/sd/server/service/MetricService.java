// Caminho: server/src/main/java/pt/uevora/sd/server/service/MetricService.java

package pt.uevora.sd.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uevora.sd.server.dto.AverageMetricDTO;
import pt.uevora.sd.server.dto.MetricDTO;
import pt.uevora.sd.server.model.Device;
import pt.uevora.sd.server.model.Metric;
import pt.uevora.sd.server.repository.DeviceRepository;
import pt.uevora.sd.server.repository.MetricRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para processamento e consulta de métricas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final MetricRepository metricRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    /**
     * Processa e armazena uma métrica
     */
    @Transactional
    public Metric processMetric(MetricDTO metricDTO) {
        log.debug("Processando métrica do dispositivo: {}", metricDTO.getDeviceId());

        // Verificar se o dispositivo está registado e ativo
        if (!deviceService.isDeviceActive(metricDTO.getDeviceId())) {
            log.warn("Métrica descartada - Dispositivo inativo ou não registado: {}", metricDTO.getDeviceId());
            throw new IllegalArgumentException("Dispositivo não está ativo: " + metricDTO.getDeviceId());
        }

        // Buscar informações do dispositivo para desnormalizar
        Device device = deviceRepository.findById(metricDTO.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado"));

        // Criar métrica com dados desnormalizados
        Metric metric = metricDTO.toEntity();
        metric.setRoom(device.getRoom());
        metric.setDepartment(device.getDepartment());
        metric.setFloor(device.getFloor());
        metric.setBuilding(device.getBuilding());

        // Salvar métrica
        Metric savedMetric = metricRepository.save(metric);
        log.info("Métrica salva com sucesso - Device: {}, Temp: {}°C, Hum: {}%",
                metricDTO.getDeviceId(), metricDTO.getTemperature(), metricDTO.getHumidity());

        return savedMetric;
    }

    /**
     * Busca métricas brutas de um dispositivo
     */
    public List<Metric> getRawMetrics(String deviceId, LocalDateTime from, LocalDateTime to) {
        log.debug("Buscando métricas brutas - Device: {}, Period: {} to {}", deviceId, from, to);

        if (from == null || to == null) {
            // Default: últimas 24 horas
            to = LocalDateTime.now();
            from = to.minusHours(24);
        }

        return metricRepository.findByDeviceIdAndTimestampBetween(deviceId, from, to);
    }

    /**
     * Calcula média por nível (sala, departamento, piso, edifício)
     */
    public AverageMetricDTO getAverageMetrics(String level, String id, LocalDateTime from, LocalDateTime to) {
        log.debug("Calculando médias - Level: {}, ID: {}, Period: {} to {}", level, id, from, to);

        if (from == null || to == null) {
            to = LocalDateTime.now();
            from = to.minusHours(24);
        }

        Object[] result;
        List<Metric> metrics;

        switch (level.toLowerCase()) {
            case "sala":
                result = metricRepository.findAverageByRoom(id, from, to);
                metrics = metricRepository.findByRoomAndTimestampBetween(id, from, to);
                break;
            case "departamento":
                result = metricRepository.findAverageByDepartment(id, from, to);
                metrics = metricRepository.findByDepartmentAndTimestampBetween(id, from, to);
                break;
            case "piso":
                result = metricRepository.findAverageByFloor(id, from, to);
                metrics = metricRepository.findByFloorAndTimestampBetween(id, from, to);
                break;
            case "edificio":
                result = metricRepository.findAverageByBuilding(id, from, to);
                metrics = metricRepository.findByBuildingAndTimestampBetween(id, from, to);
                break;
            default:
                throw new IllegalArgumentException("Nível inválido: " + level);
        }

        Double avgTemp = result[0] != null ? (Double) result[0] : 0.0;
        Double avgHum = result[1] != null ? (Double) result[1] : 0.0;

        return AverageMetricDTO.builder()
                .level(level)
                .id(id)
                .avgTemperature(avgTemp)
                .avgHumidity(avgHum)
                .from(from)
                .to(to)
                .count((long) metrics.size())
                .build();
    }
}