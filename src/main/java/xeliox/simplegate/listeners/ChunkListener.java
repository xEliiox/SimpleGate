package xeliox.simplegate.listeners;

import org.bukkit.Chunk;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import xeliox.simplegate.gate.Gate;
import xeliox.simplegate.gate.GateManager;

import java.util.Set;

public class ChunkListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        Set<Gate> gates = GateManager.getGatesInChunk(chunk);
        for (Gate gate : gates) {
            if (gate.isIntact()) {
                gate.startPortalParticles();
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Set<Gate> gates = GateManager.getGatesInChunk(event.getChunk());

        for (Gate gate : gates) {
            gate.stopPortalParticles();
        }
    }
}