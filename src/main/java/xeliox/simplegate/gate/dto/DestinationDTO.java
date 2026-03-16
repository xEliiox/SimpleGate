package xeliox.simplegate.gate.dto;

public class DestinationDTO {
    private String worldName;
    private int x, y, z;
    private float pitch, yaw;

    public DestinationDTO() {
    }

    public DestinationDTO(String worldName, int x, int y, int z, float pitch, float yaw) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
}
