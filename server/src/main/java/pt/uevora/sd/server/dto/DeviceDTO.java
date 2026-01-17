// Caminho: server/src/main/java/pt/uevora/sd/server/dto/DeviceDTO.java

package pt.uevora.sd.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.uevora.sd.server.model.Device;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de dispositivos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {

    @NotBlank(message = "ID do dispositivo é obrigatório")
    private String id;

    @NotNull(message = "Protocolo é obrigatório")
    private Device.ProtocolType protocol;

    @NotBlank(message = "Sala é obrigatória")
    private String room;

    @NotBlank(message = "Departamento é obrigatório")
    private String department;

    private String floor;

    @NotBlank(message = "Edifício é obrigatório")
    private String building;

    private Device.DeviceStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converte entidade Device para DTO
     */
    public static DeviceDTO fromEntity(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .protocol(device.getProtocol())
                .room(device.getRoom())
                .department(device.getDepartment())
                .floor(device.getFloor())
                .building(device.getBuilding())
                .status(device.getStatus())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    /**
     * Converte DTO para entidade Device
     */
    public Device toEntity() {
        return Device.builder()
                .id(this.id)
                .protocol(this.protocol)
                .room(this.room)
                .department(this.department)
                .floor(this.floor)
                .building(this.building)
                .status(this.status != null ? this.status : Device.DeviceStatus.ACTIVE)
                .build();
    }
}