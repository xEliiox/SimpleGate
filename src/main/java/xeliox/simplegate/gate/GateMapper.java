package xeliox.simplegate.gate;

import xeliox.simplegate.gate.dto.BlockLocationDTO;
import xeliox.simplegate.gate.dto.DestinationDTO;
import xeliox.simplegate.gate.dto.GateDTO;
import xeliox.simplegate.teleport.BlockLocation;
import xeliox.simplegate.teleport.Destination;
import xeliox.simplegate.teleport.Heading;

import java.util.UUID;
import java.util.stream.Collectors;

public class GateMapper {

    private GateMapper() {}

    /* BlockLocation */
    public static BlockLocationDTO toDTO(BlockLocation loc) {
        return new BlockLocationDTO(
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    public static BlockLocation toBlockLocation(BlockLocationDTO dto) {
        return new BlockLocation(dto.getBlockX(), dto.getBlockY(), dto.getBlockZ());
    }

    /* Destination */
    public static DestinationDTO toDTO(Destination dest) {
        return new DestinationDTO(
                dest.getWorld(),
                dest.getLocation().getBlockX(),
                dest.getLocation().getBlockY(),
                dest.getLocation().getBlockZ(),
                dest.getHeading().getPitch(),
                dest.getHeading().getYaw()
        );
    }

    public static Destination toDestination(DestinationDTO dto) {
        return new Destination(
                dto.getWorldName(),
                new BlockLocation(dto.getX(), dto.getY(), dto.getZ()),
                new Heading(dto.getPitch(), dto.getYaw())
        );
    }

    /* Gate */
    public static GateDTO toDTO(Gate gate) {
        return new GateDTO(
                gate.id,
                gate.networkId,
                toDTO(gate.exit),
                gate.getPortalWorldName(),
                gate.portalTypeId,
                gate.creatorId.toString(),
                gate.creationTimeMillis,
                gate.restricted,
                gate.enterEnabled,
                gate.exitEnabled,
                gate.isFakeEndGateway(),
                gate.orientation,
                gate.getFrameCoords().stream().map(GateMapper::toDTO).collect(Collectors.toList()),
                gate.getPortalCoords().stream().map(GateMapper::toDTO).collect(Collectors.toList())
        );
    }

    public static Gate toGate(GateDTO dto) {
        Gate gate = new Gate(
                dto.getNetworkId(),
                toDestination(dto.getExit()),
                dto.getFrameCoords().stream()
                        .map(GateMapper::toBlockLocation)
                        .collect(Collectors.toSet()),
                dto.getOrientation(),
                dto.getPortalCoords().stream()
                        .map(GateMapper::toBlockLocation)
                        .collect(Collectors.toSet()),
                UUID.fromString(dto.getCreatorId())
        );

        gate.portalWorldName = dto.getPortalWorldName();
        gate.portalTypeId = dto.getPortalTypeId();
        gate.orientation = dto.getOrientation();
        gate.id = dto.getId();
        gate.creationTimeMillis = dto.getCreationTimeMillis();
        gate.restricted = dto.isRestricted();
        gate.enterEnabled = dto.isEnterEnabled();
        gate.exitEnabled = dto.isExitEnabled();
        gate.setFakeEndGateway(dto.isFakeEndGateway());

        return gate;
    }
}
