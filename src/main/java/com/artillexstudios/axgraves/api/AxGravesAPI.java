package com.artillexstudios.axgraves.api;

import com.artillexstudios.axgraves.grave.Grave;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

/**
 * AxGraves API interface for interacting with graves
 */
public interface AxGravesAPI {

    /**
     * Find a grave by world and coordinates
     * To remove a grave you can simply call grave.remove();
     * @param player The owner of the grave
     * @param world the world name
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return An Optional containing the Grave if found, otherwise an empty Optional
     */
    Optional<Grave> findGrave(OfflinePlayer player, String world, double x, double y, double z);

}
