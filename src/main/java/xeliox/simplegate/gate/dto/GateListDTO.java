package xeliox.simplegate.gate.dto;

import java.util.List;

public class GateListDTO {
    private List<GateDTO> gates;

    public GateListDTO() {
    }

    public GateListDTO(List<GateDTO> gates) {
        this.gates = gates;
    }

    public List<GateDTO> getGates() {
        return gates;
    }
}
