package me.jaime29010.itemcoder;

import static com.squareup.javapoet.MethodSpec.Builder;

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
                        "&3/itemcoder code &7- &cConverts generated code to a item.",
                        "&3/itemcoder decode &7- &cConverts the item in your hand to code.",
                        "&3/itemcoder export [NAME] &7- &cExport the code to a file",
                        "&3/itemcoder paste [NAME] &7- &cPaste the code to pastebin",
                        "&3/itemcoder reload &7- &cReloads the configuration of the plugin.",
                        "&3/itemcoder clear &7- &cDeletes all the generated code.",
                        "&e=====================================================");
            } else {
                switch (args[0].toLowerCase()) {
                    default: Messager.sendf(sender, "&cYou have used a bad argument, run &7/%s &cfor more info", cmd.getName());
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
                        } else Messager.send(sender, "&cThis command can only be executed by a player.");
                        break;
                    }

                    case "decode": {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            Messager.send(sender, "&cThis feature is not implemented yet!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player.");
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
                                    Messager.sendr(sender, "&aItem successfully exported as &6{name}", Replacer.add("{name}", name));
                                } else Messager.send(sender, "&cYou have to provide a name for the file");
                            } else Messager.send(sender, "&cYou have to code an item first!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player.");
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
                                        Messager.send(sender, "&aSucessfully pasted the code in pastebin.").sendr("&aLink: &6{link}", Replacer.add("{link}", link));
                                    } catch (IOException e) {
                                        Messager.send(sender, "&cAn error occurred when trying to export the code");
                                        e.printStackTrace();
                                    } catch (PastebinException e) {
                                        Messager.send(sender, "&cAn error occurred when trying to paste the code");
                                        e.printStackTrace();
                                    }
                                } else Messager.send(sender, "&cYou have to provide a name for the file");
                            } else Messager.send(sender, "&cYou have to code an item first!");
                        } else Messager.send(sender, "&cThis command can only be executed by a player.");
                        break;
                    }

                    case "reload": {
                        reloadConfig();
                        FilePaster.setAPIKey(getConfig().getString("pastebin_api_key"));
                        Messager.send(sender, "&aThe configuration has been reloaded");
                        break;
                    }

                    case "clear": {
                        List<File> files = Arrays.asList(getSnippetsFolder().listFiles());
                        if(!files.isEmpty()) {
                            for(File file : files) {
                                file.delete();
                            }
                            Messager.sendf(sender, "&aSuccessfully deleted &7%s&a code snippets.", files.size());
                        } else Messager.send(sender, "&cThere are no code snippets to delete.");
                        break;
                    }
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
