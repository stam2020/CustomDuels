package plainplaying.plugin.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


public class DuelGUI implements Listener {
    private Player player;
    private Player opponent;
    private final Inventory gui;
    public DuelGUI(Player player, Player opponent){
        this.player = player;
        this.opponent = opponent;
        gui = Bukkit.createInventory(null,54, ChatColor.translateAlternateColorCodes('&',"&cDuel Settings"));
        setupGUI();
        player.openInventory(gui);
    }
    private void setupGUI(){
        gui.setItem(1,createGuiItem(Material.GRAY_DYE, "&bSame Kit: &cDisabled"));
        gui.setItem(4,createGuiItem(Material.DIAMOND_SWORD, "&bSend to "+opponent.getName()));
        gui.setItem(7,createGuiItem(Material.CLOCK, "&aTime: 5m"));
        if (CustomDuels.config.getConfigurationSection("maps").getKeys(false).size() >= 16){
            CustomDuels.logger.warning("Too many maps, max 16");
        }
        if (CustomDuels.config.getConfigurationSection("kits").getKeys(false).size() >= 16){
            CustomDuels.logger.warning("Too many kits, max 16");
        }
        int mapNumber = 0;
        for (String mapID : CustomDuels.config.getConfigurationSection("maps").getKeys(false)){
            ConfigurationSection map = CustomDuels.config.getConfigurationSection("maps."+mapID);
            gui.setItem(18+(mapNumber%4)+9*(mapNumber/4),createGuiItem(Material.matchMaterial(map.getString("rep_item")),map.getString("name")));
            mapNumber++;
        }
        int kitNumber = 0;
        for (String kitID : CustomDuels.config.getConfigurationSection("kits").getKeys(false)){
            ConfigurationSection kit = CustomDuels.config.getConfigurationSection("kits."+kitID);
            gui.setItem(23+(kitNumber%4)+9*(kitNumber/4),createGuiItem(Material.matchMaterial(kit.getString("rep_item")),kit.getString("name")));
            kitNumber++;
        }
        for (int i = 9; i <= 17;i++) {
            gui.setItem(i,createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }
        gui.setItem(3,createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        gui.setItem(5,createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        for (int i = 13; i <= 49;i+=9){
            gui.setItem(i,createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }
    }
    //Stole this function from an online tutorial
    static ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}