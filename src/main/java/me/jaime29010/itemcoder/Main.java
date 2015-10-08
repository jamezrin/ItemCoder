package me.jaime29010.itemcoder;

import static com.squareup.javapoet.MethodSpec.Builder;

import com.google.common.base.Joiner;
import me.jaime29010.itemcoder.core.FilePaster;
import me.jaime29010.itemcoder.core.ItemCoder;
import me.jaime29010.itemcoder.core.ItemExporter;
import me.jaime29010.itemcoder.Messager.Replacer;
import me.jaime29010.itemcoder.core.ItemPaster;
import me.nrubin29.pastebinapi.PastebinException;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin {
    private Map<UUID, Builder> storage = new HashMap<>();
    private File snippets = null;
    @Override
    public void onEnable() {
        snippets = new File(getDataFolder(), "snippets");
        if(!snippets.exists()) {
            snippets.mkdirs();
            saveDefaultConfig();
        }
        FilePaster.setAPIKey(getConfig().getString("pastebin_api_key"));
        Prefixer.setPrefix("&7[&5Item&6Coder&7]&r");
    }

    @Override
    public void onDisable() {
        storage.clear();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("itemcoder")) {
            if (args.length == 0) {
                Messager.send(sender,
                        "&e=====================================================",
                        "&7Commands for ItemCoder:",
                        "&3/.. code &7- &cTurns an item to code",
                        "&3/.. decode &7- &cTurns code to an item",
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
                                Messager.send(sender, "&aGenerating the code for the item in your hand...");
                                if(storage.containsKey(player.getUniqueId())) {
                                    storage.remove(player.getUniqueId());
                                    Messager.send(sender, "&aDeleted the previously coded item");
                                }
                                storage.put(player.getUniqueId(), ItemCoder.code(item, this));
                                Messager.send(sender, "&aThe code for the item has been generated, run &6/itemcoder export &ato save it");
                            } else Messager.send(sender, "&cYou have to have a item in your hand.");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
                        break;
                    }

                    case "decode": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            Messager.send(sender, "&cThis feature is not implemented yet!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
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
                                        Messager.send(sender, "&cAn error occurred when trying to export the code");
                                        e.printStackTrace();
                                    }
                                    Messager.sendr(sender, "&aItem successfully exported as &6{name}", Replacer.create("{name}", name));
                                } else Messager.send(sender, "&cYou have to provide a name for the file");
                            } else Messager.send(sender, "&cYou have to code an item first!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
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
                                        Messager.send(sender, "&aSucessfully pasted the code in pastebin").sendr("&aLink: &6{link}", Replacer.create("{link}", link));
                                    } catch (IOException e) {
                                        Messager.send(sender, "&cAn error occurred when trying to export the code");
                                        e.printStackTrace();
                                    } catch (PastebinException e) {
                                        Messager.send(sender, "&cAn error occurred when trying to paste the code");
                                        e.printStackTrace();
                                    }
                                } else Messager.send(sender, "&cYou have to provide a name for the file");
                            } else Messager.send(sender, "&cYou have to code an item first!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
                        break;
                    }

                    case "reload": {
                        reloadConfig();
                        FilePaster.setAPIKey(getConfig().getString("pastebin_api_key"));
                        Messager.send(sender, "&aThe configuration has been reloaded");
                        break;
                    }

                    case "clear": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(storage.containsKey(player.getUniqueId())) {
                                storage.remove(player.getUniqueId());
                                Messager.send(sender, "&aCleared your code clipboard");
                            } else Messager.send(sender, "&cYou have not coded a item before!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
                        break;
                    }

                    case "purge": {
                        List<File> files = Arrays.asList(getSnippetsFolder().listFiles());
                        if(!files.isEmpty()) {
                            for(File file : files) {
                                file.delete();
                            }
                            Messager.sendf(sender, "&aSuccessfully deleted &7%s&a code snippets", files.size());
                        } else Messager.send(sender, "&cThere are no code snippets to delete");
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
                                    Messager.sendr(sender, "&aSet the name to &7\"&f&o{name}&7\"", Replacer.create("{name}", name));
                                } else Messager.send(sender, "&cYou have to provide an name");
                            } else Messager.send(sender, "&cYou need to have a item in your hand");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
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
                                        Messager.sendr(sender, "&aSet the amount to &6\"&7{amount}&6\"", Replacer.create("{amount}", String.valueOf(amount)));
                                    } catch (Exception e) {
                                        Messager.send(sender, "&cThe amount has to be an integer");
                                    }
                                } else Messager.send(sender, "&cYou have to provide an integer");
                            } else Messager.send(sender, "&cYou need to have a item in your hand");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
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
                                        Messager.sendr(sender, "&aSet the durability to &6\"&7{durability}&6\"", Replacer.create("{durability}", String.valueOf(durability)));
                                    } catch (Exception e) {
                                        Messager.send(sender, "&cThe durability has to be an short");
                                    }
                                } else Messager.send(sender, "&cYou have to provide an short");
                            } else Messager.send(sender, "&cYou need to have a item in your hand");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
                        break;
                    }

                    case "lore": {
                        Messager.send(sender, "&cThis feature is not implemented yet");
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack item = player.getItemInHand();
                            if(item.getType() != Material.AIR) {
                                ItemMeta meta = ((item.hasItemMeta()) ? item.getItemMeta() : getServer().getItemFactory().getItemMeta(item.getType()));
                                List<String> lore = meta.getLore();
                                switch (args.length) {
                                    case 2: {
                                        lore.add(Messager.colorize(args[1]));
                                        Messager.sendr(sender, "&aAdded the line &6\"&7{text}&6\" to the lore", Replacer.create("{text}", args[1]));
                                        break;
                                    }
                                    case 3: {
                                        try {
                                            int index = Integer.valueOf(args[1]);
                                            try {
                                                lore.set(index, Messager.colorize(args[2]));
                                                Messager.sendr(sender, "&aSet the line {line} to &6\"&7{text}&6\"", Replacer.create("{line}", String.valueOf(index)).add("{text}", args[2]));
                                            } catch (Exception e) {
                                                Messager.send(sender, "&cThe lore does not have that line");
                                            }
                                        } catch (Exception e) {
                                            Messager.send(sender, "&cYou have to provide an number of the line you want to edit");
                                        }
                                        break;
                                    }
                                    default: Messager.send(sender, "&cYou have to provide an line of text");
                                }

                            } else Messager.send(sender, "&cYou need to have a item in your hand");
                        } else Messager.send(sender, "&cThis command can only be executed by a player");
                        break;
                    }
                    default: Messager.sendf(sender, "&cYou have used a bad argument, run &7/%s &cfor more info", cmd.getName());
                }
            }
            return true;
        }
        return false;
    }

    public File getSnippetsFolder() {
        return snippets;
    }
}