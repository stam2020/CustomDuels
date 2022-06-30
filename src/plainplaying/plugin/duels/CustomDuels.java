package plainplaying.plugin.duels;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class CustomDuels extends JavaPlugin implements Listener {
    private final File configFile = new File(getDataFolder(),"config.yml");
    private Connection conn;
    private Statement stmtExecutor;
    private static Economy eco;
    public static Logger logger;
    public static FileConfiguration config;
    private static CustomDuels instance;
    public static File maps;
    public static HashMap<Player,Fight> duels;
    @Override
    public void onLoad() {
        getLogger().info("Plugin loaded successfully");
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("duel").setExecutor(new CommandHandler(this,eco));
        getCommand("CustomDuels").setExecutor(new CommandHandler(this,eco));
        getCommand("CustomDuels").setTabCompleter(new CommandHandler(this,eco));
//        try {
//            Class.forName("org.sqlite.JDBC");
//            conn = DriverManager.getConnection("jdbc:sqlite:info.db");
//            stmtExecutor = conn.createStatement();
//        } catch ( Exception e ) {
//            getLogger().severe(e.getStackTrace().toString());
//            conn = null;
//        }
//        String generateGens = "CREATE TABLE IF NOT EXISTS maps(" +
//                "player1 TEXT," +
//                "player1 TEXT," +
//                "worldid TEXT" +
//                ");";
//        try {
//            stmtExecutor.executeUpdate(generateGens);
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
        //mysqlSetup();
        //db = new SQLite(this);
       // db.load();
        getServer().getPluginManager().registerEvents(this,this);
        onEnableRun();
    }

    @Override
    public void onDisable() {
        getLogger().info("Custom duels is down.");
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block placedBlock = e.getBlock();
        Player player = e.getPlayer();
        ItemMeta heldItem = player.getInventory().getItemInMainHand().getItemMeta();
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block brokenBlock = e.getBlock();
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e){
        CustomDuels.logger.info(e.getView().getTitle());
        if (!e.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',"&cDuel Settings"))) return;
        e.setCancelled(true);
        Player player = (Player)e.getWhoClicked();
        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        if (duels.containsKey(player)) {
            Fight relevantFight = duels.get(player);
            switch(e.getRawSlot()){
                case 1 -> {
                    relevantFight.sameKit = !relevantFight.sameKit;
                    if (relevantFight.sameKit){
                        e.getInventory().setItem(1, DuelGUI.createGuiItem(Material.GRAY_DYE, "&bSame Kit: &cDisabled"));
                    }else{
                        e.getInventory().setItem(1, DuelGUI.createGuiItem(Material.LIME_DYE, "&bSame Kit: &aEnabled"));
                    }
                }
                case 4 -> player.closeInventory();
                case 7 -> {
                    switch (relevantFight.time){
                        case 3 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 5m"));
                            relevantFight.time = 5;
                        }
                        case 5 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 7m"));
                            relevantFight.time = 7;
                        }
                        case 7 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 10m"));
                            relevantFight.time = 10;
                        }
                        case 10 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 13m"));
                            relevantFight.time = 13;
                        }
                        case 13 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 15m"));
                            relevantFight.time = 15;
                        }
                        case 15 -> {
                            e.getInventory().setItem(7, DuelGUI.createGuiItem(Material.CLOCK, "&aTime: 3m"));
                            relevantFight.time = 3;
                        }

                    }
                }
            }
        }
    }
    public static CustomDuels getInstance(){
        return instance;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }
    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
    public String parseLocation(Location loc){
        return loc.getWorld().getName()+" "+loc.getX()+" "+loc.getY()+" "+loc.getZ();
    }
    public Location unparseLocation(String loc){
        String[] locationData = loc.split(" ");
        return new Location(getServer().getWorld(locationData[0]),Double.parseDouble(locationData[1]),Double.parseDouble(locationData[2]),Double.parseDouble(locationData[3]));
    }
    public void giveItemsNaturally(Player player, ItemStack items, Location dropLocation){
        Inventory playerInv = player.getInventory();
        int firstAvailable = playerInv.firstEmpty();
        for (ItemStack item : playerInv) {
            if (item != null) {
                if (item.isSimilar(items) && item.getItemMeta().getDisplayName().equals(items.getItemMeta().getDisplayName()) && item.getItemMeta().getLore().equals(items.getItemMeta().getLore())) {
                    int newAmount = item.getAmount() + items.getAmount();
                    if (newAmount <= 64) {
                        item.setAmount(newAmount);
                        return;
                    } else {
                        item.setAmount(64);
                        items.setAmount(newAmount - 64);

                    }
                }
            } else {
                if (firstAvailable != -1) {
                    playerInv.setItem(firstAvailable, items);
                }else{
                    player.getWorld().dropItemNaturally(dropLocation, items);
                }
                return;
            }
        }
        player.getWorld().dropItemNaturally(dropLocation, items);
    }
    public void executeMessage(ConfigurationSection message, Player player, HashMap<String,String> env){
        switch (message.getString("type")){
            case "message" -> player.sendMessage(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("message"),env)));
            case "title" ->  player.sendTitle(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("title"),env)),ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("subtitle"),env)),message.getInt("fade_in"),message.getInt("stay"),message.getInt("fade_out"));
            case "actionbar" -> {player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.translateAlternateColorCodes('&',translateEnv(message.getString("contents"),env))));}
            case "none" -> {}
        }
    }
    private String translateEnv(String message, HashMap<String,String> env){
        if (env != null) {
            for (String key : env.keySet()) {
                message = message.replace("%" + key + "%", env.get(key));
            }
        }
        return message;
    }
    public static List<String> parseByQuotes(String[] strings){
        StringBuilder currentArgument = new StringBuilder();
        List<String> quotedArguments = new ArrayList<>();
        boolean inQuotes = false;
        for (String s : strings){
            if (s.charAt(0) == '\"' && s.charAt(s.length()-1) == '\"'){
                quotedArguments.add(s.substring(1,s.length()-1));
            } else if (s.charAt(0) == '\"'){
                currentArgument.append(s.substring(1)).append(" ");
                inQuotes = true;
            }else if (s.charAt(s.length()-1) == '\"'){
                quotedArguments.add(currentArgument.append(s, 0, s.length()-1).toString());
                currentArgument.setLength(0);
                inQuotes = false;
            }else{
                if (inQuotes) {
                    currentArgument.append(s).append(" ");
                }else{
                    quotedArguments.add(s);
                }
            }
        }
        return quotedArguments;
    }
    public void onEnableRun(){
        getLogger().info("Simple generators is setup!");
        if (!configFile.exists()){
            saveDefaultConfig();
        }
        this.reloadConfig();
        logger = getLogger();
        config = getConfig();
        File temp = new File(getDataFolder() + "/maps");
        if(!temp.exists()){
            temp.mkdirs();
        }
        maps = temp;
    }
}