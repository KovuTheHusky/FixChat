package com.kovuthehusky.fixchat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapCommonAPI;
import org.kitteh.vanish.VanishCheck;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

import com.google.common.base.Joiner;

public class FixChat extends JavaPlugin implements Listener {
    public enum Advancements {
        MINECRAFT_STORY_ROOT("Minecraft"),
        MINECRAFT_STORY_MINE_STONE("Stone Age"),
        MINECRAFT_STORY_UPGRADE_TOOLS("Getting an Upgrade"),
        MINECRAFT_STORY_SMELT_IRON("Acquire Hardware"),
        MINECRAFT_STORY_OBTAIN_ARMOR("Suit Up"),
        MINECRAFT_STORY_LAVA_BUCKET("Hot Stuff"),
        MINECRAFT_STORY_IRON_TOOLS("Isn't It Iron Pick"),
        MINECRAFT_STORY_DEFLECT_ARROW("Not Today, Thank You"),
        MINECRAFT_STORY_FORM_OBSIDIAN("Ice Bucket Challenge"),
        MINECRAFT_STORY_MINE_DIAMOND("Diamonds!"),
        MINECRAFT_STORY_ENTER_THE_NETHER("We Need to Go Deeper"),
        MINECRAFT_STORY_SHINY_GEAR("Cover Me With Diamonds"),
        MINECRAFT_STORY_ENCHANT_ITEM("Enchanter"),
        MINECRAFT_STORY_CURE_ZOMBIE_VILLAGER("Zombie Doctor"),
        MINECRAFT_STORY_FOLLOW_ENDER_EYE("Eye Spy"),
        MINECRAFT_STORY_ENTER_THE_END("The End?"),
        MINECRAFT_NETHER_ROOT("Nether"),
        MINECRAFT_NETHER_FAST_TRAVEL("Subspace Bubble"),
        MINECRAFT_NETHER_FIND_FORTRESS("A Terrible Fortress"),
        MINECRAFT_NETHER_RETURN_TO_SENDER("Return to Sender"),
        MINECRAFT_NETHER_OBTAIN_BLAZE_ROD("Into Fire"),
        MINECRAFT_NETHER_GET_WITHER_SKULL("Spooky Scary Skeleton"),
        MINECRAFT_NETHER_UNEASY_ALLIANCE("Uneasy Alliance"),
        MINECRAFT_NETHER_BREW_POTION("Local Brewery"),
        MINECRAFT_NETHER_SUMMON_WITHER("Withering Heights"),
        MINECRAFT_NETHER_ALL_POTIONS("A Furious Cocktail"),
        MINECRAFT_NETHER_CREATE_BEACON("Bring Home the Beacon"),
        MINECRAFT_NETHER_ALL_EFFECTS("How Did We Get Here?"),
        MINECRAFT_NETHER_CREATE_FULL_BEACON("Beaconator"),
        MINECRAFT_END_ROOT("The End"),
        MINECRAFT_END_KILL_DRAGON("Free the End"),
        MINECRAFT_END_DRAGON_EGG("The Next Generation"),
        MINECRAFT_END_ENTER_END_GATEWAY("Remote Getaway"),
        MINECRAFT_END_RESPAWN_DRAGON("The End... Again..."),
        MINECRAFT_END_DRAGON_BREATH("You Need a Mint"),
        MINECRAFT_END_FIND_END_CITY("The City at the End of the Game"),
        MINECRAFT_END_ELYTRA("Sky's the Limit"),
        MINECRAFT_END_LEVITATE("Great View From Up Here"),
        MINECRAFT_ADVENTURE_ROOT("Adventure"),
        MINECRAFT_ADVENTURE_KILL_A_MOB("Monster Hunter"),
        MINECRAFT_ADVENTURE_TRADE("What a Deal!"),
        MINECRAFT_ADVENTURE_SLEEP_IN_BED("Sweet dreams"),
        MINECRAFT_ADVENTURE_SHOOT_ARROW("Take Aim"),
        MINECRAFT_ADVENTURE_KILL_ALL_MOBS("Monsters Hunted"),
        MINECRAFT_ADVENTURE_TOTEM_OF_UNDYING("Postmortal"),
        MINECRAFT_ADVENTURE_SUMMON_IRON_GOLEM("Hired Help"),
        MINECRAFT_ADVENTURE_ADVENTURING_TIME("Adventuring Time"),
        MINECRAFT_ADVENTURE_SNIPER_DUEL("Sniper duel"),
        MINECRAFT_HUSBANDRY_ROOT("Husbandry"),
        MINECRAFT_HUSBANDRY_BREED_AN_ANIMAL("The Parrots and the Bats"),
        MINECRAFT_HUSBANDRY_TAME_AN_ANIMAL("Best Friends Forever"),
        MINECRAFT_HUSBANDRY_PLANT_SEED("A Seedy Place"),
        MINECRAFT_HUSBANDRY_BRED_ALL_ANIMALS("Two by Two"),
        MINECRAFT_HUSBANDRY_BALANCED_DIET("A Balanced Diet"),
        MINECRAFT_HUSBANDRY_BREAK_DIAMOND_HOE("Serious Dedication");



