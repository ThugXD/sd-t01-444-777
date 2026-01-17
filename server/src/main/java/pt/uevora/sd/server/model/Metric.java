package pt.uevora.sd.server.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma métrica ambiental (temperatura e humidade)
 */
@Entity
@Table(name = "metrics", indexes = {
        @Index(name = "idx_device_timestamp", columnList = "device_id,timestamp"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    // Campos desnormalizados para facilitar consultas agregadas
    @Column(length = 100)
    private String room;

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String floor;

    @Column(length = 100)
    private String building;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}