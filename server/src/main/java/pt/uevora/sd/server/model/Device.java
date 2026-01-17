
package pt.uevora.sd.server.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um dispositivo IoT no sistema
 */
@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProtocolType protocol;

    @Column(nullable = false, length = 100)
    private String room;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String floor;

    @Column(nullable = false, length = 100)
    private String building;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DeviceStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Tipo de protocolo usado pelo dispositivo
     */
    public enum ProtocolType {
        MQTT,
        GRPC,
        REST
    }

    /**
     * Estado do dispositivo
     */
    public enum DeviceStatus {
        ACTIVE,
        INACTIVE
    }
}