        private final String name;

        Advancements(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Strings {
        AWAY(" is away from keyboard"),
        MOTD("Message of the day:"),
        MOTD_UPDATED("The message of the day has been updated."),
        NO_WHISPER_REPLY("There's no whisper to reply to."),
        NON_PLAYER_REPLY("Non-players cannot respond because they cannot receive whispers."),
        NOT_AWAY(" is no longer away from keyboard");

        private final String name;

        Strings(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static int _MPS = 1000;
    public static int _TPS = 20;
    public static int AWAY = 300 * _MPS;
    public static int INTERVAL = 5 * _TPS;

    private final ArrayList<Player> away = new ArrayList<>();
    private FileConfiguration configuration;
    private final HashMap<Player, Long> idle = new HashMap<>();
    private final HashMap<Player, Long> knockback = new HashMap<>();
    private final HashMap<Player, Player> reply = new HashMap<>();
    private final String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().substring(23);
    private Server server;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player from = null;
        if (Player.class.isInstance(sender))
            from = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("t") || cmd.getName().equalsIgnoreCase("whisper") || cmd.getName().equalsIgnoreCase("w"))
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getName() + " <player> <message>");
                return true;
            } else {
                Player to = null;
                for (Player p : server.getOnlinePlayers())
                    if (p.getName().equalsIgnoreCase(args[0]))
                        to = p;
                if (from != null && to != null)
                    reply.put(to, from);
                server.dispatchCommand(from != null ? from : sender, "tell " + Joiner.on(' ').join(args));
                return true;
            }
        else if (cmd.getName().equalsIgnoreCase("reply") || cmd.getName().equalsIgnoreCase("r"))
            if (from == null) {
                sender.sendMessage(Strings.NON_PLAYER_REPLY.toString());
                return true;
            } else if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getName() + " <message>");
                return true;
            } else if (reply.get(from) == null) {
                sender.sendMessage(Strings.NO_WHISPER_REPLY.toString());
                return true;
            } else {
                reply.put(reply.get(from), from);
                server.dispatchCommand(from, "tell " + reply.get(from).getName() + " " + Joiner.on(' ').join(args));
                return true;
            }
        else if (cmd.getName().equalsIgnoreCase("motd"))
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getName() + " <message> OR /" + cmd.getName() + " clear");
                return true;
            } else {
                String motd = Joiner.on(' ').join(args);
                if (motd.equalsIgnoreCase("clear"))
                    motd = "";
                configuration.set("motd", motd);
                this.saveConfig();
                sender.sendMessage(Strings.MOTD_UPDATED + "");
                return true;
            }
        else if (cmd.getName().equalsIgnoreCase("ping")) {
            if (!(sender instanceof Player))
                sender.sendMessage("Non-players cannot ask for their ping to the server.");
            else
                try {
                    Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".entity.CraftPlayer");
                    Object handle = craftPlayer.getMethod("getHandle").invoke(sender);
                    int ping = (int) handle.getClass().getDeclaredField("ping").get(handle);
                    ChatColor color;
                    if (ping < 0) {
                        color = ChatColor.DARK_RED;
                    } else if (ping < 150) {
                        color = ChatColor.GREEN;
                    } else if (ping < 300) {
                        color = ChatColor.YELLOW;
                    } else if (ping < 600) {
                        color = ChatColor.GOLD;
                    } else if (ping < 1000) {
                        color = ChatColor.RED;
                    } else {
                        color = ChatColor.DARK_RED;
                    }
                    sender.sendMessage(ChatColor.RESET + "Your ping is " + color + ping + "ms" + ChatColor.RESET + ".");
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
                    sender.sendMessage(ChatColor.RESET + "Your ping cannot be determined.");
                }
            return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        new Metrics(this);
        this.saveDefaultConfig();
        configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        this.saveConfig();
        server = this.getServer();
        server.getPluginManager().registerEvents(this, this);
        if (configuration.getBoolean("afk"))
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : server.getOnlinePlayers())
                        if (idle.get(p) != null && !away.contains(p))
                            if (System.currentTimeMillis() - idle.get(p) > AWAY) {
                                if (Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket")) {
                                    VanishManager vanish = ((VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager();
                                    if (vanish != null)
                                        if ((Boolean) new VanishCheck(vanish, p.getName()).call())
                                            return;
                                }
                                away.add(p);
                                Bukkit.broadcastMessage(ChatColor.YELLOW + p.getName() + Strings.AWAY);
                                if (Bukkit.getPluginManager().isPluginEnabled("dynmap"))
                                    ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, p.getName() + " is away from keyboard.");
                            }
                }
            }.runTaskTimer(this, INTERVAL, INTERVAL);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.PLAYER)
            knockback.put((Player) event.getEntity(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        String adv = event.getAdvancement().getKey().getNamespace().toUpperCase() + "_" + event.getAdvancement().getKey().getKey().replace('/', '_').toUpperCase();
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("dynmap"))
                ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, event.getPlayer().getName() + " has made the advancement [" + Advancements.valueOf(adv) + "]");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        idle.put(event.getPlayer(), System.currentTimeMillis());
        if (away.contains(event.getPlayer())) {
            away.remove(event.getPlayer());
            Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayer().getName() + Strings.NOT_AWAY);
            if (Bukkit.getPluginManager().isPluginEnabled("dynmap"))
                ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, event.getPlayer().getName() + " is no longer away from keyboard.");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] msg = event.getMessage().split("\\s+");
        if (msg.length > 2 && msg[0].equalsIgnoreCase("/tell")) {
            Player to = null;
            for (Player p : server.getOnlinePlayers())
                if (p.getName().equalsIgnoreCase(msg[1]))
                    to = p;
            if (to != null)
                reply.put(to, event.getPlayer());
        } else if (Bukkit.getPluginManager().isPluginEnabled("dynmap") && msg.length > 1 && msg[0].equalsIgnoreCase("/say") && event.getPlayer().hasPermission("minecraft.command.say"))
            ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(event.getPlayer().getName(), Joiner.on(' ').join(Arrays.copyOfRange(msg, 1, msg.length)));
        else if (Bukkit.getPluginManager().isPluginEnabled("dynmap") && msg.length > 1 && msg[0].equalsIgnoreCase("/me") && event.getPlayer().hasPermission("minecraft.command.me"))
            ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, "* " + event.getPlayer().getName() + " " + Joiner.on(' ').join(Arrays.copyOfRange(msg, 1, msg.length)));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap"))
            ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, event.getDeathMessage());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        idle.put(event.getPlayer(), System.currentTimeMillis());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().hasPermission("minecraft.command.list"))
                    server.dispatchCommand(event.getPlayer(), "list");
                if (configuration.getString("motd") != null && configuration.getString("motd").length() > 0) {
                    event.getPlayer().sendMessage(Strings.MOTD + "");
                    event.getPlayer().sendMessage(configuration.getString("motd"));
                }
            }
        }.runTaskLater(this, 1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        idle.put(event.getPlayer(), System.currentTimeMillis());
        if (away.contains(event.getPlayer()) && (knockback.get(event.getPlayer()) == null || knockback.get(event.getPlayer()) < System.currentTimeMillis() - 3000)) {
            away.remove(event.getPlayer());
            Bukkit.broadcastMessage(ChatColor.YELLOW + event.getPlayer().getName() + Strings.NOT_AWAY);
            if (Bukkit.getPluginManager().isPluginEnabled("dynmap"))
                ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb(null, event.getPlayer().getName() + " is no longer away from keyboard.");
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        idle.remove(event.getPlayer());
        away.remove(event.getPlayer());
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String[] msg = event.getCommand().split("\\s+");
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap") && msg.length > 1 && msg[0].equalsIgnoreCase("say"))
            ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).sendBroadcastToWeb("Server", Joiner.on(' ').join(Arrays.copyOfRange(msg, 1, msg.length)));
    }
}
