// Copyright © 2022 MrMarL. All rights reserved.
package Oneblock;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.*;

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
import org.jetbrains.annotations.NotNull;

public class Oneblock extends JavaPlugin {
    // PLUGIN ENABLED ?
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
    boolean rebirthEnabled = false;
    boolean autojoinEnabled = false;
    boolean levelBarModeEnabled = false;
    boolean chatAlertEnabled = false;
    boolean protectionEnabled = false;
    boolean papiEnabled = false;
    boolean worldGuardEnabled = false;
    boolean progressBarEnabled = true;

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
    Long frequencyValue;
    BarColor progressBarColor;
    BlockData[][][] customisland = null;
    ArrayList<Invitation> invitations = new ArrayList<>();

    // WORLDGUARD
    OBWorldGuard obWorldGuard;
    boolean obCanUse = false;

    // MATERIALS
    static final XMaterial grassBlock = XMaterial.GRASS_BLOCK;
    static final XMaterial grass = XMaterial.GRASS;

    // CONFIG FILE ASSIGNMENTS
    public static final String WORLD_GUARD = "worldGuard";
    public static final String X_LEAVE = "xleave";
    public static final String Y_LEAVE = "yleave";
    public static final String Z_LEAVE = "zleave";
    public static final String DEFAULT_WORLD = "world";
    public static final String LEAVE_WORLD = "leaveworld";
    public static final String PROGRESS_BAR = "progressBar";
    public static final String PROGRESS_BAR_TEXT = "progressBar_text";
    public static final String ONEBLOCK_JOIN = "oneblock join";

    // FILE NAME ASSIGNMENTS
    public static final String BLOCKS_YML = "blocks.yml";
    public static final String FLOWERS_YML = "flowers.yml";
    public static final String MOBS_YML = "mobs.yml";
    public static final String CONFIG_YML = "config.yml";
    public static final String LEVELS_YML = "levels.yml";
    public static final String CHESTS_YML = "chests.yml";
    public static final String PLAYER_DATA_JSON = "playerData.json";

    // PERMISSIONS VARIABLES
    static String permissionSet = "Oneblock.set";

    //    LEAVE VARIABLES CONSTANTS (UNKNOWN USE TO ME)
    public static final String XLEAF = "xleaf";
    public static final String YLEAF = "yleaf";
    public static final String ZLEAF = "zleaf";
    public static final String LEAFWORLD = "leafworld";

    //    COMMAND ARGUMENTS
    public static final String FREQUENCY = "frequency";
    public static final String PROTECTION = "protection";
    public static final String INVITE = "invite";
    public static final String SETLEVEL = "setlevel";
    public static final String SET_TEXT = "settext";
    public static final String RELOAD = "reload";
    public static final String LISTLVL = "listlvl";
    public static final String CHAT_ALERT = "chatAlert";
    public static final String CUSTOM_ISLAND = "custom_island";
    public static final String AUTOJOIN = "autojoin";
    public static final String ISLANDS = "islands";
    public static final String
            ISLAND_REBIRTH = "island_rebirth";
    public static final String CLEAR = "clear";
    public static final String LEVEL = "level";
    public static final String COLOR = "color";

    //    COMMAND ARGUMENTS BOOLEANS
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    //    STRINGS
    public static final String TRUE_OR_FALSE = "enter a valid value true or false";
    public static final String NO_PERMISSION_OBSET = String.format("%sYou don't have permission [Oneblock.set].", ChatColor.RED);
    public static final String ENABLED = "enabled.";
    public static final String DISABLED = "disabled.";

