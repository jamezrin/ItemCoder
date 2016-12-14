package me.jaimemartz.itemcoder;

import com.google.common.base.Joiner;
import me.jaimemartz.faucet.Messager;
import me.jaimemartz.itemcoder.core.ItemCoder;
import me.jaimemartz.itemcoder.core.ItemDecoder;
import me.jaimemartz.itemcoder.core.ItemExporter;
import me.jaimemartz.itemcoder.core.ItemPaster;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static com.squareup.javapoet.MethodSpec.Builder;

public class Main extends JavaPlugin {
    /*
    TODO Add support for BlockStateMeta
    TODO Add support for Repairable
    TODO Add specific subcommands to items
     */

    private Map<UUID, Builder> storage = new HashMap<>();
    private File snippets = null;
    private ClassLoader loader = null;

    @Override
    public void onEnable() {
        snippets = new File(getDataFolder(), "snippets");
        if(!snippets.exists()) {
            snippets.mkdirs();
            saveDefaultConfig();
        }

        File compile = new File(snippets, "compile");
        if(!compile.exists()) {
            compile.mkdirs();
        }

        URL url = null;
        try {
            url = compile.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL[] urls = new URL[] {url};
        loader = new URLClassLoader(urls);

        ItemPaster.setDeveloperKey(getConfig().getString("pastebin_api_key"));
        //Prefix for the plugin is "&7[&5Item&6Coder&7]&r"
    }

    @Override
    public void onDisable() {
        storage.clear();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("itemcoder")) {
            Messager msgr = new Messager(sender);
            if (args.length == 0) {
                msgr.send(
                        "&e=====================================================",
                        "&7Commands for ItemCoder:",
                        "&3/.. code &7- &cTurns an item to code",
                        "&3/.. decode <name> &7- &cTurns code to an item [NIY]",
                        "&3/.. export <name> &7- &cExport the code to a file",
                        "&3/.. paste <name> &7- &cPaste the code at pastebin",
                        "&3/.. reload &7- &cReload config",
                        "&3/.. clear &7- &cClear clipboard of code",
                        "&3/.. purge &7- &cDelete the generated files",
                        "&7Tools for ItemCoder:",
                        "&3/.. name <name> &7- &cSet the item of the item",
                        "&3/.. amount <amount> &7- &cSet the amount of the item",
                        "&3/.. durability <amount> &7- &cSet the durability of the item",
                        "&3/.. lore [line] <text> &7- &cSet the lore of the item",
                        "&e=====================================================");
            } else {
                switch (args[0].toLowerCase()) {
                    case "code": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                msgr.send("&aGenerating the code for the item in your hand...");
                                if(storage.containsKey(player.getUniqueId())) {
                                    storage.remove(player.getUniqueId());
                                    msgr.send("&aDeleted the previously coded item");
                                }
                                storage.put(player.getUniqueId(), ItemCoder.code(item, this));
                                msgr.send("&aThe code for the item has been generated, run &6/itemcoder export &ato save it");
                            } else msgr.send("&cYou have to have a item in your hand.");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "decode": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(args.length == 2) {
                                String name = args[1];
                                File file = new File(getSnippetsFolder(), name + ".java");
                                if(file.exists()) {
                                    msgr.send("&aDecoding the code, this may take a while...");
                                    ItemStack item = ItemDecoder.decode(name, file, this);
                                    if(item != null) {
                                        player.getInventory().addItem(item);
                                        msgr.send("&aSucessfully decoded the file &6{name} &a, you should have it in your inventory".replace("{name}", name));
                                    } else msgr.send("&cAn error occurred when decoding the item");
                                } else msgr.send("&cThere is no file named {name} in the snippets folder".replace("{name}", name));
                            } else msgr.send("&cYou have to provide a name for the file");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "export": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(storage.containsKey(player.getUniqueId())) {
                                if(args.length == 2) {
                                    String name = args[1];
                                    try {
                                        ItemExporter.exportCode(storage.remove(player.getUniqueId()), name, this);
                                    } catch (IOException e) {
                                        msgr.send("&cAn error occurred when trying to export the code");
                                        e.printStackTrace();
                                    }
                                    msgr.send("&aItem successfully exported as &6{name}".replace("{name}", name));
                                } else msgr.send("&cYou have to provide a name for the file");
                            } else msgr.send("&cYou have to code an item first!");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "paste": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(storage.containsKey(player.getUniqueId())) {
                                if(args.length == 2) {
                                    String name = args[1];
                                    try {
                                        String link = ItemPaster.pasteCode(storage.remove(player.getUniqueId()), name);
                                        msgr.send("&aSuccessfully pasted the code in pastebin");
                                        msgr.send("&aLink: &6{link}".replace("{link}", link));
                                    } catch (Exception e) {
                                        msgr.send("&cCould not paste your item");
                                        e.printStackTrace();
                                    }
                                } else msgr.send("&cYou have to provide a name for the file");
                            } else msgr.send("&cYou have to code an item first!");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "reload": {
                        reloadConfig();
                        ItemPaster.setDeveloperKey(getConfig().getString("pastebin_api_key"));
                        msgr.send("&aThe configuration has been reloaded");
                        break;
                    }

                    case "clear": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(storage.containsKey(player.getUniqueId())) {
                                storage.remove(player.getUniqueId());
                                msgr.send("&aCleared your code clipboard");
                            } else msgr.send("&cYou have not coded a item before!");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "purge": {
                        List<File> files = Arrays.asList(getSnippetsFolder().listFiles());
                        if(!files.isEmpty()) {
                            for(File file : files) {
                                file.delete();
                            }
                            msgr.send(String.format("&aSuccessfully deleted &7%s&a code snippets", files.size()));
                        } else msgr.send("&cThere are no code snippets to delete");
                        break;
                    }

                    case "name": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                if(args.length > 1) {
                                    String name = Messager.colorize(Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length)));
                                    ItemMeta meta = ((item.hasItemMeta()) ? item.getItemMeta() : getServer().getItemFactory().getItemMeta(item.getType()));
                                    meta.setDisplayName(name);
                                    item.setItemMeta(meta);
                                    msgr.send("&aSet the name to &7\"&f&o{name}&7\"".replace("{name}", name));
                                } else msgr.send("&cYou have to provide an name");
                            } else msgr.send("&cYou need to have a item in your hand");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "amount": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                if(args.length == 2) {
                                    try {
                                        int amount = Integer.valueOf(args[1]);
                                        item.setAmount(amount);
                                        msgr.send("&aSet the amount to &6\"&7{amount}&6\"".replace("{amount}", String.valueOf(amount)));
                                    } catch (Exception e) {
                                        msgr.send("&cThe amount has to be an integer");
                                    }
                                } else msgr.send("&cYou have to provide an integer");
                            } else msgr.send("&cYou need to have a item in your hand");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "durability": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                if(args.length == 2) {
                                    try {
                                        short durability = Short.valueOf(args[1]);
                                        item.setDurability(durability);
                                        msgr.send("&aSet the durability to &6\"&7{durability}&6\"".replace("{durability}", String.valueOf(durability)));
                                    } catch (Exception e) {
                                        msgr.send("&cThe durability has to be an short");
                                    }
                                } else msgr.send("&cYou have to provide an short");
                            } else msgr.send("&cYou need to have a item in your hand");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }

                    case "lore": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                ItemMeta meta = ((item.hasItemMeta()) ? item.getItemMeta() : getServer().getItemFactory().getItemMeta(item.getType()));
                                List<String> lore = ((meta.hasLore()) ? meta.getLore() : new ArrayList<String>());

                                if(args.length > 1) {
                                    String arg1 = args[1];
                                    try {
                                        int line = Integer.valueOf(arg1);
                                        try {
                                            String text = Messager.colorize(Joiner.on(" ").join(Arrays.copyOfRange(args, 2, args.length)));
                                            lore.set(line, text);
                                            meta.setLore(lore);
                                            item.setItemMeta(meta);
                                            msgr.send("&aSet the line &6{line} &ato &6\"&7{text}&6\"".replace("{line}", String.valueOf(line)).replace("{text}", text));
                                        } catch (IndexOutOfBoundsException e) {
                                            msgr.send("&cThe lore does not have that line");
                                        }
                                    } catch (NumberFormatException e) {
                                        String text = Messager.colorize(Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length)));
                                        lore.add(text);
                                        meta.setLore(lore);
                                        item.setItemMeta(meta);
                                        msgr.send("&aAdded the line &6\"&7{text}&6\" &ato the lore".replace("{text}", text));
                                        break;
                                    }
                                } else msgr.send("&cYou have to provide an line of text");
                            } else msgr.send("&cYou need to have a item in your hand");
                        } else msgr.send("&cThis command can only be executed by a player");
                        break;
                    }
                    default: msgr.send(String.format("&cYou have used a bad argument, run &7/%s &cfor more info", cmd.getName()));
                }
            }
            return true;
        }
        return false;
    }

    public File getSnippetsFolder() {
        return snippets;
    }

    public ClassLoader getSnippetsLoader() {
        return loader;
    }
}