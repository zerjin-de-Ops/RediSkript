package net.limework.rediskript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Date;
import net.limework.rediskript.commands.CommandReloadRedis;
import net.limework.rediskript.events.RedisMessageEvent;
import net.limework.rediskript.managers.RedisController;
import net.limework.rediskript.skript.elements.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class RediSkript extends JavaPlugin {

    private RedisController redisController;

    public void reloadRedis() {
        reloadConfig();
        redisController.shutdown();
        redisController = new RedisController(this);
    }

    public void sendLogs(String message) {
        getLogger().info(
                ChatColor.translateAlternateColorCodes('&', "&b" + message)
        );
    }
    public void sendErrorLogs(String message) {
        getLogger().severe(
                ChatColor.translateAlternateColorCodes('&', "&c" + message)
        );
    }

    public void registerSyntax() {
        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("net.limework.rediskript.skript", "elements");
            Skript.registerEvent("redis message", EvtRedis.class, RedisMessageEvent.class, "redis message");
            Skript.registerExpression(ExprVariableInChannel.class, Object.class, ExpressionType.PROPERTY, "variable[s] %strings% in [redis] [channel] %string%");

            Skript.registerExpression(ExprChannel.class, String.class, ExpressionType.SIMPLE, "redis channel");
            EventValues.registerEventValue(RedisMessageEvent.class, String.class, RedisMessageEvent::getChannelName, 0);
            Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, "redis message");
            EventValues.registerEventValue(RedisMessageEvent.class, String.class, RedisMessageEvent::getMessage, 0);
            Skript.registerExpression(ExprMessageDate.class, Date.class, ExpressionType.SIMPLE, "redis message date");
            EventValues.registerEventValue(RedisMessageEvent.class, Date.class, (e) -> new Date(e.getDate()), 0);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error registering Skript", e);
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        redisController = new RedisController(this);
        getServer().getPluginCommand("reloadredis").setExecutor(new CommandReloadRedis(this));
        registerSyntax();
    }

    @Override
    public void onDisable() {
        if (redisController != null) {
            redisController.shutdown();
        }
        getServer().getPluginCommand("reloadredis").setExecutor(null);

    }

    public RedisController getRC() {
        return redisController;
    }

    //Developer note: This is use for skript-reflect! and also for plugin use for static stuff -> skript effects classes
    //DO NOT USE THIS IN NON STATIC CLASSES
    public static RediSkript getAPI(){
        //this safer than making static.
        return (RediSkript) Bukkit.getServer().getPluginManager().getPlugin("RediSkript");
    }

}