package com.artillexstudios.axgraves;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axgraves.api.AxGravesAPI;
import com.artillexstudios.axgraves.commands.CommandManager;
import com.artillexstudios.axgraves.grave.Grave;
import com.artillexstudios.axgraves.grave.GravePlaceholders;
import com.artillexstudios.axgraves.grave.SpawnedGraves;
import com.artillexstudios.axgraves.listeners.DeathListener;
import com.artillexstudios.axgraves.listeners.PlayerInteractListener;
import com.artillexstudios.axgraves.listeners.PlayerMoveListener;
import com.artillexstudios.axgraves.schedulers.SaveGraves;
import com.artillexstudios.axgraves.schedulers.TickGraves;
import com.artillexstudios.axgraves.utils.LocationUtils;
import com.artillexstudios.axgraves.utils.UpdateNotifier;
import org.bstats.bukkit.Metrics;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AxGraves extends AxPlugin implements AxGravesAPI {
    private static AxPlugin instance;
    public static Config CONFIG;
    public static Config LANG;
    public static MessageUtils MESSAGEUTILS;
    public static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static AxMetrics metrics;

    public static AxPlugin getInstance() {
        return instance;
    }

    public static AxGravesAPI getAPI() {
        return (AxGravesAPI) instance;
    }

    public void enable() {
        instance = this;

        new Metrics(this, 20332);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "messages.yml"), getResource("messages.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

        CommandManager.load();

        GravePlaceholders.register();

        if (CONFIG.getBoolean("save-graves.enabled", true)) {
            SpawnedGraves.loadFromFile();
        }

        TickGraves.start();
        SaveGraves.start();

        metrics = new AxMetrics(this, 20);
        metrics.start();

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5076);

        getServer().getServicesManager().register(AxGravesAPI.class, this, this, ServicePriority.Normal);
    }

    public void disable() {
        getServer().getServicesManager().unregister(AxGravesAPI.class, this);

        if (metrics != null) metrics.cancel();

        TickGraves.stop();
        SaveGraves.stop();

        for (Grave grave : SpawnedGraves.getGraves()) {
            if (!CONFIG.getBoolean("save-graves.enabled", true)) grave.remove();
            if (grave.getEntity() != null) grave.getEntity().remove();
            if (grave.getHologram() != null) grave.getHologram().remove();
        }

        if (CONFIG.getBoolean("save-graves.enabled", true)) {
            SpawnedGraves.saveToFile();
        }

        LocationUtils.clearAllLastSolidLocations();

        EXECUTOR.shutdownNow();
    }

    public void updateFlags() {
        FeatureFlags.USE_LEGACY_HEX_FORMATTER.set(true);
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        FeatureFlags.HOLOGRAM_UPDATE_TICKS.set(5L);
        FeatureFlags.ENABLE_PACKET_LISTENERS.set(true);
    }

    @Override
    public Optional<Grave> findGrave(OfflinePlayer player, String world, double x, double y, double z) {
        return SpawnedGraves.getGraves().stream()
                .filter(grave -> {
                    var location = grave.getLocation();
                    if (location.getWorld() == null) return false;

                    return grave.getPlayer().getUniqueId().equals(player.getUniqueId()) && location.getX() == x && location.getY() == y && location.getZ() == z && location.getWorld().getName().equals(world);
                })
                .findFirst();
    }

}
