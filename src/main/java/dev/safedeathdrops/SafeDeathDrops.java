package dev.safedeathdrops;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SafeDeathDrops extends JavaPlugin implements Listener {

    private int invulnerableDurationTicks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        invulnerableDurationTicks = config.getInt("invulnerableDurationTicks", 60);

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("SafeDeathDrops enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("SafeDeathDrops disabled");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();

        if (damageEvent instanceof EntityDamageByEntityEvent entityDamage) {
            Entity damager = entityDamage.getDamager();
            
            //creeper explosion dont do this? maybe cause they kill you with an explosion instead of the proectile killing you then exploding.
            //also might be a second wither projectile
            if (damager instanceof Fireball || damager instanceof WitherSkull) {
                List<ItemStack> drops = new ArrayList<>(event.getDrops());
                event.getDrops().clear();

                Location location = event.getEntity().getLocation();
                for (ItemStack item : drops) {
                    Item dropped = location.getWorld().dropItemNaturally(location, item);
                    dropped.setInvulnerable(true);
                    dropped.setPickupDelay(invulnerableDurationTicks);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!dropped.isDead()) {
                                dropped.setInvulnerable(false);
                            }
                        }
                    //use config here for adjustment
                    }.runTaskLater(this, invulnerableDurationTicks);
                }
            }
        }
    }
}