    // --|PLUGIN STARTUP|--
    @Override
    public void onEnable() {
        version = "1." + XMaterial.getVersion(); // Get version number
        superlegacy = !XMaterial.supports(9);// Is version 1.9 supported?
        legacy = !XMaterial.supports(13);// Is version 1.13 supported?
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiEnabled = true;
            new OBP().register();
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] PlaceholderAPI has been found!");
        }
        if (Bukkit.getPluginManager().isPluginEnabled(WORLD_GUARD)) {
            worldGuardEnabled = true;
            Bukkit.getConsoleSender().sendMessage("[OnlyBlock] WorldGuard has been found!");
        }
        // Begin loading config files
        configFile();
        dataFile();
        blockFile();
        flowerFile();
        chestFile();
        mobFile();
        // Check if "/ob set" was called by checking y value in config.yml
        if (config.getDouble("y") != 0) { // IF y!=0 then enable plugin
            //  IF the world is null or exit world is null call world_null_gen
            if (world == null || (config.getDouble(Y_LEAVE) != 0 && leaveworld == null)) {
                Bukkit.getScheduler().runTaskTimer(this, new NullWorld(), 32, 64);
            } else {
                Bukkit.getScheduler().runTaskTimer(this, new Task(), frequencyValue, frequencyValue * 2);
                on = true;
            }
        }
        Bukkit.getPluginManager().registerEvents(new RespawnAutoJoin(), this);
    }

    public class RespawnAutoJoin implements Listener {
        @EventHandler
        public void respawn(PlayerRespawnEvent e) {
            if (rebirthEnabled && e.getPlayer().getWorld().equals(world) && existID(e.getPlayer().getName()))
                e.setRespawnLocation(
                        new Location(world, x + getID(e.getPlayer().getName()) * space + 0.5, y + 1.2,
                                z + 0.5));
        }

        @EventHandler
        public void teleport(PlayerTeleportEvent e) {
            if (autojoinEnabled) {
                Location loc = e.getTo();
                World from = e.getFrom().getWorld();
                assert loc != null;
                if(loc.getWorld() != null) {
                    World to = loc.getWorld();
                    if (from != null && !from.equals(world) && to.equals(world) &&
                            !(loc.getY() == y + 1.2 && loc.getZ() == z + 0.5)) {
                        e.setCancelled(true);
                        e.getPlayer().performCommand(ONEBLOCK_JOIN);
                    }
                }

            }
        }

        @EventHandler
        public void join(PlayerJoinEvent e) {
            if (autojoinEnabled) {
                Player pl = e.getPlayer();
                if (pl.getWorld().equals(world))
                    pl.performCommand(ONEBLOCK_JOIN);
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
                world = Bukkit.getWorld(Objects.requireNonNull(config.getString(DEFAULT_WORLD)));
                leaveworld = Bukkit.getWorld(Objects.requireNonNull(config.getString(LEAVE_WORLD)));
            } else {
                Bukkit.getLogger().info("[OB] The initialization of the world was successful!");
                worldInit();
            }
        }
    }

    public void worldInit() {
        Bukkit.getScheduler().cancelTasks(this);
        if (config.getDouble("y") != 0) {
            Bukkit.getScheduler().runTaskTimer(this, new Task(), frequencyValue, frequencyValue * 2);
            on = true;
        }
    }

    public void addInvite(String from, String to) {
        for (Invitation item : invitations)
            if (item.equals(from, to))
                return;
        Invitation tempInvite = new Invitation(from, to);
        invitations.add(tempInvite);
        class InviteDelete implements Runnable {
            @Override
            public void run() {
                invitations.remove(tempInvite);
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, new InviteDelete(), 300L);
    }

    public boolean checkInvite(Player player) {
        String name = player.getName();
        Invitation tempinv = null;
        for (Invitation item : invitations)
            if (item.Invited.equals(name))
                tempinv = item;
        if (tempinv == null || !existID(tempinv.Inviting))
            return false;
        if (existID(name)) {
            if (progressBarEnabled)
                pInf.get(getID(name)).bar.removePlayer(player);
            player.performCommand("oneblock idreset /n");
        }
        pInf.get(getID(tempinv.Inviting)).nicks.add(name);
        player.performCommand(ONEBLOCK_JOIN);
        invitations.remove(tempinv);
        return true;
    }

    public class Task implements Runnable {
        public void protect(Player ponl, int obpX) {
            if (protectionEnabled && !ponl.hasPermission("Oneblock.ignoreBarrier")) {
                int check = ponl.getLocation().getBlockX() - obpX - x;
                if (check > 50 || check < -50) {
                    if (check > 200 || check < -200) {
                        ponl.performCommand(ONEBLOCK_JOIN);
                        return;
                    }
                    ponl.setVelocity(new Vector(-check / 30, 0, 0));
                    ponl.sendMessage(String.format("%s%s%s%s", ChatColor.YELLOW, "are you trying to go ",
                            ChatColor.RED, "outside the Island ?"));
                }
            }
        }

        public void run() {
            synchronized (this) {
                online = world.getPlayers();
            }
            Collections.shuffle(online);
            for (Player ponl : online) {
                String name = ponl.getName();
                if (!existID(name))
                    continue;
                int obid = getID(name);
                int obpX = obid * space;

                // check for player exiting his Island
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
                        if (progressBarEnabled) {
                            curPlayer.bar.setColor(curMaxLevel.color);
                            if (levelBarModeEnabled)
                                curPlayer.bar.setTitle(curMaxLevel.name);
                        }
                        if (chatAlertEnabled)
                            ponl.sendMessage(String.format("%s%s", ChatColor.GREEN, curMaxLevel.name));
                    }
                    if (progressBarEnabled) {
                        if (!levelBarModeEnabled && papiEnabled)
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
            config.set(CUSTOM_ISLAND, map);
        }
        saveData();
        Config.Save(config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args)  {
        if (cmd.getName().equalsIgnoreCase("oneblock")) {
            //
            if (args.length == 0 && !(sender instanceof ConsoleCommandSender)){
                ((Player) sender).performCommand(ONEBLOCK_JOIN);
            } else if (sender instanceof ConsoleCommandSender && args.length == 0) {
                sender.sendMessage("Console cannot send this command !");
                return true;
            }

            if (!sender.hasPermission("Oneblock.join")) {
                sender.sendMessage(String.format("%sYou don't have permission [Oneblock.join].", ChatColor.RED));
                return true;
            }
            //
            if (args.length != 0) {
                switch (args[0].toLowerCase()) {
                    case ("j"):
                    case ("join"): {
                        Player player;
                        if(sender instanceof ConsoleCommandSender){
                            sender.sendMessage("Console cannot send this command !");
                            return true;
                        } else {player = (Player) sender;}
                        if (config.getInt("y") == 0 || world == null) {
                            sender.sendMessage(String.format("%sFirst you need to set the reference coordinates '/ob set'.",
                                    ChatColor.YELLOW));
                            return true;
                        }
                        String name = player.getName();
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
                            if (worldGuardEnabled && obCanUse) {
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
                            if (!superlegacy && progressBarEnabled) {
                                String temp = textP;
                                if (levelBarModeEnabled)
                                    temp = levels.get(0).name;
                                else if (papiEnabled)
                                    temp = PlaceholderAPI.setPlaceholders(player, textP);
                                curPlayer.bar = (Bukkit.createBossBar(temp, levels.get(0).color, BarStyle.SEGMENTED_10,
                                        BarFlag.DARKEN_SKY));
                            }
                        }
                        if (!on) {
                            Bukkit.getScheduler().runTaskTimer(this, new Task(), frequencyValue, frequencyValue * 2);
                            on = true;
                        }
                        if (progressBarEnabled)
                            pInf.get(getID(name)).bar.setVisible(true);
                        player.teleport(new Location(world, x + getID(name) * space + 0.5, y + 1.2, z + 0.5));
                        if (worldGuardEnabled && obCanUse) {
                            obWorldGuard.addMember(name, getID(name));
                        }
                        return true;
                    }
                    case ("leave"): {
                        Player p = (Player) sender;
                        if (!superlegacy)
                            pInf.get(getID(p.getName())).bar.removePlayer(p);
                        if (config.getDouble(Y_LEAVE) == 0 || leaveworld == null)
                            return true;
                        p.teleport(new Location(leaveworld, config.getDouble(X_LEAVE), config.getDouble(Y_LEAVE),
                                config.getDouble(Z_LEAVE)));
                        return true;
                    }
                    case ("set"): {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        Player p = (Player) sender;
                        Location l = p.getLocation();
                        x = l.getBlockX();
                        y = l.getBlockY();
                        z = l.getBlockZ();
                        world = l.getWorld();
                        int temp;
                        if (args.length >= 2) {
                            try {
                                temp = Integer.parseInt(args[1]);
                            } catch (NumberFormatException nfe) {
                                sender.sendMessage(String.format("%s Invalid value", ChatColor.RED));
                                return true;
                            }
                            if (temp > 1000 || temp < -1000) {
                                sender.sendMessage(
                                        String.format("%s Possible values are from -1000 to 1000", ChatColor.RED));
                                return true;
                            }
                            space = temp;
                            config.set("set", space);
                        }
                        if(world != null) {
                            config.set(DEFAULT_WORLD, world.getName());
                        }
                        config.set("x", (double) x);
                        config.set("y", (double) y);
                        config.set("z", (double) z);
                        Config.Save(config);
                        world.getBlockAt(x, y, z).setType(Objects.requireNonNull(grassBlock.parseMaterial()));
                        recreateWorldGuard();
                        return true;
                    }
                    case ("setleave"): {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        Player p = (Player) sender;
                        Location l = p.getLocation();
                        leaveworld = l.getWorld();
                        if (leaveworld != null){
                            config.set(LEAVE_WORLD, leaveworld.getName());
                        } else {
                            return false;
                        }
                        config.set(X_LEAVE, l.getX());
                        config.set(Y_LEAVE, l.getY());
                        config.set(Z_LEAVE, l.getZ());
                        Config.Save(config);
                        return true;
                    }
                    case INVITE: {
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
                            if (inv == sender) {
                                sender.sendMessage(String.format("%sYou can't invite yourself.", ChatColor.YELLOW));
                                return true;
                            }
                            if (!existID(sender.getName())) {
                                sender.sendMessage(
                                        String.format("%sPlease create a customisland before you do this.",
                                                ChatColor.YELLOW));
                                return true;
                            }
                            addInvite(sender.getName(), inv.getName());
                            inv.sendMessage(String.format("%sYou were invited by player %s.%n%s/ob accept to accept).",
                                    ChatColor.GREEN, sender.getName(), ChatColor.RED));
                            sender.sendMessage(String.format("%sSuccessfully invited %s.", ChatColor.GREEN, inv.getName()));
                        }
                        return true;
                    }
                    case ("kick"): {
                        if (args.length < 2) {
                            sender.sendMessage(String.format("%sUsage: /ob invite <username>", ChatColor.RED));
                            return true;
                        }
                        Player inv = Bukkit.getPlayer(args[1]);
                        String name = sender.getName();
                        if (!checkInvalidID(name))
                            return true;
                        if (inv != null) {
                            if (inv == sender) {
                                sender.sendMessage(String.format("%sYou can't kick yourself.", ChatColor.YELLOW));
                                return true;
                            }
                            if (pInf.get(getID(name)).nicks.contains(args[1])) {
                                pInf.get(getID(name)).nicks.remove(args[1]);
                                if (worldGuardEnabled && obCanUse)
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
                        if (checkInvite(pl))
                            sender.sendMessage(String.format("%sSuccessfully accepted the invitation.", ChatColor.GREEN));
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
                        if (progressBarEnabled)
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
                        if (worldGuardEnabled && obCanUse)
                            obWorldGuard.removeMember(name, playerID);
                        if (!args[args.length - 1].equals("/n"))
                            sender.sendMessage(
                                    String.format(
                                            "%sNow your data has been reset. You can create a new customisland /ob join.",
                                            ChatColor.GREEN));
                        return true;
                    }
                    case PROTECTION: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length > 1 &&
                                (args[1].equals(TRUE) || args[1].equals(FALSE))) {
                            protectionEnabled = Boolean.parseBoolean(args[1]);
                            config.set(PROTECTION, protectionEnabled);
                        } else
                            sender.sendMessage(String.format("%s Enter a valid value true or false", ChatColor.YELLOW));
                        sender.sendMessage(String.format("%s The protection is currently: %s", ChatColor.GRAY,
                                (protectionEnabled ? ENABLED : DISABLED)));
                        return true;
                    }
                    case ("worldguard"): {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (!Bukkit.getPluginManager().isPluginEnabled(WORLD_GUARD)) {
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
                                (args[1].equals(TRUE) || args[1].equals(FALSE))) {
                            worldGuardEnabled = Boolean.parseBoolean(args[1]);
                            config.set(WORLD_GUARD, worldGuardEnabled);
                            if (worldGuardEnabled)
                                recreateWorldGuard();
                            else
                                obWorldGuard.RemoveRegions(id);
                        } else
                            sender.sendMessage(String.format("%sEnter a valid value true or false", ChatColor.YELLOW));
                        sender.sendMessage(String.format("%sThe obWorldGuard is now %s", ChatColor.GREEN,
                                (worldGuardEnabled ? ENABLED : DISABLED)));
                        return true;
                    }
                    case AUTOJOIN: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length > 1 &&
                                (args[1].equals(TRUE) || args[1].equals(FALSE))) {
                            autojoinEnabled = Boolean.parseBoolean(args[1]);
                            config.set(AUTOJOIN, autojoinEnabled);
                        } else
                            sender.sendMessage(String.format("%sEnter a valid value true or false", ChatColor.YELLOW));
                        sender.sendMessage(String.format("%sAutojoin is now %s", ChatColor.GREEN,
                                (autojoinEnabled ? ENABLED : DISABLED)));
                        return true;
                    }
                    // LVL
                    case SETLEVEL: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length <= 2) {
                            sender.sendMessage(
                                    String.format("%sInvalid format. try: /ob setlevel 'nickname' 'level'", ChatColor.RED));
                            return true;
                        }
                        if (existID(args[1])) {
                            int setlvl;
                            try {
                                setlvl = Integer.parseInt(args[2]);
                            } catch (NumberFormatException nfe) {
                                sender.sendMessage(String.format("%sInvalid level value.", ChatColor.RED));
                                return true;
                            }
                            if (setlvl >= 0 && 10000 > setlvl) {
                                int i = getID(args[1]);
                                PlayerInfo curPlayer = pInf.get(i);
                                curPlayer.breaks = 0;
                                curPlayer.lvl = setlvl;
                                if (levelBarModeEnabled) {
                                    Level lvl = maxlevel;
                                    if (curPlayer.lvl < levels.size())
                                        lvl = levels.get(curPlayer.lvl);
                                    curPlayer.bar.setTitle(lvl.name);
                                    curPlayer.bar.setColor(lvl.color);
                                }
                                sender.sendMessage(String.format("%sFor player %s, level %s is set.", ChatColor.GREEN,
                                        args[1], args[2]));
                                return true;
                            }
                            sender.sendMessage(String.format("%sInvalid level value.", ChatColor.RED));
                            return true;
                        }
                        sender.sendMessage(String.format("%sA player named %s was not found.", ChatColor.RED, args[1]));
                        return true;
                    }
                    case CLEAR: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length <= 1) {
                            sender.sendMessage(String.format("%sInvalid format. try: /ob clear 'nickname'", ChatColor.RED));
                            return true;
                        }
                        if (existID(args[1])) {
                            int i = getID(args[1]);
                            PlayerInfo curPlayer = pInf.get(i);
                            curPlayer.breaks = 0;
                            curPlayer.lvl = 0;
                            if (progressBarEnabled)
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
                                    String.format("%sPlayer %s customisland is destroyed! :D", ChatColor.GREEN, args[1]));
                            return true;
                        }
                        sender.sendMessage(String.format("%sA player named %s was not found.", ChatColor.RED, args[1]));
                        return true;
                    }
                    case ("levelmultiplier"): {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length <= 1) {
                            sender.sendMessage(
                                    String.format("%s level multiplier now: %d%n5 by default", ChatColor.GREEN,
                                            levelMultiplier));
                            return true;
                        }
                        int lvl;
                        try {
                            lvl = Integer.parseInt(args[1]);
                        } catch (NumberFormatException nfe) {
                            sender.sendMessage(String.format("%sInvalid multiplier value.", ChatColor.RED));
                            return true;
                        }
                        if (lvl <= 20 && lvl >= 0) {
                            synchronized (this) {
                                levelMultiplier = lvl;
                            }
                            config.set("level_multiplier", levelMultiplier);
                        } else
                            sender.sendMessage(String.format("%sPossible values: from 0 to 20.", ChatColor.RED));
                        sender.sendMessage(
                                String.format("%s level multiplier now: %d%n5 by default", ChatColor.GREEN,
                                        levelMultiplier));
                        return true;
                    }
                    case ("progressbarph"): {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
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
                        if (args[1].equals(TRUE) || args[1].equals(FALSE)) {
                            progressBarEnabled = Boolean.parseBoolean(args[1]);
                            if (progressBarEnabled) {
                                if (progressBarColor == null)
                                    progressBarColor = BarColor.GREEN;
                                blockFile();
                            }
                            for (PlayerInfo bb : pInf)
                                if (bb.bar != null)
                                    bb.bar.setVisible(progressBarEnabled);
                            config.set(PROGRESS_BAR, progressBarEnabled);
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(COLOR)) {
                            if (args.length == 2) {
                                sender.sendMessage(String.format("%sEnter a color name.", ChatColor.YELLOW));
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
                        if (args[1].equalsIgnoreCase(LEVEL)) {
                            if (!progressBarEnabled)
                                return true;
                            if (!levelBarModeEnabled) {
                                levelBarModeEnabled = true;
                                for (PlayerInfo curPlayer : pInf)
                                    if (curPlayer.lvl >= levels.size())
                                        curPlayer.bar.setTitle(maxlevel.name);
                                    else
                                        curPlayer.bar.setTitle(levels.get(curPlayer.lvl).name);
                                config.set(PROGRESS_BAR_TEXT, LEVEL);
                            } else {
                                levelBarModeEnabled = false;
                                for (PlayerInfo bb : pInf)
                                    bb.bar.setTitle("Progress bar");
                                config.set(PROGRESS_BAR_TEXT, "Progress bar");
                            }
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(SET_TEXT)) {
                            if (!progressBarEnabled)
                                return true;
                            StringBuilder textBar = new StringBuilder();
                            for (int i = 2; i < args.length - 1; i++)
                                textBar.append(args[i]).append(" ");
                            textBar.append(args[args.length - 1]);
                            levelBarModeEnabled = false;
                            for (PlayerInfo bb : pInf)
                                bb.bar.setTitle(textBar.toString());
                            config.set(PROGRESS_BAR_TEXT, textBar.toString());
                            textP = textBar.toString();
                            if (papiEnabled)
                                for (Player ponl : Bukkit.getOnlinePlayers())
                                    pInf.get(getID(ponl.getName())).bar
                                            .setTitle(PlaceholderAPI.setPlaceholders(ponl, textBar.toString()));
                            return true;
                        }
                        sender.sendMessage(String.format("%s true, false, settext or level only!", ChatColor.RED));
                        return true;
                    }
                    case LISTLVL: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length >= 2) {
                            int temp;
                            try {
                                temp = Integer.parseInt(args[1]);
                            } catch (NumberFormatException nfe) {
                                sender.sendMessage(String.format("%sInvalid value", ChatColor.RED));
                                return true;
                            }
                            if (levels.size() <= temp || temp < 0) {
                                sender.sendMessage(String.format("%sUndefined lvl", ChatColor.RED));
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
                    case RELOAD: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length == 1) {
                            sender.sendMessage(String.format("%sReloading Plugin & Plugin Modules.", ChatColor.YELLOW));
                            blockFile();
                            flowerFile();
                            chestFile();
                            mobFile();
                            recreateWorldGuard();
                            sender.sendMessage(String.format("%sAll .yml reloaded!", ChatColor.GREEN));
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(BLOCKS_YML)) {
                            blockFile();
                            sender.sendMessage(String.format("%sBlocks.yml reloaded!", ChatColor.GREEN));
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(FLOWERS_YML)) {
                            flowerFile();
                            sender.sendMessage(String.format("%sFlowers.yml reloaded!", ChatColor.GREEN));
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(CHESTS_YML)) {
                            chestFile();
                            sender.sendMessage(String.format("%sChests.yml reloaded!", ChatColor.GREEN));
                            return true;
                        }
                        if (args[1].equalsIgnoreCase(MOBS_YML)) {
                            mobFile();
                            sender.sendMessage(String.format("%sMobs.yml reloaded!", ChatColor.GREEN));
                            return true;
                        }
                        sender.sendMessage(String.format("%sTry blocks.yml or chests.yml", ChatColor.RED));
                        return true;
                    }
                    case "chatalert": {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        chatAlertEnabled = !chatAlertEnabled;
                        sender.sendMessage(
                                ChatColor.GREEN + (chatAlertEnabled ? "Alerts are now on!" : "Alerts are now disabled!"));
                        config.set(CHAT_ALERT, chatAlertEnabled);
                        return true;
                    }
                    case FREQUENCY: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length == 1) {
                            sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                            return true;
                        }
                        long frequencytemp;
                        String frequencyName = "";
                        try {
                            frequencytemp = Long.parseLong(args[1]);
                        } catch (Exception e) {
                            sender.sendMessage(ChatColor.YELLOW + "enter a valid value (4 to 20)\n7 by default");
                            return true;
                        }
                        if (frequencytemp >= 4L && frequencytemp <= 20L && on) {
                            frequencyValue = frequencytemp;
                            Bukkit.getScheduler().cancelTasks(this);
                            config.set(FREQUENCY, frequencyValue);
                            if (frequencyValue == 4L)
                                frequencyName = " (Extreme)";
                            else if (frequencyValue < 7L)
                                frequencyName = " (Fast)";
                            else if (frequencyValue == 7L)
                                frequencyName = " (Default)";
                            else if (frequencyValue < 9L)
                                frequencyName = " (Normal)";
                            else if (frequencyValue < 13L)
                                frequencyName = " (Slow)";
                            else if (frequencyValue < 17L)
                                frequencyName = " (Slower)";
                            else
                                frequencyName = " (Max TPS)";
                            Bukkit.getScheduler().runTaskTimer(this, new Task(), frequencyValue, frequencyValue * 2);
                        }
                        sender.sendMessage(ChatColor.GREEN + "Now frequency = " + frequencyValue + frequencyName);
                        return true;
                    }
                    case ISLANDS: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length == 1) {
                            sender.sendMessage(ChatColor.YELLOW + TRUE_OR_FALSE);
                            return true;
                        }
                        if (args[1].equals(TRUE) || args[1].equals(FALSE)) {
                            il3x3 = Boolean.parseBoolean(args[1]);
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
                            config.set(CUSTOM_ISLAND, null);
                            customisland = null;
                            sender.sendMessage(ChatColor.GREEN + "The default customisland is installed.");
                            return true;
                        }
                        sender.sendMessage(ChatColor.YELLOW + TRUE_OR_FALSE);
                        return true;
                    }
                    case ISLAND_REBIRTH: {
                        if (!sender.hasPermission(permissionSet)) {
                            sender.sendMessage(NO_PERMISSION_OBSET);
                            return true;
                        }
                        if (args.length == 1) {
                            sender.sendMessage(ChatColor.YELLOW + TRUE_OR_FALSE);
                            return true;
                        }
                        if (args[1].equals(TRUE) || args[1].equals(FALSE)) {
                            rebirthEnabled = Boolean.parseBoolean(args[1]);
                            config.set("Rebirth_on_the_island", rebirthEnabled);
                            sender.sendMessage(ChatColor.GREEN + "Rebirth_on_the_island = " + rebirthEnabled);
                            return true;
                        }
                        sender.sendMessage(ChatColor.YELLOW + TRUE_OR_FALSE);
                        return true;
                    }
                    case ("help"): {
                        sender.sendMessage(ChatColor.GREEN + "OnlyBlock Plugin Help");
                        boolean admin = sender.hasPermission(permissionSet);
                        if (admin)
                            sender.sendMessage(ChatColor.GRAY + "/ob set" + ChatColor.WHITE
                                    + " - sets the location of the first Island.");
                        sender.sendMessage(
                                ChatColor.GRAY + "/ob j" + ChatColor.WHITE + " - join a new one or your own Island.");
                        if (admin)
                            sender.sendMessage(ChatColor.GRAY + "/ob protection" + ChatColor.WHITE
                                    + " - does not allow players to leave their Island.");
                        sender.sendMessage(ChatColor.GRAY + "/ob invite 'player-name'" + ChatColor.WHITE
                                + " - an invitation to the Island.\n" +
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
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Error.");
            return false;
        }
        return false;
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

    private void recreateWorldGuard() {
        if (worldGuardEnabled && obCanUse) {
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
            File playerData = new File(getDataFolder(), PLAYER_DATA_JSON);
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
        File block = new File(getDataFolder(), BLOCKS_YML);
        if (!block.exists())
            saveResource(BLOCKS_YML, false);
        newConfig = YamlConfiguration.loadConfiguration(block);
        if (newConfig.isString("MaxLevel"))
            maxlevel.name = newConfig.getString("MaxLevel");
        for (int i = 0; newConfig.isList(String.format("%d", i)); i++) {
            List<String> blockTemp = newConfig.getStringList(String.format("%d", i));
            Level level = new Level(blockTemp.get(0));
            levels.add(level);
            int q = 1;
            if (progressBarEnabled && q < blockTemp.size())
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
        if (!superlegacy && progressBarEnabled && !pInf.isEmpty() && pInf.get(0).bar == null) {
            maxlevel.color = progressBarColor;
            for (PlayerInfo curPlayer : pInf) {
                Level lvl = maxlevel;
                if (curPlayer.lvl < levels.size())
                    lvl = levels.get(curPlayer.lvl);
                curPlayer.bar = Bukkit.createBossBar(levelBarModeEnabled ? lvl.name : textP, lvl.color, BarStyle.SEGMENTED_10,
                        BarFlag.DARKEN_SKY);
            }
            Bukkit.getPluginManager().registerEvents(new ChangedWorld(), this);
        }
    }

    private void flowerFile() {
        flowers.clear();
        File flower = new File(getDataFolder(), FLOWERS_YML);
        if (!flower.exists())
            saveResource(FLOWERS_YML, false);
        newConfig = YamlConfiguration.loadConfiguration(flower);
        flowers.add(grass);
        for (String list : newConfig.getStringList("flowers"))
            if (!XMaterial.matchXMaterial(list).isPresent())
                flowers.add(grass);
            else {
                XMaterial temp;
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
        File chest = new File(getDataFolder(), CHESTS_YML);
        if (!chest.exists())
            saveResource(CHESTS_YML, false);
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
        File mob = new File(getDataFolder(), MOBS_YML);
        if (!mob.exists())
            saveResource(MOBS_YML, false);
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
        File playerData = new File(getDataFolder(), PLAYER_DATA_JSON);
        if (playerData.exists())
            synchronized (this) {
                pInf = JsonSimple.Read(playerData);
            }
        if (worldGuardEnabled && obCanUse) {
            recreateWorldGuard();
        }
        id = pInf.size();
    }

    private void configFile() {
        File configFile = new File(getDataFolder(), CONFIG_YML);
        if (!configFile.exists())
            saveResource(CONFIG_YML, false);
        config = this.getConfig();
        world = Bukkit.getWorld(check(DEFAULT_WORLD, DEFAULT_WORLD));
        x = (int) check("x", (double) x);
        y = (int) check("y", (double) y);
        z = (int) check("z", (double) z);
        // leave - leaf
        if (config.isString(LEAFWORLD)) {
            config.set(LEAVE_WORLD, config.getString(LEAFWORLD));
            config.set(LEAFWORLD, null);
        }
        if (config.isSet(XLEAF)) {
            config.set(X_LEAVE, config.getDouble(XLEAF));
            config.set(XLEAF, null);
        }
        if (config.isSet(YLEAF)) {
            config.set(Y_LEAVE, config.getDouble(YLEAF));
            config.set(YLEAF, null);
        }
        if (config.isSet(ZLEAF)) {
            config.set(Z_LEAVE, config.getDouble(ZLEAF));
            config.set(ZLEAF, null);
        }
        leaveworld = Bukkit.getWorld(check(LEAVE_WORLD, DEFAULT_WORLD));
        check(X_LEAVE, 0.0);
        check(Y_LEAVE, 0.0);
        check(Z_LEAVE, 0.0);
        progressBarEnabled = check(PROGRESS_BAR, true);
        if (superlegacy)
            progressBarEnabled = false;
        if (!config.isInt(FREQUENCY))
            config.set(FREQUENCY, 7L);
        frequencyValue = config.getLong(FREQUENCY);
        // Text
        if (!superlegacy) {
            textP = check(PROGRESS_BAR_TEXT, LEVEL);
            if (textP.equals(LEVEL))
                levelBarModeEnabled = true;
        }
        // alert
        chatAlertEnabled = check(CHAT_ALERT, !levelBarModeEnabled);
        if (progressBarEnabled)
            progressBarColor = BarColor.valueOf(check("progressBar_color", "GREEN"));
        il3x3 = check("Island_for_new_players", true);
        rebirthEnabled = check("Rebirth_on_the_island", true);
        synchronized (this) {
            levelMultiplier = check("level_multiplier", levelMultiplier);
        }
        protectionEnabled = check(PROTECTION, protectionEnabled);
        if (worldGuardEnabled && obCanUse) {
            worldGuardEnabled = check(WORLD_GUARD, true);
        }
        autojoinEnabled = check(AUTOJOIN, autojoinEnabled);
        if (config.isSet(CUSTOM_ISLAND) && !legacy) {
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
        Config.Save(config, configFile);
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

    public static PlayerInfo gettop(int i) {
        if (pInf.size() <= i)
            return new PlayerInfo();
        ArrayList<PlayerInfo> ppii = (ArrayList<PlayerInfo>) pInf.clone();
        ppii.sort(PlayerInfo.COMPARE_BY_LVL);
        return ppii.get(i);
    }

    //TODO: Fix error when invoking "/ob" from console
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            commands.addAll(Arrays.asList("j", "join", "leave", INVITE, "accept", "kick", "ver", "IDreset", "help"));
            if (sender.hasPermission(permissionSet)) {
                commands.addAll(Arrays.asList("set", "setleave", PROGRESS_BAR, CHAT_ALERT, SETLEVEL, CLEAR,
                        "levelMultiplier", RELOAD, FREQUENCY, ISLANDS, ISLAND_REBIRTH, PROTECTION, WORLD_GUARD,
                        LISTLVL, "autoJoin"));
            }
        } else if (args.length == 2) {
            if (args[0].equals(INVITE) || args[0].equals("kick")) {
                for (Player ponl : online)
                    commands.add(ponl.getName());
            } else if (sender.hasPermission(permissionSet)) {
                switch (args[0]) {
                    case CLEAR:
                    case SETLEVEL: {
                        for (Player ponl : online)
                            commands.add(ponl.getName());
                        break;
                    }
                    case ("progressbarph"): {
                        commands.add(TRUE);
                        commands.add(FALSE);
                        commands.add(LEVEL);
                        commands.add(SET_TEXT);
                        commands.add(COLOR);
                        break;
                    }
                    case RELOAD: {
                        commands.add(BLOCKS_YML);
                        commands.add(CHESTS_YML);
                        commands.add(MOBS_YML);
                        commands.add(FLOWERS_YML);
                        break;
                    }
                    case ISLANDS:
                        commands.add("set_my_by_def");
                        commands.add("default");
                        break;
                    case ISLAND_REBIRTH:
                    case PROTECTION:
                    case ("worldguard"):
                    case ("autoJoin"):
                        commands.add(TRUE);
                        commands.add(FALSE);
                        break;
                    case LISTLVL:
                        for (int i = 0; i < levels.size(); i++)
                            commands.add(String.format("%d", i));
                        break;
                    case FREQUENCY:
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
            if (args[0].equals(PROGRESS_BAR)) {
                if (args[1].equals(COLOR))
                    for (BarColor bc : BarColor.values())
                        commands.add(bc.name());
                if (args[1].equals(SET_TEXT)) {
                    commands.add("...");
                    if (papiEnabled)
                        commands.add("%OB_lvl_name%. There are %OB_need_to_lvl_up% block(s) left.");
                }
            } else if (args[0].equals(SETLEVEL)) {
                for (int i = 0; i < levels.size(); i++)
                    commands.add(String.format("%d", i));
            }
        }
        Collections.sort(commands);
        return commands;
    }
}