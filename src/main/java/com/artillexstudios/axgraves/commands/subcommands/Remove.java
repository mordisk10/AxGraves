package com.artillexstudios.axgraves.commands.subcommands;

import com.artillexstudios.axgraves.AxGraves;
import com.artillexstudios.axgraves.grave.Grave;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

import static com.artillexstudios.axgraves.AxGraves.MESSAGEUTILS;

// Not intended to use this command, maybe rare cases for admins
public enum Remove {
    INSTANCE;

    public void execute(Player sender, World world, Double x, Double y, Double z) {
        if (world == null || x == null || y == null || z == null) {
            MESSAGEUTILS.sendLang(sender, "grave-remove.invalid-arguments");
            return;
        }

        Optional<Grave> grave = AxGraves.getAPI().findGrave(sender, world.getName(), x, y, z);

        if (grave.isEmpty()) {
            MESSAGEUTILS.sendLang(sender, "grave-remove.not-found");
            return;
        }
        
        grave.get().remove();
        MESSAGEUTILS.sendLang(sender, "grave-remove.success");
    }
}
