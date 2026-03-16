package xeliox.simplegate.teleport;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Heading {

    private float pitch = 0.0f;
    private float yaw = 0.0f;

    public Heading() {
    }

    public Heading(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(pitch)
                .append(yaw)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Heading other = (Heading) obj;
        return new EqualsBuilder()
                .append(pitch, other.pitch)
                .append(yaw, other.yaw)
                .isEquals();
    }

    @Override
    public String toString() {
        return "(pitch=" + pitch + ", yaw=" + yaw + ")";
    }
}