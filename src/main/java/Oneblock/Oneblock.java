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
    static World world;
    static int x = 0;
    static int y = 0;
    static int z = 0;

    // LEAVE WORLD DATA
    World leaveworld;

    // PLAYER DATA DECLARATIONS
    int id = 0;
    static ArrayList<PlayerInfo> pInf = new ArrayList<>();

    // BUKKIT CONFIG DECLARATIONS
    FileConfiguration config;
    FileConfiguration newConfig;

    // CONFIG VALUES
    boolean il3x3 = false;
    boolean rebirth = false;
    boolean autojoin = false;
    boolean lvl_bar_mode = false;
    boolean chat_alert = false;
    boolean protection = false;
    boolean PAPI = false;
    boolean WorldGuard = false;
    boolean Progress_bar = true;

    // VERSION
    boolean superlegacy;
    boolean legacy;
    String version = "";

    // VARIABLES
    String playerDF = "playerData.json";

    // ETC
    ArrayList<Object> blocks = new ArrayList<>();
    ArrayList<Material> s_ch;
    ArrayList<Material> m_ch;
    ArrayList<Material> h_ch;
    ArrayList<EntityType> mobs = new ArrayList<>();
    ArrayList<XMaterial> flowers = new ArrayList<>();
    static ArrayList<Level> levels = new ArrayList<>();
    static Level maxlevel = new Level("Level: MAX");
    static List<Player> online;
    static int lvl_mult = 5;
    String TextP = "";
    int space = 100;
    Long fr;
    BarColor Progress_color;
    OBWorldGuard OBWorldGuard;
    boolean OBCanUse = false;
    BlockData[][][] customisland = null;
    static ArrayList<Invitation> invite = new ArrayList<>();
    XMaterial GRASS_BLOCK = XMaterial.GRASS_BLOCK;
    XMaterial GRASS = XMaterial.GRASS;
    String noperm = String.format("%sYou don't have permission [Oneblock.set].", ChatColor.RED);

    @Override
    public void onEnable() {
        version = "1." + XMaterial.getVersion();
        superlegacy = !XMaterial.supports(9);// Is version 1.9 supported?
        legacy = !XMaterial.supports(13);// Is version 1.13 supported?
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PAPI = true;
            new OBP().register();
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] PlaceholderAPI has been found!");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            WorldGuard = true;
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] WorldGuard has been found!");
        }
        Configfile();
        Datafile();
        Blockfile();
        Flowerfile();
        Chestfile();
        Mobfile();
        if (config.getDouble("y") != 0) { // IF y!=0 then enable plugin
            // IF world is null or exit world is null call world_null_gen
            if (world == null || (config.getDouble("yleave") != 0 && leaveworld == null)) {
                Bukkit.getScheduler().runTaskTimer(this, new NullWorld(), 32, 64);
            } else {
                Bukkit.getScheduler().runTaskTimer(this, new Task(), fr, fr * 2);
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
                world = Bukkit.getWorld(config.getString("world"));
                leaveworld = Bukkit.getWorld(config.getString("leaveworld"));
            } else {
                Bukkit.getLogger().info("[OB] The initialization of the world was successful!");
                worldInit();
            }
        }
    }

    public void worldInit() {
        Bukkit.getScheduler().cancelTasks(this);
        if (config.getDouble("y") != 0) {
            Bukkit.getScheduler().runTaskTimer(this, new Task(), fr, fr * 2);
            on = true;
        }
    }

    public void addinvite(String name, String to) {
        for (Invitation item : invite)
            if (item.equals(name, to))
                return;
        Invitation tempinv = new Invitation(name, to);
        invite.add(tempinv);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                invite.remove(tempinv);
            }
        }, 300L);
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
            if (Progress_bar)
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
                    if (curPlayer.breaks >= 16 + curPlayer.lvl * lvl_mult) {
                        curPlayer.lvlup();
                        curMaxLevel = maxlevel;
                        if (curPlayer.lvl < levels.size())
                            curMaxLevel = levels.get(curPlayer.lvl);
                        if (Progress_bar) {
                            curPlayer.bar.setColor(curMaxLevel.color);
                            if (lvl_bar_mode)
                                curPlayer.bar.setTitle(curMaxLevel.name);
                        }
                        if (chat_alert)
                            ponl.sendMessage(String.format("%s%s", ChatColor.GREEN, curMaxLevel.name));
                    }
                    if (Progress_bar) {
                        if (!lvl_bar_mode && PAPI)
                            curPlayer.bar.setTitle(PlaceholderAPI.setPlaceholders(ponl, TextP));
                        curPlayer.bar.setProgress((double) curPlayer.breaks / (16 + curPlayer.lvl * lvl_mult));
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
                        XBlock.setType(block, GRASS_BLOCK);
                        if (rnd.nextInt(3) == 1)
                            XBlock.setType(world.getBlockAt(x + obpX, y + 1, z),
                                    flowers.get(rnd.nextInt(flowers.size())));
                    } else if (blocks.get(random) == XMaterial.CHEST) {
                        try {
                            block.setType(Material.CHEST);
                            Chest chest = (Chest) block.getState();
                            Inventory inv = chest.getInventory();
                            ArrayList<Material> ch_now;
                            if (random < 26)
                                ch_now = s_ch;
                            else if (random < 68)
                                ch_now = m_ch;
                            else
                                ch_now = h_ch;
                            int max = rnd.nextInt(3) + 2;
                            for (int i = 0; i < max; i++) {
                                Material m = ch_now.get(rnd.nextInt(ch_now.size()));
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
                                            XBlock.setType(world.getBlockAt(x + id * space + i, y, z + q), GRASS_BLOCK);
                            }
                        }
                        // WorldGuard
                        if (WorldGuard && OBCanUse) {
                            int xWG = x + id * space;
                            Vector Block1 = new Vector(xWG - space / 2 + 1, 0, z - 100);
                            Vector Block2 = new Vector(xWG + space / 2 - 1, 255, z + 100);
                            OBWorldGuard.CreateRegion(name, Block1, Block2, id);
                        }
                        id++;
                        saveData();
                        PlayerInfo curPlayer = new PlayerInfo();
                        pInf.add(curPlayer);
                        curPlayer.nick = name;
                        if (!superlegacy && Progress_bar) {
                            String temp = TextP;
                            if (lvl_bar_mode)
                                temp = levels.get(0).name;
                            else if (PAPI)
                                temp = PlaceholderAPI.setPlaceholders(p, TextP);
                            curPlayer.bar = (Bukkit.createBossBar(temp, levels.get(0).color, BarStyle.SEGMENTED_10,
                                    BarFlag.DARKEN_SKY));
                        }
                    }
                    if (!on) {
                        Bukkit.getScheduler().runTaskTimer(this, new Task(), fr, fr * 2);
                        on = true;
                    }
                    if (Progress_bar)
                        pInf.get(getID(name)).bar.setVisible(true);
                    p.teleport(new Location(world, x + getID(name) * space + 0.5, y + 1.2, z + 0.5));
                    if (WorldGuard && OBCanUse) {
                        OBWorldGuard.addMember(name, getID(name));
                    }
                    return true;
                }
                case ("leave"): {
                    Player p = (Player) sender;
                    if (!superlegacy)
                        pInf.get(getID(p.getName())).bar.removePlayer(p);
                    if (config.getDouble("yleave") == 0 || leaveworld == null)
                        return true;
                    p.teleport(new Location(leaveworld, config.getDouble("xleave"), config.getDouble("yleave"),
                            config.getDouble("zleave")));
                    return true;
                }
                case ("set"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    config.set("world", world.getName());
                    config.set("x", (double) x);
                    config.set("y", (double) y);
                    config.set("z", (double) z);
                    Config.Save(config);
                    world.getBlockAt(x, y, z).setType(GRASS_BLOCK.parseMaterial());
                    recreateWorldguard();
                    return true;
                }
                case ("setleave"): {
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    Player p = (Player) sender;
                    Location l = p.getLocation();
                    leaveworld = l.getWorld();
                    config.set("leaveworld", leaveworld.getName());
                    config.set("xleave", l.getX());
                    config.set("yleave", l.getY());
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
                            if (WorldGuard && OBCanUse)
                                OBWorldGuard.removeMember(inv.getName(), getID(name));
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
                    int PlId = getID(name);
                    if (Progress_bar)
                        pInf.get(PlId).bar.removePlayer(pl);
                    PlayerInfo plp = pInf.get(PlId);
                    if (plp.nick.equals(name)) {
                        if (plp.nicks.isEmpty()) {
                            plp.nick = plp.nicks.get(0);
                            plp.nicks.remove(0);
                        } else
                            plp.nick = null;
                    } else
                        plp.nicks.remove(name);
                    if (WorldGuard && OBCanUse)
                        OBWorldGuard.removeMember(name, PlId);
                    if (!args[args.length - 1].equals("/n"))
                        sender.sendMessage(
                                String.format(
                                        "%sNow your data has been reset. You can create a new customisland /ob join.",
                                        ChatColor.GREEN));
                    return true;
                }
                case ("protection"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                        sender.sendMessage(
                                String.format("%sThe WorldGuard plugin was not detected!", ChatColor.YELLOW));
                        return true;
                    }
                    if (OBWorldGuard == null || !OBCanUse) {
                        sender.sendMessage(
                                String.format("%sThis feature is only available in the premium version of the plugin!",
                                        ChatColor.YELLOW));
                        return true;
                    }
                    if (args.length > 1 &&
                            (args[1].equals("true") || args[1].equals("false"))) {
                        WorldGuard = Boolean.valueOf(args[1]);
                        config.set("WorldGuard", WorldGuard);
                        if (WorldGuard)
                            recreateWorldguard();
                        else
                            OBWorldGuard.RemoveRegions(id);
                    } else
                        sender.sendMessage(String.format("%senter a valid value true or false", ChatColor.YELLOW));
                    sender.sendMessage(String.format("%sthe OBWorldGuard is now %s", ChatColor.GREEN,
                            (WorldGuard ? "enabled." : "disabled.")));
                    return true;
                }
                case ("autojoin"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    if (!sender.hasPermission("Oneblock.set")) {
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
                            if (lvl_bar_mode) {
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
                    if (!sender.hasPermission("Oneblock.set")) {
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
                        if (Progress_bar)
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
                case ("lvl_mult"): {
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length <= 1) {
                        sender.sendMessage(
                                String.format("%slevel multiplier now: %d%n5 by default", ChatColor.GREEN, lvl_mult));
                        return true;
                    }
                    int lvl = lvl_mult;
                    try {
                        lvl = Integer.parseInt(args[1]);
                    } catch (NumberFormatException nfe) {
                        sender.sendMessage(String.format("%sinvalid multiplier value.", ChatColor.RED));
                        return true;
                    }
                    if (lvl <= 20 && lvl >= 0) {
                        lvl_mult = lvl;
                        config.set("level_multiplier", lvl_mult);
                    } else
                        sender.sendMessage(String.format("%spossible values: from 0 to 20.", ChatColor.RED));
                    sender.sendMessage(
                            String.format("%slevel multiplier now: %d%n5 by default", ChatColor.GREEN, lvl_mult));
                    return true;
                }
                case ("progress_bar"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                        Progress_bar = Boolean.valueOf(args[1]);
                        if (Progress_bar) {
                            if (Progress_color == null)
                                Progress_color = BarColor.GREEN;
                            Blockfile();
                        }
                        for (PlayerInfo bb : pInf)
                            if (bb.bar != null)
                                bb.bar.setVisible(Progress_bar);
                        config.set("Progress_bar", Progress_bar);
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("color")) {
                        if (args.length == 2) {
                            sender.sendMessage(String.format("%senter a color name.", ChatColor.YELLOW));
                            return true;
                        }
                        try {
                            Progress_color = BarColor.valueOf(args[2]);
                            for (PlayerInfo bb : pInf)
                                bb.bar.setColor(Progress_color);
                            Blockfile();
                            config.set("Progress_bar_color", Progress_color.toString());
                        } catch (Exception e) {
                            sender.sendMessage(
                                    String.format("%sPlease enter a valid color. For example: RED", ChatColor.YELLOW));
                        }
                        sender.sendMessage(
                                String.format("%sProgress bar color = %s", ChatColor.GREEN, Progress_color.toString()));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("level")) {
                        if (!Progress_bar)
                            return true;
                        if (!lvl_bar_mode) {
                            lvl_bar_mode = true;
                            for (PlayerInfo curPlayer : pInf)
                                if (curPlayer.lvl >= levels.size())
                                    curPlayer.bar.setTitle(maxlevel.name);
                                else
                                    curPlayer.bar.setTitle(levels.get(curPlayer.lvl).name);
                            config.set("Progress_bar_text", "level");
                            return true;
                        } else {
                            lvl_bar_mode = false;
                            for (PlayerInfo bb : pInf)
                                bb.bar.setTitle("Progress bar");
                            config.set("Progress_bar_text", "Progress bar");
                            return true;
                        }
                    }
                    if (args[1].equalsIgnoreCase("settext")) {
                        if (!Progress_bar)
                            return true;
                        String txt_bar = "";
                        for (int i = 2; i < args.length - 1; i++)
                            txt_bar += args[i] + " ";
                        txt_bar += args[args.length - 1];
                        lvl_bar_mode = false;
                        for (PlayerInfo bb : pInf)
                            bb.bar.setTitle(txt_bar);
                        config.set("Progress_bar_text", txt_bar);
                        TextP = txt_bar;
                        if (PAPI)
                            for (Player ponl : Bukkit.getOnlinePlayers())
                                pInf.get(getID(ponl.getName())).bar
                                        .setTitle(PlaceholderAPI.setPlaceholders(ponl, txt_bar));
                        return true;
                    }
                    sender.sendMessage(String.format("%strue, false, settext or level only!", ChatColor.RED));
                    return true;
                }
                case ("listlvl"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(String.format("%sReloading Plugin & Plugin Modules.", ChatColor.YELLOW));
                        Blockfile();
                        Flowerfile();
                        Chestfile();
                        Mobfile();
                        recreateWorldguard();
                        sender.sendMessage(String.format("%sAll .yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("blocks.yml")) {
                        Blockfile();
                        sender.sendMessage(String.format("%sBlocks.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("flowers.yml")) {
                        Flowerfile();
                        sender.sendMessage(String.format("%sFlowers.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("chests.yml")) {
                        Chestfile();
                        sender.sendMessage(String.format("%sChests.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("mobs.yml")) {
                        Mobfile();
                        sender.sendMessage(String.format("%sMobs.yml reloaded!", ChatColor.GREEN));
                        return true;
                    }
                    sender.sendMessage(String.format("%sTry blocks.yml or chests.yml", ChatColor.RED));
                    return true;
                }
                case ("chat_alert"): {
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    chat_alert = !chat_alert;
                    sender.sendMessage(
                            ChatColor.GREEN + (chat_alert ? "Alerts are now on!" : "Alerts are now disabled!"));
                    config.set("Chat_alert", chat_alert);
                    return true;
                }
                case ("frequency"): {
                    if (!sender.hasPermission("Oneblock.set")) {
                        sender.sendMessage(noperm);
                        return true;
                    }
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                        return true;
                    }
                    Long fr_;
                    String Sfr = "";
                    try {
                        fr_ = Long.parseLong(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                        return true;
                    }
                    if (fr_ >= 4L && fr_ <= 20L && on) {
                        fr = fr_;
                        Bukkit.getScheduler().cancelTasks(this);
                        config.set("frequency", fr);
                        if (fr == 4L)
                            Sfr = " (Extreme)";
                        else if (fr < 7L)
                            Sfr = " (Fast)";
                        else if (fr == 7L)
                            Sfr = " (Default)";
                        else if (fr < 9L)
                            Sfr = " (Normal)";
                        else if (fr < 13L)
                            Sfr = " (Slow)";
                        else if (fr < 17L)
                            Sfr = " (Slower)";
                        else
                            Sfr = " (Max TPS)";
                        Bukkit.getScheduler().runTaskTimer(this, new Task(), fr, fr * 2);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Now frequency = " + fr + Sfr);
                    return true;
                }
                case ("islands"): {
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    if (!sender.hasPermission("Oneblock.set")) {
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
                    boolean admin = sender.hasPermission("Oneblock.set");
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

    private void recreateWorldguard() {
        if (!WorldGuard || !OBCanUse) {
            return;
        } else {
            OBWorldGuard.RemoveRegions(id);
            for (int i = 0; i < id; i++) {
                PlayerInfo owner = pInf.get(i);
                if (owner.nick == null)
                    continue;
                String name = owner.nick;
                int xWG = x + i * space;
                Vector Block1 = new Vector(xWG - space / 2 + 1, 0, z - 100);
                Vector Block2 = new Vector(xWG + space / 2 - 1, 255, z + 100);
                OBWorldGuard.CreateRegion(name, Block1, Block2, i);
                for (String member : owner.nicks)
                    OBWorldGuard.addMember(member, i);
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

    private void Blockfile() {
        blocks.clear();
        levels.clear();
        File block = new File(getDataFolder(), "blocks.yml");
        if (!block.exists())
            saveResource("blocks.yml", false);
        newConfig = YamlConfiguration.loadConfiguration(block);
        if (newConfig.isString("MaxLevel"))
            maxlevel.name = newConfig.getString("MaxLevel");
        for (int i = 0; newConfig.isList(String.format("%d", i)); i++) {
            List<String> bl_temp = newConfig.getStringList(String.format("%d", i));
            Level level = new Level(bl_temp.get(0));
            levels.add(level);
            int q = 1;
            if (Progress_bar && q < bl_temp.size())
                try {
                    level.color = BarColor.valueOf(bl_temp.get(1));
                    q++;
                } catch (Exception e) {
                    level.color = Progress_color;
                }
            for (; q < bl_temp.size(); q++) {
                String text = bl_temp.get(q);
                Optional<XMaterial> a = XMaterial.matchXMaterial(text);
                if (text.charAt(0) == '/')
                    blocks.add(text.replaceFirst("/", ""));
                else if (!a.isPresent() || a.get() == GRASS_BLOCK)
                    blocks.add(null);
                else
                    blocks.add(a.get());
            }
            level.size = blocks.size();
        }
        maxlevel.size = blocks.size();
        // Progress_bar
        if (!superlegacy && Progress_bar && !pInf.isEmpty() && pInf.get(0).bar == null) {
            maxlevel.color = Progress_color;
            for (PlayerInfo curPlayer : pInf) {
                Level lvl = maxlevel;
                if (curPlayer.lvl < levels.size())
                    lvl = levels.get(curPlayer.lvl);
                curPlayer.bar = Bukkit.createBossBar(lvl_bar_mode ? lvl.name : TextP, lvl.color, BarStyle.SEGMENTED_10,
                        BarFlag.DARKEN_SKY);
            }
            Bukkit.getPluginManager().registerEvents(new ChangedWorld(), this);
        }
    }

    private void Flowerfile() {
        flowers.clear();
        File flower = new File(getDataFolder(), "flowers.yml");
        if (!flower.exists())
            saveResource("flowers.yml", false);
        newConfig = YamlConfiguration.loadConfiguration(flower);
        flowers.add(GRASS);
        for (String list : newConfig.getStringList("flowers"))
            if (!XMaterial.matchXMaterial(list).isPresent())
                flowers.add(GRASS);
            else {
                XMaterial temp = null;
                if (XMaterial.matchXMaterial(list).isPresent()) {
                    temp = XMaterial.matchXMaterial(list).get();
                    flowers.add(temp);
                }
            }

    }

    private void Chestfile() {
        s_ch = new ArrayList<>();
        m_ch = new ArrayList<>();
        h_ch = new ArrayList<>();
        File chest = new File(getDataFolder(), "chests.yml");
        if (!chest.exists())
            saveResource("chests.yml", false);
        newConfig = YamlConfiguration.loadConfiguration(chest);
        for (String s : newConfig.getStringList("small_chest"))
            s_ch.add(Material.getMaterial(s));
        for (String s : newConfig.getStringList("medium_chest"))
            m_ch.add(Material.getMaterial(s));
        for (String s : newConfig.getStringList("high_chest"))
            h_ch.add(Material.getMaterial(s));
    }

    private void Mobfile() {

        mobs.clear();
        File mob = new File(getDataFolder(), "mobs.yml");
        if (!mob.exists())
            saveResource("mobs.yml", false);
        newConfig = YamlConfiguration.loadConfiguration(mob);
        for (int i = 0; newConfig.isString("id" + i); i++) {
            try {
                mobs.add(EntityType.valueOf((newConfig.getString("id" + i))));
            } catch (Exception ex) {
                // not supported mob)
            }
        }
    }

    private void Datafile() {
        File playerData = new File(getDataFolder(), playerDF);
        if (playerData.exists())
            pInf = JsonSimple.Read(playerData);
        if (WorldGuard && OBCanUse) {
            recreateWorldguard();
        }
        id = pInf.size();
        return;
    }

    private void Configfile() {
        File con = new File(getDataFolder(), "config.yml");
        if (!con.exists())
            saveResource("config.yml", false);
        config = this.getConfig();
        world = Bukkit.getWorld(check("world", "world"));
        x = (int) check("x", (double) x);
        y = (int) check("y", (double) y);
        z = (int) check("z", (double) z);
        // leave - leaf
        if (config.isString("leafworld")) {
            config.set("leaveworld", config.getString("leafworld"));
            config.set("leafworld", null);
        }
        if (config.isSet("xleaf")) {
            config.set("xleave", config.getDouble("xleaf"));
            config.set("xleaf", null);
        }
        if (config.isSet("yleaf")) {
            config.set("yleave", config.getDouble("yleaf"));
            config.set("yleaf", null);
        }
        if (config.isSet("zleaf")) {
            config.set("zleave", config.getDouble("zleaf"));
            config.set("zleaf", null);
        }
        leaveworld = Bukkit.getWorld(check("leaveworld", "world"));
        check("xleave", 0.0);
        check("yleave", 0.0);
        check("zleave", 0.0);
        Progress_bar = check("Progress_bar", true);
        if (superlegacy)
            Progress_bar = false;
        if (!config.isInt("frequency"))
            config.set("frequency", 7L);
        fr = config.getLong("frequency");
        // Text
        if (!superlegacy) {
            TextP = check("Progress_bar_text", "level");
            if (TextP.equals("level"))
                lvl_bar_mode = true;
        }
        // alert
        chat_alert = check("Chat_alert", !lvl_bar_mode);
        if (Progress_bar)
            Progress_color = BarColor.valueOf(check("Progress_bar_color", "GREEN"));
        il3x3 = check("Island_for_new_players", true);
        rebirth = check("Rebirth_on_the_island", true);
        lvl_mult = check("level_multiplier", lvl_mult);
        protection = check("protection", protection);
        if (WorldGuard && OBCanUse) {
            WorldGuard = check("WorldGuard", WorldGuard);
        }
        autojoin = check("autojoin", autojoin);
        if (config.isSet("custom_island") && !legacy) {
            customisland = new BlockData[7][3][7];
            for (int yy = 0; yy < 3; yy++) {
                List<String> cust_s = config.getStringList(String.format("custom_island.y%d", yy));
                for (int xx = 0; xx < 7; xx++)
                    for (int zz = 0; zz < 7; zz++)
                        customisland[xx][yy][zz] = Bukkit.createBlockData(cust_s.get(7 * xx + zz));
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
        PlayerInfo id_pl = pInf.get(getID(playerName));
        return 16 + id_pl.lvl * lvl_mult - id_pl.breaks;
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
            if (sender.hasPermission("Oneblock.set")) {
                commands.addAll(Arrays.asList("set", "setleave", "Progress_bar", "chat_alert", "setlevel", "clear",
                        "lvl_mult", "reload", "frequency", "islands", "island_rebirth", "protection", "worldguard",
                        "listlvl", "autoJoin"));
            }
        } else if (args.length == 2) {
            if (args[0].equals("invite") || args[0].equals("kick")) {
                for (Player ponl : online)
                    commands.add(ponl.getName());
            } else if (sender.hasPermission("Oneblock.set")) {
                switch (args[0]) {
                    case ("clear"):
                    case ("setlevel"): {
                        for (Player ponl : online)
                            commands.add(ponl.getName());
                        break;
                    }
                    case ("Progress_bar"): {
                        commands.add("true");
                        commands.add("false");
                        commands.add("level");
                        commands.add("settext");
                        commands.add("color");
                        break;
                    }
                    case ("reload"): {
                        commands.add("blocks.yml");
                        commands.add("chests.yml");
                        commands.add("mobs.yml");
                        commands.add("flowers.yml");
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
                    case ("lvl_mult"):
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
        } else if (sender.hasPermission("Oneblock.set") && args.length == 3) {
            if (args[0].equals("Progress_bar")) {
                if (args[1].equals("color"))
                    for (BarColor bc : BarColor.values())
                        commands.add(bc.name());
                if (args[1].equals("settext")) {
                    commands.add("...");
                    if (PAPI)
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