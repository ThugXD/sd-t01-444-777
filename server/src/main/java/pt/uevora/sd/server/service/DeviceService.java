// Caminho: server/src/main/java/pt/uevora/sd/server/service/DeviceService.java

package pt.uevora.sd.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uevora.sd.server.model.Device;
import pt.uevora.sd.server.repository.DeviceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serviço para gestão de dispositivos IoT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /**
     * Cria um novo dispositivo
     */
    @Transactional
    public Device createDevice(Device device) {
        log.info("Criando dispositivo: {}", device.getId());

        if (deviceRepository.existsById(device.getId())) {
            throw new IllegalArgumentException("Dispositivo com ID " + device.getId() + " já existe");
        }

        return deviceRepository.save(device);
    }

    /**
     * Lista todos os dispositivos
     */
    public List<Device> getAllDevices() {
        log.debug("Listando todos os dispositivos");
        return deviceRepository.findAll();
    }

    /**
     * Busca dispositivo por ID
     */
    public Optional<Device> getDeviceById(String id) {
        log.debug("Buscando dispositivo: {}", id);
        return deviceRepository.findById(id);
    }

    /**
     * Atualiza um dispositivo existente
     */
    @Transactional
    public Device updateDevice(String id, Device updatedDevice) {
        log.info("Atualizando dispositivo: {}", id);

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado: " + id));

        // Atualizar campos
        existingDevice.setProtocol(updatedDevice.getProtocol());
        existingDevice.setRoom(updatedDevice.getRoom());
        existingDevice.setDepartment(updatedDevice.getDepartment());
        existingDevice.setFloor(updatedDevice.getFloor());
        existingDevice.setBuilding(updatedDevice.getBuilding());
        existingDevice.setStatus(updatedDevice.getStatus());

        return deviceRepository.save(existingDevice);
    }

    /**
     * Elimina um dispositivo
     */
    @Transactional
    public void deleteDevice(String id) {
        log.info("Eliminando dispositivo: {}", id);

        if (!deviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Dispositivo não encontrado: " + id);
        }

        deviceRepository.deleteById(id);
    }

    /**
     * Verifica se dispositivo está ativo
     */
    public boolean isDeviceActive(String deviceId) {
        return deviceRepository.existsByIdAndStatus(deviceId, Device.DeviceStatus.ACTIVE);
    }

    /**
     * Busca dispositivos por edifício
     */
    public List<Device> getDevicesByBuilding(String building) {
        return deviceRepository.findByBuilding(building);
    }

    /**
     * Busca dispositivos por departamento
     */
    public List<Device> getDevicesByDepartment(String department) {
        return deviceRepository.findByDepartment(department);
    }

    /**
     * Busca dispositivos por piso
     */
    public List<Device> getDevicesByFloor(String floor) {
        return deviceRepository.findByFloor(floor);
    }

    /**
     * Busca dispositivos por sala
     */
    public List<Device> getDevicesByRoom(String room) {
        return deviceRepository.findByRoom(room);
    }
}