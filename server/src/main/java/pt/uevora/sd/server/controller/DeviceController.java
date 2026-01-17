// Caminho: server/src/main/java/pt/uevora/sd/server/controller/DeviceController.java

package pt.uevora.sd.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uevora.sd.server.dto.DeviceDTO;
import pt.uevora.sd.server.model.Device;
import pt.uevora.sd.server.service.DeviceService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gestão de dispositivos IoT
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * POST /api/devices - Criar novo dispositivo
     */
    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceDTO deviceDTO) {
        log.info("POST /api/devices - Criar dispositivo: {}", deviceDTO.getId());

        try {
            Device device = deviceService.createDevice(deviceDTO.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DeviceDTO.fromEntity(device));
        } catch (IllegalArgumentException e) {
            log.error("Erro ao criar dispositivo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/devices - Listar todos os dispositivos
     */
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        log.info("GET /api/devices - Listar todos os dispositivos");

        List<DeviceDTO> devices = deviceService.getAllDevices()
                .stream()
                .map(DeviceDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(devices);
    }

    /**
     * GET /api/devices/{id} - Obter detalhes de um dispositivo
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable String id) {
        log.info("GET /api/devices/{} - Obter dispositivo", id);

        return deviceService.getDeviceById(id)
                .map(device -> ResponseEntity.ok(DeviceDTO.fromEntity(device)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/devices/{id} - Atualizar dispositivo
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable String id,
            @Valid @RequestBody DeviceDTO deviceDTO) {

        log.info("PUT /api/devices/{} - Atualizar dispositivo", id);

        try {
            Device updatedDevice = deviceService.updateDevice(id, deviceDTO.toEntity());
            return ResponseEntity.ok(DeviceDTO.fromEntity(updatedDevice));
        } catch (IllegalArgumentException e) {
            log.error("Erro ao atualizar dispositivo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/devices/{id} - Eliminar dispositivo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        log.info("DELETE /api/devices/{} - Eliminar dispositivo", id);

        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Erro ao eliminar dispositivo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}