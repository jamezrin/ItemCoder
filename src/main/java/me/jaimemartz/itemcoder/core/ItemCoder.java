package me.jaimemartz.itemcoder.core;

import com.google.common.base.Joiner;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemCoder {
    public static Builder code(ItemStack item, Plugin plugin) {
        Builder builder = MethodSpec.methodBuilder("getItemStack");
        {
            builder.addJavadoc(Joiner.on("\n").join("This method has been generated using ItemStackCoder", "More info at https://www.spigotmc.org/resources/itemstackcoder-item2java.13053/"));
            builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            builder.addStatement("$T item = new $T($T.$L)", ItemStack.class, ItemStack.class, Material.class, item.getType().name());

            if (item.getDurability() != 0) {
                builder.addStatement("item.setDurability(($T)$L)", short.class, item.getDurability());
            }

            if (item.getAmount() != 1) {
                builder.addStatement("item.setAmount($L)", item.getAmount());
            }

            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                switch (item.getType()) {
                    case BANNER: {
                        if (meta instanceof BannerMeta) {
                            BannerMeta bannerMeta = (BannerMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", BannerMeta.class, BannerMeta.class);
                            builder.addStatement("meta.setBaseColor($T.$L)", DyeColor.class, bannerMeta.getBaseColor().name());
                            for(Pattern pattern : bannerMeta.getPatterns()) {
                                builder.addStatement("meta.addPattern(new $T($T.$L, $T.$L))", Pattern.class, DyeColor.class, pattern.getColor().name(), PatternType.class, pattern.getPattern().name());
                            }
                        }
                        break;
                    }

                    case BOOK_AND_QUILL:
                    case WRITTEN_BOOK: {
                        if (meta instanceof BookMeta) {
                            BookMeta bookMeta = (BookMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", BookMeta.class, BookMeta.class);

                            if (bookMeta.hasAuthor()) {
                                builder.addStatement("meta.setAuthor($S)", bookMeta.getAuthor());
                            }

                            if (bookMeta.hasTitle()) {
                                builder.addStatement("meta.setTitle($S)", bookMeta.getTitle());
                            }

                            if (bookMeta.hasPages()) {
                                builder.addStatement("$T<$T> pages = new $T<>()", List.class, String.class, ArrayList.class);
                                for (String page : bookMeta.getPages()) {
                                    builder.addStatement("pages.add($S)", page);
                                }
                                builder.addStatement("meta.setPages(pages)");
                            }

                            item.setItemMeta(bookMeta);
                        }
                        break;
                    }

                    case ENCHANTED_BOOK: {
                        if (meta instanceof EnchantmentStorageMeta) {
                            EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", EnchantmentStorageMeta.class, EnchantmentStorageMeta.class);
                            if (enchantMeta.hasStoredEnchants()) {
                                for (Map.Entry<Enchantment, Integer> entry : enchantMeta.getStoredEnchants().entrySet()) {
                                    builder.addStatement("meta.addStoredEnchant($T.$L, $L, $L)", Enchantment.class, entry.getKey().getName(), entry.getValue(), true);
                                }
                            }
                            item.setItemMeta(enchantMeta);
                        }
                        break;
                    }

                    case FIREWORK_CHARGE: {
                        if (meta instanceof FireworkEffectMeta) {
                            FireworkEffectMeta effectMeta = (FireworkEffectMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", FireworkEffectMeta.class, FireworkEffectMeta.class);

                            if (effectMeta.hasEffect()) {
                                FireworkEffect effect = effectMeta.getEffect();
                                builder.addStatement("$T effect = $T.builder()", FireworkEffect.Builder.class, FireworkEffect.class);
                                builder.addStatement("effect.with($T.$L)", FireworkEffect.Type.class, effect.getType().name());

                                if(effect.hasFlicker()) {
                                    builder.addStatement("effect.withFlicker()");
                                }

                                if(effect.hasTrail()) {
                                    builder.addStatement("effect.withTrail()");
                                }

                                for(Color color : effect.getColors()) {
                                    builder.addStatement("effect.withColor($T.fromRGB($L))", Color.class, color.asRGB());
                                }

                                for(Color color : effect.getFadeColors()) {
                                    builder.addStatement("effect.withFade($T.fromRGB($L))", Color.class, color.asRGB());
                                }
                                builder.addStatement("meta.setEffect(effect.build())", FireworkEffect.class);
                            }
                        }
                        break;
                    }

                    case FIREWORK: {
                        if (meta instanceof FireworkMeta) {
                            FireworkMeta fireworkMeta = (FireworkMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", FireworkMeta.class, FireworkMeta.class);
                            if(fireworkMeta.getPower() != 1) {
                                builder.addStatement("meta.setPower($L)", fireworkMeta.getPower());
                            }

                            if (fireworkMeta.hasEffects()) {
                                int effects = 1;
                                for(FireworkEffect effect : fireworkMeta.getEffects()) {
                                    String name = "effect" + effects;
                                    builder.addStatement("$T $L = $T.builder()", FireworkEffect.Builder.class, name, FireworkEffect.class);
                                    builder.addStatement("$L.with($T.$L)", name, FireworkEffect.Type.class, effect.getType().name());

                                    if(effect.hasFlicker()) {
                                        builder.addStatement("$L.withFlicker()", name);
                                    }

                                    if(effect.hasTrail()) {
                                        builder.addStatement("$L.withTrail()", name);
                                    }

                                    for(Color color : effect.getColors()) {
                                        builder.addStatement("$L.withColor($T.fromRGB($L))", name, Color.class, color.asRGB());
                                    }

                                    for(Color color : effect.getFadeColors()) {
                                        builder.addStatement("$L.withFade($T.fromRGB($L))", name, Color.class, color.asRGB());
                                    }
                                    builder.addStatement("meta.addEffect($L.build())", name);
                                    effects++;
                                }
                            }
                        }
                        break;
                    }

                    case LEATHER_BOOTS:
                    case LEATHER_CHESTPLATE:
                    case LEATHER_HELMET:
                    case LEATHER_LEGGINGS: {
                        if (meta instanceof LeatherArmorMeta) {
                            LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", LeatherArmorMeta.class, LeatherArmorMeta.class);
                            if (!armorMeta.getColor().equals(plugin.getServer().getItemFactory().getDefaultLeatherColor())) {
                                builder.addStatement("meta.setColor($T.fromRGB($L))", Color.class, armorMeta.getColor().asRGB());
                            }
                        }
                        break;
                    }

                    case MAP: {
                        if (meta instanceof MapMeta) {
                            MapMeta mapMeta = (MapMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", MapMeta.class, MapMeta.class);
                            if (mapMeta.isScaling()) {
                                builder.addStatement("meta.setScaling($L)", true);
                            }
                        }
                        break;
                    }

                    case POTION: {
                        if (meta instanceof PotionMeta) {
                            PotionMeta potionMeta = (PotionMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", PotionMeta.class, PotionMeta.class);
                            if (potionMeta.hasCustomEffects()) {
                                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                                    builder.addStatement("meta.addCustomEffect(new $T($T.$L, $L, $L, $L, $L), $L)", PotionEffect.class, PotionEffectType.class, effect.getType().getName(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), true);
                                }
                            }
                        }
                        break;
                    }

                    case SKULL_ITEM: {
                        if (meta instanceof SkullMeta) {
                            SkullMeta skullMeta = (SkullMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", SkullMeta.class, SkullMeta.class);
                            if (skullMeta.hasOwner()) {
                                builder.addStatement("meta.setOwner($S)", skullMeta.getOwner());
                            }
                        }
                        break;
                    }
                    
                    case MONSTER_EGGS:
                    case MONSTER_EGG: {
                        if (meta instanceof SpawnEggMeta) {
                            SpawnEggMeta spawnEggMeta = (SpawnEggMeta) meta;
                            builder.addStatement("$T meta = ($T) item.getItemMeta()", SpawnEggMeta.class, SpawnEggMeta.class);
                            builder.addStatement("meta.setSpawnedType($T.$L)", EntityType.class, spawnEggMeta.getSpawnedType());
                        }
                        break;
                    }

                    default: {
                        builder.addStatement("$T meta = item.getItemMeta()", ItemMeta.class);
                    }
                }

                if (meta.hasDisplayName()) {
                    builder.addStatement("meta.setDisplayName($S)", meta.getDisplayName());
                }

                if (meta.hasEnchants()) {
                    for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                        builder.addStatement("meta.addEnchant($T.$L, $L, $L)", Enchantment.class, entry.getKey().getName(), entry.getValue(), false);
                    }
                }

                for (ItemFlag flag : meta.getItemFlags()) {
                    builder.addStatement("meta.addItemFlags($T.$L)", ItemFlag.class, flag.name());
                }

                if (meta.hasLore()) {
                    builder.addStatement("$T<$T> lore = new $T<>()", List.class, String.class, ArrayList.class);
                    for (String line : meta.getLore()) {
                        builder.addStatement("lore.add($S)", line);
                    }
                    builder.addStatement("meta.setLore(lore)");
                }

                builder.addStatement("item.setItemMeta(meta)");
            }

            builder.returns(ItemStack.class);
            builder.addStatement("return item");
        }
        return builder;
    }
}