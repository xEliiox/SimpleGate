package xeliox.simplegate.gate.dto;

import org.bukkit.block.BlockFace;
import xeliox.simplegate.gate.GateOrientation;

import java.util.List;

public class GateDTO {

    private int id;
    private String networkId;
    private DestinationDTO exit;
    private String portalWorldName;
    private Integer portalTypeId;
    private String creatorId;
    private long creationTimeMillis;
    private boolean restricted;
    private boolean enterEnabled;
    private boolean exitEnabled;
    private boolean fakeEndGateway;
    private GateOrientation orientation;
    private List<BlockLocationDTO> frameCoords;
    private List<BlockLocationDTO> portalCoords;
    private BlockFace facing;

    public GateDTO() {
    }

    public GateDTO(int id,
                   String networkId,
                   DestinationDTO exit,
                   String portalWorldName,
                   Integer portalTypeId,
                   String creatorId,
                   long creationTimeMillis,
                   boolean restricted,
                   boolean enterEnabled,
                   boolean exitEnabled,
                   boolean fakeEndGateway,
                   GateOrientation orientation,
                   List<BlockLocationDTO> frameCoords,
                   List<BlockLocationDTO> portalCoords) {

        this.id = id;
        this.networkId = networkId;
        this.exit = exit;
        this.portalWorldName = portalWorldName;
        this.portalTypeId = portalTypeId;
        this.creatorId = creatorId;
        this.creationTimeMillis = creationTimeMillis;
        this.restricted = restricted;
        this.enterEnabled = enterEnabled;
        this.exitEnabled = exitEnabled;
        this.fakeEndGateway = fakeEndGateway;
        this.orientation = orientation;
        this.frameCoords = frameCoords;
        this.portalCoords = portalCoords;
    }

    public int getId() { return id; }
    public String getNetworkId() { return networkId; }
    public DestinationDTO getExit() { return exit; }
    public String getPortalWorldName() { return portalWorldName; }
    public Integer getPortalTypeId() { return portalTypeId; }
    public String getCreatorId() { return creatorId; }
    public long getCreationTimeMillis() { return creationTimeMillis; }
    public boolean isRestricted() { return restricted; }
    public boolean isEnterEnabled() { return enterEnabled; }
    public boolean isExitEnabled() { return exitEnabled; }
    public boolean isFakeEndGateway() { return fakeEndGateway; }
    public GateOrientation getOrientation() {return orientation; }
    public List<BlockLocationDTO> getFrameCoords() { return frameCoords; }
    public List<BlockLocationDTO> getPortalCoords() { return portalCoords; }
}
