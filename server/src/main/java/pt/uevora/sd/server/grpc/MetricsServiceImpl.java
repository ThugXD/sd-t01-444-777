// Caminho: server/src/main/java/pt/uevora/sd/server/grpc/MetricsServiceImpl.java

package pt.uevora.sd.server.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.uevora.sd.grpc.MetricData;
import pt.uevora.sd.grpc.MetricResponse;
import pt.uevora.sd.grpc.MetricsServiceGrpc;
import pt.uevora.sd.grpc.MetricBatchRequest;
import pt.uevora.sd.server.dto.MetricDTO;
import pt.uevora.sd.server.service.MetricService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementação do serviço gRPC para receber métricas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsServiceImpl extends MetricsServiceGrpc.MetricsServiceImplBase {

    private final MetricService metricService;

    /**
     * Recebe uma métrica única via gRPC
     */
    @Override
    public void sendMetric(MetricData request, StreamObserver<MetricResponse> responseObserver) {
        log.info("gRPC: Métrica recebida - Device: {}", request.getDeviceId());

        try {
            // Converter MetricData (protobuf) para MetricDTO
            MetricDTO metricDTO = convertToDTO(request);

            // Processar métrica
            metricService.processMetric(metricDTO);

            // Resposta de sucesso
            MetricResponse response = MetricResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Métrica recebida com sucesso")
                    .setProcessedCount(1)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: Métrica processada com sucesso - Device: {}", request.getDeviceId());

        } catch (Exception e) {
            log.error("gRPC: Erro ao processar métrica: {}", e.getMessage(), e);

            MetricResponse response = MetricResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Erro: " + e.getMessage())
                    .setProcessedCount(0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Recebe múltiplas métricas em batch via gRPC
     */
    @Override
    public void sendMetricsBatch(MetricBatchRequest request, StreamObserver<MetricResponse> responseObserver) {
        log.info("gRPC: Batch recebido - {} métricas", request.getMetricsCount());

        int processedCount = 0;
        StringBuilder errors = new StringBuilder();

        try {
            for (MetricData metricData : request.getMetricsList()) {
                try {
                    MetricDTO metricDTO = convertToDTO(metricData);
                    metricService.processMetric(metricDTO);
                    processedCount++;
                } catch (Exception e) {
                    log.error("Erro ao processar métrica do dispositivo {}: {}",
                            metricData.getDeviceId(), e.getMessage());
                    errors.append(metricData.getDeviceId()).append(": ").append(e.getMessage()).append("; ");
                }
            }

            String message = processedCount == request.getMetricsCount()
                    ? "Todas as métricas processadas com sucesso"
                    : String.format("%d/%d métricas processadas. Erros: %s",
                    processedCount, request.getMetricsCount(), errors);

            MetricResponse response = MetricResponse.newBuilder()
                    .setSuccess(processedCount > 0)
                    .setMessage(message)
                    .setProcessedCount(processedCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: Batch processado - {}/{} métricas", processedCount, request.getMetricsCount());

        } catch (Exception e) {
            log.error("gRPC: Erro fatal ao processar batch: {}", e.getMessage(), e);

            MetricResponse response = MetricResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Erro fatal: " + e.getMessage())
                    .setProcessedCount(processedCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Converte MetricData (protobuf) para MetricDTO
     */
    private MetricDTO convertToDTO(MetricData metricData) {
        LocalDateTime timestamp = LocalDateTime.parse(
                metricData.getTimestamp(),
                DateTimeFormatter.ISO_DATE_TIME
        );

        return MetricDTO.builder()
                .deviceId(metricData.getDeviceId())
                .temperature(metricData.getTemperature())
                .humidity(metricData.getHumidity())
                .timestamp(timestamp)
                .build();
    }
}