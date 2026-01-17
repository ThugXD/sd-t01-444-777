package pt.uevora.sd.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.uevora.sd.server.model.Device;
import pt.uevora.sd.server.model.Device.DeviceStatus;
import pt.uevora.sd.server.model.Device.ProtocolType;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para acesso aos dados de dispositivos
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    /**
     * Encontra dispositivos por protocolo
     */
    List<Device> findByProtocol(ProtocolType protocol);

    /**
     * Encontra dispositivos por estado
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * Encontra dispositivos por edifício
     */
    List<Device> findByBuilding(String building);

    /**
     * Encontra dispositivos por departamento
     */
    List<Device> findByDepartment(String department);

    /**
     * Encontra dispositivos por piso
     */
    List<Device> findByFloor(String floor);

    /**
     * Encontra dispositivos por sala
     */
    List<Device> findByRoom(String room);

    /**
     * Verifica se um dispositivo existe e está ativo
     */
    boolean existsByIdAndStatus(String id, DeviceStatus status);

    /**
     * Encontra dispositivo ativo por ID
     */
    Optional<Device> findByIdAndStatus(String id, DeviceStatus status);
}