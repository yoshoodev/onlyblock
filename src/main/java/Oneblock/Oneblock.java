// Copyright © 2022 MrMarL. All rights reserved.
package Oneblock;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Oneblock extends JavaPlugin {
    boolean on = false;

    // RANDOM
    Random rnd = new Random(System.currentTimeMillis());
    int random = 0;

    // WORLD DATA
    World world;
    int x = 0;
    int y = 0;
    int z = 0;

    // LEAVE WORLD DATA
    World leaveworld;

    // PLAYER DATA DECLARATIONS
    int id = 0;
    static ArrayList<PlayerInfo> pInf = new ArrayList<>();
    static List<Player> online;

    // BUKKIT CONFIG DECLARATIONS
    FileConfiguration config;
    FileConfiguration newConfig;

    // CONFIG VALUES
    boolean il3x3 = false;
    boolean rebirth = false;
    boolean autojoin = false;
    boolean levelBarMode = false;
    boolean chatAlert = false;
    boolean protection = false;
    boolean papi = false;
    boolean worldGuard = false;
    boolean progressBar = true;

    // VERSION
    boolean superlegacy;
    boolean legacy;
    String version = "";

    // ETC
    ArrayList<Material> smallChest;
    ArrayList<Material> mediumChest;
    ArrayList<Material> hardChest;
    ArrayList<Object> blocks = new ArrayList<>();
    ArrayList<EntityType> mobs = new ArrayList<>();
    ArrayList<XMaterial> flowers = new ArrayList<>();
    static ArrayList<Level> levels = new ArrayList<>();
    static Level maxlevel = new Level("Level: MAX");
    static int levelMultiplier = 5;
    String textP = "";
    int space = 100;
    Long frequency;
    BarColor progressBarColor;
    BlockData[][][] customisland = null;
    ArrayList<Invitation> invite = new ArrayList<>();

    // WORLDGUARD
    OBWorldGuard obWorldGuard;
    boolean obCanUse = false;

    // MATERIALS
    XMaterial grassBlock = XMaterial.GRASS_BLOCK;
    XMaterial grass = XMaterial.GRASS;

    // CONFIG FILE ASSIGNMENTS
    static String wgph = "worldGuard";
    static String yl = "yleave";
    static String worldph = "world";
    static String lworldph = "leaveworld";
    static String progressbarph = "progressBar";
    static String progressbartext = "progressBar_text";

    // FILE NAME ASSIGNMENTS
    static String blocksfile = "blocks.yml";
    static String flowerfile = "flowers.yml";
    static String mobfile = "mobs.yml";
    static String configfile = "config.yml";
    static String levelsfile = "levels.yml";
    static String chestfile = "chests.yml";
    static String playerDF = "playerData.json";

    // PERMISSIONS VARIABLES
    static String permissionSet = "Oneblock.set";
    String noperm = String.format("%sYou don't have permission [Oneblock.set].", ChatColor.RED);

    // --|PLUGIN STARTUP|--
    @Override
    public void onEnable() {
        version = "1." + XMaterial.getVersion(); // Get version number
        superlegacy = !XMaterial.supports(9);// Is version 1.9 supported?
        legacy = !XMaterial.supports(13);// Is version 1.13 supported?
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papi = true;
            new OBP().register();
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] PlaceholderAPI has been found!");
        }
        if (Bukkit.getPluginManager().isPluginEnabled(wgph)) {
            worldGuard = true;
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] WorldGuard has been found!");
        }
        // Begin loading config files
        configFile();
        dataFile();
        blockFile();
        flowerFile();
        chestFile();
        mobFile();
        // Check if /ob set was called by checking y value in config.yml
        if (config.getDouble("y") != 0) { // IF y!=0 then enable plugin
            // IF world is null or exit world is null call world_null_gen
            if (world == null || (config.getDouble(yl) != 0 && leaveworld == null)) {
                Bukkit.getScheduler().runTaskTimer(this, new NullWorld(), 32, 64);
            } else {
                Bukkit.getScheduler().runTaskTimer(this, new Task(), frequency, frequency * 2);
                on = true;
            }
        }
        Bukkit.getPluginManager().registerEvents(new RespawnAutoJoin(), this);
    }

    public class RespawnAutoJoin implements Listener {
        @EventHandler
        public void respawn(PlayerRespawnEvent e) {
            if (rebirth && e.getPlayer().getWorld().equals(world) && existID(e.getPlayer().getName()))
                e.setRespawnLocation(
                        new Location(world, x + getID(e.getPlayer().getName()) * space + 0.5, y + 1.2,
                                z + 0.5));
        }

        @EventHandler
        public void teleport(PlayerTeleportEvent e) {
            if (autojoin) {
                Location loc = e.getTo();
                World from = e.getFrom().getWorld();
                World to = loc.getWorld();
                if (!from.equals(world) && to.equals(world) &&
                        !(loc.getY() == y + 1.2 && loc.getZ() == z + 0.5)) {
                    e.setCancelled(true);
                    e.getPlayer().performCommand("ob j");
                }
            }
        }

        @EventHandler
        public void join(PlayerJoinEvent e) {
            if (autojoin) {
                Player pl = e.getPlayer();
                if (pl.getWorld().equals(world))
                    pl.performCommand("ob j");
            }
        }
    }

    public class ChangedWorld implements Listener {
        @EventHandler
        public void changedWorld(PlayerChangedWorldEvent e) {
            Player p = e.getPlayer();
            World from = e.getFrom();
            if (from.equals(world) && getID(p.getName()) < pInf.size())
                pInf.get(getID(p.getName())).bar.removePlayer(p);
        }
    }

    public class NullWorld implements Runnable {
        public void run() {
            if (world == null) {
                String msg = String.format("%n%s%n%s",
                        "[OB] WORLD INITIALIZATION ERROR! world = null",
                        "[OB] Trying to initialize the world again...");
                Bukkit.getLogger().info(msg);
                world = Bukkit.getWorld(config.getString(worldph));
                leaveworld = Bukkit.getWorld(config.getString(lworldph));
            } else {
                Bukkit.getLogger().info("[OB] The initialization of the world was successful!");
                worldInit();
            }
        }
    }

    public void worldInit() {
        Bukkit.getScheduler().cancelTasks(this);
        if (config.getDouble("y") != 0) {
            Bukkit.getScheduler().runTaskTimer(this, new Task(), frequency, frequency * 2);
            on = true;
        }
    }

    public void addinvite(String name, String to) {
        for (Invitation item : invite)
            if (item.equals(name, to))
                return;
        Invitation tempinv = new Invitation(name, to);
        invite.add(tempinv);
        class InviteDelete implements Runnable {
            @Override
            public void run() {
                invite.remove(tempinv);
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, new InviteDelete(), 300L);
    }

    public boolean checkinvite(Player pl) {
        String name = pl.getName();
        Invitation tempinv = null;
        for (Invitation item : invite)
            if (item.Invited.equals(name))
                tempinv = item;

        if (tempinv == null || !existID(tempinv.Inviting))
            return false;

        if (existID(name)) {
            if (progressBar)
                pInf.get(getID(name)).bar.removePlayer(pl);
            pl.performCommand("ob idreset /n");
        }
        pInf.get(getID(tempinv.Inviting)).nicks.add(name);
        pl.performCommand("ob j");
        invite.remove(tempinv);
        return true;
    }

    public class Task implements Runnable {
        public void protect(Player ponl, int obpX) {
            if (protection && !ponl.hasPermission("Oneblock.ignoreBarrier")) {
                int check = ponl.getLocation().getBlockX() - obpX - x;
                if (check > 50 || check < -50) {
                    if (check > 200 || check < -200) {
                        ponl.performCommand("ob j");
                        return;
                    }
                    ponl.setVelocity(new Vector(-check / 30, 0, 0));
                    ponl.sendMessage(String.format("%s%s%s%s", ChatColor.YELLOW, "are you trying to go ",
                            ChatColor.RED, "outside the customisland?"));
                }
            }
        }

        public void run() {
            online = world.getPlayers();
            Collections.shuffle(online);
            for (Player ponl : online) {
                String name = ponl.getName();
                if (!existID(name))
                    continue;
                int obid = getID(name);
                int obpX = obid * space;

                // check for player exiting his customisland
                protect(ponl, obpX);

                Block block = world.getBlockAt(x + obpX, y, z);
                if (block.isEmpty()) {
                    PlayerInfo curPlayer = pInf.get(obid);
                    Level curMaxLevel = maxlevel;
                    if (curPlayer.lvl < levels.size())
                        curMaxLevel = levels.get(curPlayer.lvl);
                    curPlayer.breaks++;
                    if (curPlayer.breaks >= 16 + curPlayer.lvl * levelMultiplier) {
                        curPlayer.lvlup();
                        curMaxLevel = maxlevel;
                        if (curPlayer.lvl < levels.size())
                            curMaxLevel = levels.get(curPlayer.lvl);
                        if (progressBar) {
                            curPlayer.bar.setColor(curMaxLevel.color);
                            if (levelBarMode)
                                curPlayer.bar.setTitle(curMaxLevel.name);
                        }
                        if (chatAlert)
                            ponl.sendMessage(String.format("%s%s", ChatColor.GREEN, curMaxLevel.name));
                    }
                    if (progressBar) {
                        if (!levelBarMode && papi)
                            curPlayer.bar.setTitle(PlaceholderAPI.setPlaceholders(ponl, textP));
                        curPlayer.bar.setProgress((double) curPlayer.breaks / (16 + curPlayer.lvl * levelMultiplier));
                        curPlayer.bar.addPlayer(ponl);
                    }
                    Location loc = ponl.getLocation();
                    if (loc.getBlockX() == x + obpX && loc.getY() - 1 < y && loc.getBlockZ() == z) {
                        loc.setY((double) y + 1);
                        ponl.teleport(loc);
                    } else
                        for (Player pll : playerList()) {
                            loc = pll.getLocation();
                            if (loc.getBlockX() == x + obpX && loc.getY() - 1 < y && loc.getBlockZ() == z) {
                                loc.setY((double) y + 1);
                                pll.teleport(loc);
                                break;
                            }
                        }
                    random = curMaxLevel.size;
                    if (random != 0)
                        random = rnd.nextInt(random);
                    if (blocks.get(random) == null) {
                        XBlock.setType(block, grassBlock);
                        if (rnd.nextInt(3) == 1)
                            XBlock.setType(world.getBlockAt(x + obpX, y + 1, z),
                                    flowers.get(rnd.nextInt(flowers.size())));
                    } else if (blocks.get(random) == XMaterial.CHEST) {
                        try {
                            block.setType(Material.CHEST);
                            Chest chest = (Chest) block.getState();
                            Inventory inv = chest.getInventory();
                            ArrayList<Material> chestNow;
                            if (random < 26)
                                chestNow = smallChest;
                            else if (random < 68)
                                chestNow = mediumChest;
                            else
                                chestNow = hardChest;
                            int max = rnd.nextInt(3) + 2;
                            for (int i = 0; i < max; i++) {
                                Material m = chestNow.get(rnd.nextInt(chestNow.size()));
                                if (m.getMaxStackSize() == 1)
                                    inv.addItem(new ItemStack(m, 1));
                                else
                                    inv.addItem(new ItemStack(m, rnd.nextInt(4) + 2));
                            }
                        } catch (Exception e) {
                            Bukkit.getConsoleSender().sendMessage(
                                    "[OB] Error when generating items for the chest! Pls redo chests.yml!");
                        }
                    } else
                        XBlock.setType(block, blocks.get(random));

                    if (rnd.nextInt(9) == 0) {
                        if (curPlayer.lvl < blocks.size() / 9)
                            random = rnd.nextInt(mobs.size() / 3);
                        else if (curPlayer.lvl < blocks.size() / 9 * 2)
                            random = rnd.nextInt(mobs.size() / 3 * 2);
                        else
                            random = rnd.nextInt(mobs.size());
                        world.spawnEntity(new Location(world, (double) x + obpX, (double) y + 1, z), mobs.get(random));
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (customisland != null) {
            HashMap<String, List<String>> map = new HashMap<>();
            List<String> yNow = new ArrayList<>();
            for (int yy = 0; yy < 3; yy++) {
                yNow.clear();
                for (int xx = 0; xx < 7; xx++)
                    for (int zz = 0; zz < 7; zz++)
                        yNow.add(customisland[xx][yy][zz].getAsString());
                map.put(String.format("y%d", yy), yNow);
            }
            config.set("custom_island", map);
        }
        saveData();
        Config.Save(config);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("oneblock")) {
            //
            if (args.length == 0)
                return ((Player) sender).performCommand("ob j");

            if (!sender.hasPermission("Oneblock.join")) {
                sender.sendMessage(String.format("%sYou don't have permission [Oneblock.join].", ChatColor.RED));
                return true;
            }
            //
            switch (args[0].toLowerCase()) {
                case ("j"):
                case ("join"): {
                    if (config.getInt("y") == 0 || world == null) {
                        sender.sendMessage(String.format("%sFirst you need to set the reference coordinates '/ob set'.",
                                ChatColor.YELLOW));
                        return true;
                    }
                    Player p = (Player) sender;
                    String name = p.getName();
                    if (!existID(name)) {
                        if (il3x3) {
                            if (customisland != null) {
                                int px = x + id * space - 3;
                                for (int xx = 0; xx < 7; xx++)
                                    for (int yy = 0; yy < 3; yy++)
                                        for (int zz = 0; zz < 7; zz++)
                                            world.getBlockAt(px + xx, y + yy, z - 3 + zz)
                                                    .setBlockData(customisland[xx][yy][zz]);
                            } else {
                                for (int i = -2; i <= 2; i++)
                                    for (int q = -2; q <= 2; q++)
                                        if (Math.abs(i) + Math.abs(q) < 3)
                                            XBlock.setType(world.getBlockAt(x + id * space + i, y, z + q), grassBlock);
                            }
                        }
                        // worldGuard
                        if (worldGuard && obCanUse) {
                            int xWG = x + id * space;
                            Vector block1 = new Vector(xWG - space / 2 + 1, 0, z - 100);
                            Vector block2 = new Vector(xWG + space / 2 - 1, 255, z + 100);
                            obWorldGuard.CreateRegion(name, block1, block2, id);
                        }
                        id++;
                        saveData();
                        PlayerInfo curPlayer = new PlayerInfo();
                        pInf.add(curPlayer);
                        curPlayer.nick = name;
                        if (!superlegacy && progressBar) {
                            String temp = textP;
                            if (levelBarMode)
                                temp = levels.get(0).name;
                            else if (papi)
                                temp = PlaceholderAPI.setPlaceholders(p, textP);
                            curPlayer.bar = (Bukkit.createBossBar(temp, levels.get(0).color, BarStyle.SEGMENTED_10,
                                    BarFlag.DARKEN_SKY));
                        }
                    }
                    if (!on) {
                        Bukkit.getScheduler().runTaskTimer(this, new Task(), frequency, frequency * 2);
                        on = true;
                    }
                    if (progressBar)
                        pInf.get(getID(name)).bar.setVisible(true);
                    p.teleport(new Location(world, x + getID(name) * space + 0.5, y + 1.2, z + 0.5));
                    if (worldGuard && obCanUse) {
                        obWorldGuard.addMember(name, getID(name));
                    }
                    return true;
                }
                case ("leave"): {
                    Player p = (Player) sender;
                    if (!superlegacy)
                        pInf.get(getID(p.getName())).bar.removePlayer(p);
                    if (config.getDouble(yl) == 0 || leaveworld == null)
                        return true;
                    p.teleport(new Location(leaveworld, config.getDouble("xleave"), config.getDouble(yl),
                            config.getDouble("zleave")));
                    return true;
                }
                case ("set"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    Player p = (Player) sender;
                    Location l = p.getLocation();
                    x = l.getBlockX();
                    y = l.getBlockY();
                    z = l.getBlockZ();
                    world = l.getWorld();
                    int temp = 100;
                    if (args.length >= 2) {
                        try {
                            temp = Integer.parseInt(args[1]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(String.format("%sinvalid value", ChatColor.RED));
                            return true;
                        }
                        if (temp > 1000 || temp < -1000) {
                            sender.sendMessage(
                                    String.format("%spossible values are from -1000 to 1000", ChatColor.RED));
                            return true;
                        }
                        space = temp;
                        config.set("set", space);
                    }
                    config.set(worldph, world.getName());
                    config.set("x", (double) x);
                    config.set("y", (double) y);
                    config.set("z", (double) z);
                    Config.Save(config);
                    world.getBlockAt(x, y, z).setType(grassBlock.parseMaterial());
                    recreateworldGuard();
                    return true;
                }
                case ("setleave"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    Player p = (Player) sender;
                    Location l = p.getLocation();
                    leaveworld = l.getWorld();
                    config.set(lworldph, leaveworld.getName());
                    config.set("xleave", l.getX());
                    config.set(yl, l.getY());
                    config.set("zleave", l.getZ());
                    Config.Save(config);
                    return true;
                }
                case ("invite"): {
                    if (!sender.hasPermission("Oneblock.invite")) {
                        sender.sendMessage(String.format("%sYou don't have the permission to execute this command",
                                ChatColor.RED));
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage(String.format("%sUsage: /ob invite <username>", ChatColor.RED));
                        return true;
                    }
                    Player inv = Bukkit.getPlayer(args[1]);
                    if (inv != null) {
                        if (inv == (Player) sender) {
                            sender.sendMessage(String.format("%sYou can't invite yourself.", ChatColor.YELLOW));
                            return true;
                        }
                        if (!existID(((Player) sender).getName())) {
                            sender.sendMessage(
                                    String.format("%sPlease create a customisland before you do this.",
                                            ChatColor.YELLOW));
                            return true;
                        }
                        addinvite(((Player) sender).getName(), inv.getName());
                        inv.sendMessage(String.format("%sYou were invited by player %s.%n%s/ob accept to accept).",
                                ChatColor.GREEN, ((Player) sender).getName(), ChatColor.RED));
                        sender.sendMessage(String.format("%sSuccesfully invited %s.", ChatColor.GREEN, inv.getName()));
                    }
                    return true;
                }
                case ("kick"): {
                    if (args.length < 2) {
                        sender.sendMessage(String.format("%sUsage: /ob invite <username>", ChatColor.RED));
                        return true;
                    }
                    Player inv = Bukkit.getPlayer(args[1]);
                    String name = ((Player) sender).getName();
                    if (!checkInvalidID(name))
                        return true;
                    if (inv != null) {
                        if (inv == (Player) sender) {
                            sender.sendMessage(String.format("%sYou can't kick yourself.", ChatColor.YELLOW));
                            return true;
                        }
                        if (pInf.get(getID(name)).nicks.contains(args[1])) {
                            pInf.get(getID(name)).nicks.remove(args[1]);
                            if (worldGuard && obCanUse)
                                obWorldGuard.removeMember(inv.getName(), getID(name));
                            inv.performCommand("ob j");
                            return true;
                        }
                    } else if (pInf.get(getID(name)).nicks.contains(args[1])) {
                        pInf.get(getID(name)).nicks.remove(args[1]);
                        sender.sendMessage(String.format("%sYou can't kick yourself.", ChatColor.YELLOW));
                    }
                    return true;
                }
                case ("accept"): {
                    Player pl = (Player) sender;
                    if (checkinvite(pl))
                        sender.sendMessage(String.format("%sSuccesfully accepted the invitation.", ChatColor.GREEN));
                    else
                        sender.sendMessage(
                                String.format("%s[There is no Pending invitations for you.]", ChatColor.RED));
                    return true;
                }
                case ("idreset"): {
                    Player pl = (Player) sender;
                    String name = pl.getName();
                    if (!existID(name))
                        return true;
                    int playerID = getID(name);
                    if (progressBar)
                        pInf.get(playerID).bar.removePlayer(pl);
                    PlayerInfo plp = pInf.get(playerID);
                    if (plp.nick.equals(name)) {
                        if (!plp.nicks.isEmpty()) {
                            plp.nick = plp.nicks.get(0);
                            plp.nicks.remove(0);
                        } else
                            plp.nick = null;
                    } else
                        plp.nicks.remove(name);
                    if (worldGuard && obCanUse)
                        obWorldGuard.removeMember(name, playerID);
                    if (!args[args.length - 1].equals("/n"))
                        sender.sendMessage(
                                String.format(
                                        "%sNow your data has been reset. You can create a new customisland /ob join.",
                                        ChatColor.GREEN));
                    return true;
                }
                case ("protection"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length > 1 &&
                            (args[1].equals("true") || args[1].equals("false"))) {
                        protection = Boolean.valueOf(args[1]);
                        config.set("protection", protection);
                    } else
                        sender.sendMessage(String.format("%senter a valid value true or false", ChatColor.YELLOW));
                    sender.sendMessage(String.format("%sthe protection is now %s", ChatColor.GREEN,
                            (protection ? "enabled." : "disabled.")));
                    return true;
                }
                case ("worldguard"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (!Bukkit.getPluginManager().isPluginEnabled(wgph)) {
                        sender.sendMessage(
                                String.format("%sThe worldGuard plugin was not detected!", ChatColor.YELLOW));
                        return true;
                    }
                    if (obWorldGuard == null || !obCanUse) {
                        sender.sendMessage(
                                String.format("%sThis feature is only available in the premium version of the plugin!",
                                        ChatColor.YELLOW));
                        return true;
                    }
                    if (args.length > 1 &&
                            (args[1].equals("true") || args[1].equals("false"))) {
                        worldGuard = Boolean.valueOf(args[1]);
                        config.set(wgph, worldGuard);
                        if (worldGuard)
                            recreateworldGuard();
                        else
                            obWorldGuard.RemoveRegions(id);
                    } else
                        sender.sendMessage(String.format("%senter a valid value true or false", ChatColor.YELLOW));
                    sender.sendMessage(String.format("%sthe obWorldGuard is now %s", ChatColor.GREEN,
                            (worldGuard ? "enabled." : "disabled.")));
                    return true;
                }
                case ("autojoin"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length > 1 &&
                            (args[1].equals("true") || args[1].equals("false"))) {
                        autojoin = Boolean.valueOf(args[1]);
                        config.set("autojoin", autojoin);
                    } else
                        sender.sendMessage(String.format("%senter a valid value true or false", ChatColor.YELLOW));
                    sender.sendMessage(String.format("%sautojoin is now %s", ChatColor.GREEN,
                            (autojoin ? "enabled." : "disabled.")));
                    return true;
                }
                // LVL
                case ("setlevel"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length <= 2) {
                        sender.sendMessage(
                                String.format("%sinvalid format. try: /ob setlevel 'nickname' 'level'", ChatColor.RED));
                        return true;
                    }
                    if (existID(args[1])) {
                        int setlvl = 0;
                        try {
                            setlvl = Integer.parseInt(args[2]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(String.format("%sinvalid level value.", ChatColor.RED));
                            return true;
                        }
                        if (setlvl >= 0 && 10000 > setlvl) {
                            int i = getID(args[1]);
                            PlayerInfo curPlayer = pInf.get(i);
                            curPlayer.breaks = 0;
                            curPlayer.lvl = setlvl;
                            if (levelBarMode) {
                                Level lvl = maxlevel;
                                if (curPlayer.lvl < levels.size())
                                    lvl = levels.get(curPlayer.lvl);
                                curPlayer.bar.setTitle(lvl.name);
                                curPlayer.bar.setColor(lvl.color);
                            }
                            sender.sendMessage(String.format("%sfor player %s, level %s is set.", ChatColor.GREEN,
                                    args[1], args[2]));
                            return true;
                        }
                        sender.sendMessage(String.format("%sinvalid level value.", ChatColor.RED));
                        return true;
                    }
                    sender.sendMessage(String.format("%sa player named %s was not found.", ChatColor.RED, args[1]));
                    return true;
                }
                case ("clear"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length <= 1) {
                        sender.sendMessage(String.format("%sinvalid format. try: /ob clear 'nickname'", ChatColor.RED));
                        return true;
                    }
                    if (existID(args[1])) {
                        int i = getID(args[1]);
                        PlayerInfo curPlayer = pInf.get(i);
                        curPlayer.breaks = 0;
                        curPlayer.lvl = 0;
                        if (progressBar)
                            curPlayer.bar.setVisible(false);
                        int xNow = x + i * 100 - 12;
                        int yNow = y - 6;
                        int zNow = z - 12;
                        if (yNow <= 1)
                            yNow = 1;
                        for (int xx = 0; xx < 24; xx++)
                            for (int yy = 0; yy < 16; yy++)
                                for (int zz = 0; zz < 24; zz++)
                                    world.getBlockAt(xNow + xx, yNow + yy, zNow + zz).setType(Material.AIR);
                        sender.sendMessage(
                                String.format("%splayer %s customisland is destroyed! :D", ChatColor.GREEN, args[1]));
                        return true;
                    }
                    sender.sendMessage(String.format("%sa player named %s was not found.", ChatColor.RED, args[1]));
                    return true;
                }
                case ("levelMultiplier"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length <= 1) {
                        sender.sendMessage(
                                String.format("%slevel multiplier now: %d%n5 by default", ChatColor.GREEN,
                                        levelMultiplier));
                        return true;
                    }
                    int lvl = levelMultiplier;
                    try {
                        lvl = Integer.parseInt(args[1]);
                    } catch (NumberFormatException nfe) {
                        sender.sendMessage(String.format("%sinvalid multiplier value.", ChatColor.RED));
                        return true;
                    }
                    if (lvl <= 20 && lvl >= 0) {
                        levelMultiplier = lvl;
                        config.set("level_multiplier", levelMultiplier);
                    } else
                        sender.sendMessage(String.format("%spossible values: from 0 to 20.", ChatColor.RED));
                    sender.sendMessage(
                            String.format("%slevel multiplier now: %d%n5 by default", ChatColor.GREEN,
                                    levelMultiplier));
                    return true;
                }
                case ("progressbarph"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (superlegacy) {
                        sender.sendMessage(String.format(
                                "%sYou server version is super legacy! ProgressBar unsupported!", ChatColor.RED));
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(String.format("%sand?", ChatColor.YELLOW));
                        return true;
                    }
                    if (args[1].equals("true") || args[1].equals("false")) {
                        progressBar = Boolean.valueOf(args[1]);
                        if (progressBar) {
                            if (progressBarColor == null)
                                progressBarColor = BarColor.GREEN;
                            blockFile();
                        }
                        for (PlayerInfo bb : pInf)
                            if (bb.bar != null)
                                bb.bar.setVisible(progressBar);
                        config.set(progressbarph, progressBar);
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("color")) {
                        if (args.length == 2) {
                            sender.sendMessage(String.format("%senter a color name.", ChatColor.YELLOW));
                            return true;
                        }
                        try {
                            progressBarColor = BarColor.valueOf(args[2]);
                            for (PlayerInfo bb : pInf)
                                bb.bar.setColor(progressBarColor);
                            blockFile();
                            config.set("progressBar_color", progressBarColor.toString());
                        } catch (Exception e) {
                            sender.sendMessage(
                                    String.format("%sPlease enter a valid color. For example: RED", ChatColor.YELLOW));
                        }
                        sender.sendMessage(
                                String.format("%sProgress bar color = %s", ChatColor.GREEN,
                                        progressBarColor.toString()));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("level")) {
                        if (!progressBar)
                            return true;
                        if (!levelBarMode) {
                            levelBarMode = true;
                            for (PlayerInfo curPlayer : pInf)
                                if (curPlayer.lvl >= levels.size())
                                    curPlayer.bar.setTitle(maxlevel.name);
                                else
                                    curPlayer.bar.setTitle(levels.get(curPlayer.lvl).name);
                            config.set(progressbartext, "level");
                            return true;
                        } else {
                            levelBarMode = false;
                            for (PlayerInfo bb : pInf)
                                bb.bar.setTitle("Progress bar");
                            config.set(progressbartext, "Progress bar");
                            return true;
                        }
                    }
                    if (args[1].equalsIgnoreCase("settext")) {
                        if (!progressBar)
                            return true;
                        StringBuilder textBar = new StringBuilder();
                        for (int i = 2; i < args.length - 1; i++)
                            textBar.append(args[i] + " ");
                        textBar.append(args[args.length - 1]);
                        levelBarMode = false;
                        for (PlayerInfo bb : pInf)
                            bb.bar.setTitle(textBar.toString());
                        config.set(progressbartext, textBar.toString());
                        textP = textBar.toString();
                        if (papi)
                            for (Player ponl : Bukkit.getOnlinePlayers())
                                pInf.get(getID(ponl.getName())).bar
                                        .setTitle(PlaceholderAPI.setPlaceholders(ponl, textBar.toString()));
                        return true;
                    }
                    sender.sendMessage(String.format("%strue, false, settext or level only!", ChatColor.RED));
                    return true;
                }
                case ("listlvl"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length >= 2) {
                        int temp = 0;
                        try {
                            temp = Integer.parseInt(args[1]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(String.format("%sinvalid value", ChatColor.RED));
                            return true;
                        }
                        if (levels.size() <= temp || temp < 0) {
                            sender.sendMessage(String.format("%sundefined lvl", ChatColor.RED));
                            return true;
                        }
                        sender.sendMessage(String.format("%s%s", ChatColor.GREEN, levels.get(temp).name));
                        int i = 0;
                        if (temp != 0)
                            i = levels.get(temp - 1).size;
                        for (; i < levels.get(temp).size; i++)
                            if (blocks.get(i) == null)
                                sender.sendMessage("Grass or undefined");
                            else if (blocks.get(i).getClass() == XMaterial.class)
                                sender.sendMessage(((XMaterial) blocks.get(i)).name());
                            else
                                sender.sendMessage((String) blocks.get(i));
                        return true;
                    }
                    for (int i = 0; i < levels.size(); i++)
                        sender.sendMessage(String.format("%d: %s%s", i, ChatColor.GREEN, levels.get(i).name));
                    return true;
                }
                case ("reload"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(String.format("%sReloading Plugin & Plugin Modules.", ChatColor.YELLOW));
                        blockFile();
                        flowerFile();
                        chestFile();
                        mobFile();
                        recreateworldGuard();
                        sender.sendMessage(String.format("%sAll .yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(blocksfile)) {
                        blockFile();
                        sender.sendMessage(String.format("%sBlocks.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(flowerfile)) {
                        flowerFile();
                        sender.sendMessage(String.format("%sFlowers.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(chestfile)) {
                        chestFile();
                        sender.sendMessage(String.format("%sChests.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase(mobfile)) {
                        mobFile();
                        sender.sendMessage(String.format("%sMobs.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    sender.sendMessage(String.format("%sTry blocks.yml or chests.yml", ChatColor.RED));
                    return true;
                }
                case ("chatAlert"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    chatAlert = !chatAlert;
                    sender.sendMessage(
                            ChatColor.GREEN + (chatAlert ? "Alerts are now on!" : "Alerts are now disabled!"));
                    config.set("chatAlert", chatAlert);
                    return true;
                }
                case ("frequency"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                        return true;
                    }
                    Long frequencytemp;
                    String frequencyName = "";
                    try {
                        frequencytemp = Long.parseLong(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                        return true;
                    }
                    if (frequencytemp >= 4L && frequencytemp <= 20L && on) {
                        frequency = frequencytemp;
                        Bukkit.getScheduler().cancelTasks(this);
                        config.set("frequency", frequency);
                        if (frequency == 4L)
                            frequencyName = " (Extreme)";
                        else if (frequency < 7L)
                            frequencyName = " (Fast)";
                        else if (frequency == 7L)
                            frequencyName = " (Default)";
                        else if (frequency < 9L)
                            frequencyName = " (Normal)";
                        else if (frequency < 13L)
                            frequencyName = " (Slow)";
                        else if (frequency < 17L)
                            frequencyName = " (Slower)";
                        else
                            frequencyName = " (Max TPS)";
                        Bukkit.getScheduler().runTaskTimer(this, new Task(), frequency, frequency * 2);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Now frequency = " + frequency + frequencyName);
                    return true;
                }
                case ("islands"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value true or false");
                        return true;
                    }
                    if (args[1].equals("true") || args[1].equals("false")) {
                        il3x3 = Boolean.valueOf(args[1]);
                        config.set("Island_for_new_players", il3x3);
                        sender.sendMessage(ChatColor.GREEN + "Island_for_new_players = " + il3x3);
                        return true;
                    }
                    if (args[1].equals("set_my_by_def")) {
                        if (legacy) {
                            sender.sendMessage(ChatColor.RED + "Not supported in legacy versions!");
                            return true;
                        }
                        Player p = (Player) sender;
                        String name = p.getName();
                        if (existID(name)) {
                            if (customisland == null)
                                customisland = new BlockData[7][3][7];
                            int px = x + getID(name) * space - 3;
                            for (int xx = 0; xx < 7; xx++)
                                for (int yy = 0; yy < 3; yy++)
                                    for (int zz = 0; zz < 7; zz++)
                                        customisland[xx][yy][zz] = world.getBlockAt(px + xx, y + yy, z - 3 + zz)
                                                .getBlockData();
                            sender.sendMessage(ChatColor.GREEN
                                    + "Your customisland has been successfully saved and set as default for new players!");
                        } else
                            sender.sendMessage(ChatColor.RED + "You don't have an customisland!");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("default")) {
                        if (legacy) {
                            sender.sendMessage(ChatColor.RED + "Not supported in legacy versions!");
                            return true;
                        }
                        config.set("custom_island", null);
                        customisland = null;
                        sender.sendMessage(ChatColor.GREEN + "The default customisland is installed.");
                        return true;
                    }
                    sender.sendMessage(ChatColor.YELLOW + "enter a valid value true or false");
                    return true;
                }
                case ("island_rebirth"): {
                    if (!sender.hasPermission(permissionSet)) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value true or false");
                        return true;
                    }
                    if (args[1].equals("true") || args[1].equals("false")) {
                        rebirth = Boolean.valueOf(args[1]);
                        config.set("Rebirth_on_the_island", rebirth);
                        sender.sendMessage(ChatColor.GREEN + "Rebirth_on_the_island = " + rebirth);
                        return true;
                    }
                    sender.sendMessage(ChatColor.YELLOW + "enter a valid value true or false");
                    return true;
                }
                case ("help"): {
                    sender.sendMessage(ChatColor.GREEN + "OnlyBlock Plugin Help");
                    boolean admin = sender.hasPermission(permissionSet);
                    if (admin)
                        sender.sendMessage(ChatColor.GRAY + "/ob set" + ChatColor.WHITE
                                + " - sets the location of the first customisland.");
                    sender.sendMessage(
                            ChatColor.GRAY + "/ob j" + ChatColor.WHITE + " - join a new one or your own customisland.");
                    if (admin)
                        sender.sendMessage(ChatColor.GRAY + "/ob protection" + ChatColor.WHITE
                                + " - does not allow players to leave their customisland.");
                    sender.sendMessage(ChatColor.GRAY + "/ob invite 'playername'" + ChatColor.WHITE
                            + " - an invitation to the customisland.\n" +
                            ChatColor.GRAY + "/ob accept" + ChatColor.WHITE + " - to accept an invitation.");
                    if (admin) {
                        sender.sendMessage(ChatColor.GRAY + "/ob islands true" + ChatColor.WHITE
                                + " - islands for new players.\n" +
                                ChatColor.GRAY + "/ob islands set_my_by_def" + ChatColor.WHITE
                                + " - sets your customisland as default for new players.");
                    }
                    sender.sendMessage(
                            ChatColor.GRAY + "/ob IDreset" + ChatColor.WHITE + " - deletes the player's data.");
                    return true;
                }
                default:
                    // ver
                    sender.sendMessage(String.format("%s%s%n%s%n%s%n%s%n%s%s",
                            ChatColor.values()[rnd.nextInt(ChatColor.values().length)],
                            "  ▄▄    ▄▄",
                            "█    █  █▄▀",
                            "▀▄▄▀ █▄▀",
                            "Created by Yoshoo\nPlugin version: v0.0.1",
                            "Server version: ",
                            superlegacy ? "super legacy(1.7 - 1.8)" : (legacy ? "legacy(1.9 - 1.12)" : version)));
                    return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Error.");
            return false;
        }
    }

    ArrayList<Player> playerList() {
        ArrayList<Player> pls = new ArrayList<>();
        for (Player ponl : online)
            if (existID(ponl.getName()))
                pls.add(ponl);
        return pls;
    }

    boolean checkInvalidID(String name) {
        for (PlayerInfo pl : pInf) {
            if (pl.nick == null)
                continue;
            if (pl.nick.equals(name))
                return true;
        }
        return false;
    }

    boolean existID(String name) {
        for (PlayerInfo pl : pInf) {
            if (pl.nick == null)
                continue;
            if (pl.nick.equals(name))
                return true;
            if (pl.nicks.contains(name))
                return true;
        }
        return false;
    }

    private void recreateworldGuard() {
        if (!worldGuard || !obCanUse) {
            return;
        } else {
            obWorldGuard.RemoveRegions(id);
            for (int i = 0; i < id; i++) {
                PlayerInfo owner = pInf.get(i);
                if (owner.nick == null)
                    continue;
                String name = owner.nick;
                int xWG = x + i * space;
                Vector block1 = new Vector(xWG - space / 2 + 1, 0, z - 100);
                Vector block2 = new Vector(xWG + space / 2 - 1, 255, z + 100);
                obWorldGuard.CreateRegion(name, block1, block2, i);
                for (String member : owner.nicks)
                    obWorldGuard.addMember(member, i);
            }

        }

    }

    public void saveData() {
        try {
            File playerData = new File(getDataFolder(), playerDF);
            JsonSimple.Write(id, pInf, playerData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Bukkit.getLogger().info("[OnlyBlock]Player Data Saved Successfully");
        }
    }

    // LOAD CONFIGURATIONS

    private void blockFile() {
        blocks.clear();
        levels.clear();
        File block = new File(getDataFolder(), blocksfile);
        if (!block.exists())
            saveResource(blocksfile, false);
        newConfig = YamlConfiguration.loadConfiguration(block);
        if (newConfig.isString("MaxLevel"))
            maxlevel.name = newConfig.getString("MaxLevel");
        for (int i = 0; newConfig.isList(String.format("%d", i)); i++) {
            List<String> blockTemp = newConfig.getStringList(String.format("%d", i));
            Level level = new Level(blockTemp.get(0));
            levels.add(level);
            int q = 1;
            if (progressBar && q < blockTemp.size())
                try {
                    level.color = BarColor.valueOf(blockTemp.get(1));
                    q++;
                } catch (Exception e) {
                    level.color = progressBarColor;
                }
            for (; q < blockTemp.size(); q++) {
                String text = blockTemp.get(q);
                Optional<XMaterial> a = XMaterial.matchXMaterial(text);
                if (text.charAt(0) == '/')
                    blocks.add(text.replaceFirst("/", ""));
                else if (!a.isPresent() || a.get() == grassBlock)
                    blocks.add(null);
                else
                    blocks.add(a.get());
            }
            level.size = blocks.size();
        }
        maxlevel.size = blocks.size();
        // progressBar
        if (!superlegacy && progressBar && !pInf.isEmpty() && pInf.get(0).bar == null) {
            maxlevel.color = progressBarColor;
            for (PlayerInfo curPlayer : pInf) {
                Level lvl = maxlevel;
                if (curPlayer.lvl < levels.size())
                    lvl = levels.get(curPlayer.lvl);
                curPlayer.bar = Bukkit.createBossBar(levelBarMode ? lvl.name : textP, lvl.color, BarStyle.SEGMENTED_10,
                        BarFlag.DARKEN_SKY);
            }
            Bukkit.getPluginManager().registerEvents(new ChangedWorld(), this);
        }
    }

    private void flowerFile() {
        flowers.clear();
        File flower = new File(getDataFolder(), flowerfile);
        if (!flower.exists())
            saveResource(flowerfile, false);
        newConfig = YamlConfiguration.loadConfiguration(flower);
        flowers.add(grass);
        for (String list : newConfig.getStringList("flowers"))
            if (!XMaterial.matchXMaterial(list).isPresent())
                flowers.add(grass);
            else {
                XMaterial temp = null;
                if (XMaterial.matchXMaterial(list).isPresent()) {
                    temp = XMaterial.matchXMaterial(list).get();
                    flowers.add(temp);
                }
            }

    }

    private void chestFile() {
        smallChest = new ArrayList<>();
        mediumChest = new ArrayList<>();
        hardChest = new ArrayList<>();
        File chest = new File(getDataFolder(), chestfile);
        if (!chest.exists())
            saveResource(chestfile, false);
        newConfig = YamlConfiguration.loadConfiguration(chest);
        for (String s : newConfig.getStringList("small_chest"))
            smallChest.add(Material.getMaterial(s));
        for (String s : newConfig.getStringList("medium_chest"))
            mediumChest.add(Material.getMaterial(s));
        for (String s : newConfig.getStringList("high_chest"))
            hardChest.add(Material.getMaterial(s));
    }

    private void mobFile() {

        mobs.clear();
        File mob = new File(getDataFolder(), mobfile);
        if (!mob.exists())
            saveResource(mobfile, false);
        newConfig = YamlConfiguration.loadConfiguration(mob);
        for (int i = 0; newConfig.isString("id" + i); i++) {
            try {
                mobs.add(EntityType.valueOf((newConfig.getString("id" + i))));
            } catch (Exception ex) {
                // not supported mob)
            }
        }
    }

    private void dataFile() {
        File playerData = new File(getDataFolder(), playerDF);
        if (playerData.exists())
            pInf = JsonSimple.Read(playerData);
        if (worldGuard && obCanUse) {
            recreateworldGuard();
        }
        id = pInf.size();
        return;
    }

    private void configFile() {
        File con = new File(getDataFolder(), configfile);
        if (!con.exists())
            saveResource(configfile, false);
        config = this.getConfig();
        world = Bukkit.getWorld(check(worldph, worldph));
        x = (int) check("x", (double) x);
        y = (int) check("y", (double) y);
        z = (int) check("z", (double) z);
        // leave - leaf
        if (config.isString("leafworld")) {
            config.set(lworldph, config.getString("leafworld"));
            config.set("leafworld", null);
        }
        if (config.isSet("xleaf")) {
            config.set("xleave", config.getDouble("xleaf"));
            config.set("xleaf", null);
        }
        if (config.isSet("yleaf")) {
            config.set(yl, config.getDouble("yleaf"));
            config.set("yleaf", null);
        }
        if (config.isSet("zleaf")) {
            config.set("zleave", config.getDouble("zleaf"));
            config.set("zleaf", null);
        }
        leaveworld = Bukkit.getWorld(check(lworldph, worldph));
        check("xleave", 0.0);
        check(yl, 0.0);
        check("zleave", 0.0);
        progressBar = check(progressbarph, true);
        if (superlegacy)
            progressBar = false;
        if (!config.isInt("frequency"))
            config.set("frequency", 7L);
        frequency = config.getLong("frequency");
        // Text
        if (!superlegacy) {
            textP = check(progressbartext, "level");
            if (textP.equals("level"))
                levelBarMode = true;
        }
        // alert
        chatAlert = check("chatAlert", !levelBarMode);
        if (progressBar)
            progressBarColor = BarColor.valueOf(check("progressBar_color", "GREEN"));
        il3x3 = check("Island_for_new_players", true);
        rebirth = check("Rebirth_on_the_island", true);
        levelMultiplier = check("level_multiplier", levelMultiplier);
        protection = check("protection", protection);
        if (worldGuard && obCanUse) {
            worldGuard = check(wgph, worldGuard);
        }
        autojoin = check("autojoin", autojoin);
        if (config.isSet("custom_island") && !legacy) {
            customisland = new BlockData[7][3][7];
            for (int yy = 0; yy < 3; yy++) {
                List<String> customIslandList = config.getStringList(String.format("custom_island.y%d", yy));
                for (int xx = 0; xx < 7; xx++)
                    for (int zz = 0; zz < 7; zz++)
                        customisland[xx][yy][zz] = Bukkit.createBlockData(customIslandList.get(7 * xx + zz));
            }
        }
        if (config.isInt("set"))
            space = config.getInt("set");
        Config.Save(config, con);
    }

    // ALL CHECKS

    String check(String type, String data) {
        if (!config.isString(type))
            config.set(type, data);
        return config.getString(type);
    }

    int check(String type, int data) {
        if (!config.isInt(type))
            config.set(type, data);
        return config.getInt(type);
    }

    double check(String type, double data) {
        if (!config.isDouble(type))
            config.set(type, data);
        return config.getDouble(type);
    }

    boolean check(String type, boolean data) {
        if (!config.isBoolean(type))
            config.set(type, data);
        return config.getBoolean(type);
    }

    // GET METHODS

    public static int getlvl(String playerName) {
        return pInf.get(getID(playerName)).lvl;
    }

    public static int getnextlvl(String playerName) {
        return getlvl(playerName) + 1;
    }

    public static String getlvlname(String playerName) {
        int lvl = getlvl(playerName);
        if (lvl < levels.size())
            return levels.get(lvl).name;
        return maxlevel.name;
    }

    public static String getnextlvlname(String playerName) {
        int lvl = getnextlvl(playerName);
        if (lvl < levels.size())
            return levels.get(lvl).name;
        return maxlevel.name;
    }

    public static int getblocks(String playerName) {
        return pInf.get(getID(playerName)).breaks;
    }

    public static int getneed(String playerName) {
        PlayerInfo idPlayer = pInf.get(getID(playerName));
        return 16 + idPlayer.lvl * levelMultiplier - idPlayer.breaks;
    }

    static int getID(String name) {
        for (int i = 0; i < pInf.size(); i++) {
            PlayerInfo pl = pInf.get(i);
            if (pl.nick == null)
                continue;
            if (pl.nick.equals(name))
                return i;
            if (pl.nicks.contains(name))
                return i;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public static PlayerInfo gettop(int i) {
        if (pInf.size() <= i)
            return new PlayerInfo();
        ArrayList<PlayerInfo> ppii = (ArrayList<PlayerInfo>) pInf.clone();
        Collections.sort(ppii, PlayerInfo.COMPARE_BY_LVL);
        return ppii.get(i);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            commands.addAll(Arrays.asList("j", "join", "leave", "invite", "accept", "kick", "ver", "IDreset", "help"));
            if (sender.hasPermission(permissionSet)) {
                commands.addAll(Arrays.asList("set", "setleave", progressbarph, "chatAlert", "setlevel", "clear",
                        "levelMultiplier", "reload", "frequency", "islands", "island_rebirth", "protection", wgph,
                        "listlvl", "autoJoin"));
            }
        } else if (args.length == 2) {
            if (args[0].equals("invite") || args[0].equals("kick")) {
                for (Player ponl : online)
                    commands.add(ponl.getName());
            } else if (sender.hasPermission(permissionSet)) {
                switch (args[0]) {
                    case ("clear"):
                    case ("setlevel"): {
                        for (Player ponl : online)
                            commands.add(ponl.getName());
                        break;
                    }
                    case ("progressbarph"): {
                        commands.add("true");
                        commands.add("false");
                        commands.add("level");
                        commands.add("settext");
                        commands.add("color");
                        break;
                    }
                    case ("reload"): {
                        commands.add(blocksfile);
                        commands.add(chestfile);
                        commands.add(mobfile);
                        commands.add(flowerfile);
                        break;
                    }
                    case ("islands"):
                        commands.add("set_my_by_def");
                        commands.add("default");
                        break;
                    case ("island_rebirth"):
                    case ("protection"):
                    case ("worldguard"):
                    case ("autoJoin"):
                        commands.add("true");
                        commands.add("false");
                        break;
                    case ("listlvl"):
                        for (int i = 0; i < levels.size(); i++)
                            commands.add(String.format("%d", i));
                        break;
                    case ("frequency"):
                        for (int i = 4; i <= 20; i++)
                            commands.add(String.format("%d", i));
                        break;
                    case ("levelMultiplier"):
                        for (int i = 0; i <= 20; i++)
                            commands.add(String.format("%d", i));
                        break;
                    case ("set"):
                        commands.add("100");
                        commands.add("500");
                        break;
                    default:
                }
            }
        } else if (sender.hasPermission(permissionSet) && args.length == 3) {
            if (args[0].equals(progressbarph)) {
                if (args[1].equals("color"))
                    for (BarColor bc : BarColor.values())
                        commands.add(bc.name());
                if (args[1].equals("settext")) {
                    commands.add("...");
                    if (papi)
                        commands.add("%OB_lvl_name%. There are %OB_need_to_lvl_up% block(s) left.");
                }
            } else if (args[0].equals("setlevel")) {
                for (int i = 0; i < levels.size(); i++)
                    commands.add(String.format("%d", i));
            }
        }
        Collections.sort(commands);
        return commands;
    }
}