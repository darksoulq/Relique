package com.github.darksoulq.relique.data;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.ops.ByteOps;
import com.github.darksoulq.abyssallib.common.serialization.ops.YamlOps;
import com.github.darksoulq.relique.Relique;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Represents persistent per-player data for the Relique system.
 * <p>
 * This class handles storing, loading, and managing equipped relics across all registered slot types.
 * Each player has an individual YAML file under {@code config/relique/data/}, which keeps their
 * relic inventory synced across sessions.
 */
public class PlayerData {
    /** The folder where all player relic data is stored. */
    private static final File DATA_FOLDER = new File(Relique.INSTANCE.getDataFolder().getParentFile().getParent(), "config/relique/data");
    /** Cached player data instances to avoid unnecessary reloads. */
    private static final Map<UUID, PlayerData> CACHE = new HashMap<>();

    private final Player player;
    private final Map<String, List<ItemStack>> slots = new HashMap<>();


    private PlayerData(Player player) {
        this.player = player;
    }

    /**
     * Retrieves the {@link PlayerData} instance for the given player, loading it from disk if necessary.
     *
     * @param player the player to get data for
     * @return the loaded or cached {@link PlayerData} instance
     */
    public static PlayerData get(Player player) {
        return CACHE.computeIfAbsent(player.getUniqueId(), id -> load(player));
    }

    /**
     * Loads player data from the YAML file on disk.
     * <p>
     * Each slot is automatically filled or truncated to match its registered amount in {@link Slots}.
     *
     * @param player the player whose data should be loaded
     * @return the loaded {@link PlayerData} instance
     */
    private static PlayerData load(Player player) {
        File file = new File(DATA_FOLDER, player.getUniqueId() + ".yml");
        PlayerData data = new PlayerData(player);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for (String slotType : cfg.getKeys(false)) {
            List<?> unparsed = cfg.getList(slotType, new ArrayList<>());
            List<ItemStack> items = new ArrayList<>();
            unparsed.forEach(v -> {
                try {
                    items.add(Codecs.ITEM_STACK.nullable().decode(YamlOps.INSTANCE, v));
                } catch (Codec.CodecException e) {
                    throw new RuntimeException(e);
                }
            });
            data.slots.put(slotType, new ArrayList<>(items));
        }

        for (Slots.Slot slot : Slots.all()) {
            List<ItemStack> list = data.slots.computeIfAbsent(slot.getName(), k -> new ArrayList<>());
            while (list.size() < slot.getAmount()) list.add(slot.getDisplay().clone());
            if (list.size() > slot.getAmount()) list.subList(slot.getAmount(), list.size()).clear();
        }

        return data;
    }

    /**
     * Saves this player's relic data to disk.
     * <p>
     * Each slot type is serialized into YAML using AbyssalLib's {@link Codecs#ITEM_STACK} codec.
     */
    public void save() {
        File file = new File(DATA_FOLDER, player.getUniqueId() + ".yml");
        file.getParentFile().mkdirs();
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            slots.forEach((key, items) -> {
                try {
                    cfg.set(key, Codecs.ITEM_STACK.nullable().list().encode(YamlOps.INSTANCE, items));
                } catch (Codec.CodecException e) {
                    throw new RuntimeException(e);
                }
            });
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the item currently stored in a given slot and index.
     *
     * @param slot  the slot type
     * @param index the index inside the slot (0-based)
     * @return an {@link Optional} containing the item, or empty if out of range or missing
     */
    public Optional<ItemStack> get(Slots.Slot slot, int index) {
        List<ItemStack> list = slots.get(slot.getName());
        if (list == null || index < 0 || index >= list.size()) return Optional.empty();
        return Optional.ofNullable(list.get(index));
    }

    /**
     * Sets an item into the specified slot and index, expanding the list if necessary.
     *
     * @param slot  the slot type
     * @param index the slot index (0-based)
     * @param item  the item to place
     */
    public void set(Slots.Slot slot, int index, ItemStack item) {
        List<ItemStack> list = slots.computeIfAbsent(slot.getName(), k -> new ArrayList<>());
        while (list.size() <= index) list.add(null);
        list.set(index, item);
    }

    /**
     * Returns all items currently in the given slot type.
     *
     * @param slot the slot type
     * @return a mutable list of items for that slot
     */
    public List<ItemStack> getAll(Slots.Slot slot) {
        return slots.getOrDefault(slot.getName(), new ArrayList<>());
    }

    /**
     * Counts how many empty slots are available in this slot type.
     *
     * @param slot the slot type
     * @return the number of empty slots
     */
    public int getEmptySlots(Slots.Slot slot) {
        List<ItemStack> list = slots.getOrDefault(slot.getName(), new ArrayList<>());
        long empty = IntStream.range(0, slot.getAmount())
                .filter(i -> i >= list.size() || list.get(i).isSimilar(slot.getDisplay()))
                .count();
        return (int) empty;
    }

    /**
     * Returns the first available empty index in the given slot type.
     *
     * @param slot the slot type
     * @return the first available index, or {@code -1} if the slot is full
     */
    public int firstEmpty(Slots.Slot slot) {
        List<ItemStack> list = slots.getOrDefault(slot.getName(), new ArrayList<>());
        for (int i = 0; i < slot.getAmount(); i++) {
            if (i >= list.size() || list.get(i).isSimilar(slot.getDisplay())) return i;
        }
        return -1;
    }

    /**
     * Checks whether the given slot type is completely full.
     *
     * @param slot the slot type
     * @return {@code true} if there are no empty positions, otherwise {@code false}
     */
    public boolean isFull(Slots.Slot slot) {
        return getEmptySlots(slot) == 0;
    }

    /**
     * Returns an unmodifiable view of all slot data.
     * <p>
     * The returned map cannot be modified directly; use {@link #set(Slots.Slot, int, ItemStack)} to change items.
     *
     * @return an immutable view of the player's slot contents
     */
    public Map<String, List<ItemStack>> all() {
        return Collections.unmodifiableMap(slots);
    }
}
