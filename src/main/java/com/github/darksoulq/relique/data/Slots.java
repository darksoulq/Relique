package com.github.darksoulq.relique.data;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordCodecBuilder;
import com.github.darksoulq.abyssallib.common.serialization.ops.YamlOps;
import com.github.darksoulq.abyssallib.server.registry.Registry;
import com.github.darksoulq.relique.Relique;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Manages all registered {@link Slot} types for the Relique system.
 * <p>
 * Each slot defines a category of relics (e.g., "necklace", "ring", "charm") and
 * how many relics of that type a player can equip. Slots are serialized to and from
 * {@code config/relique/slots.yml}.
 */
public final class Slots {
    /** Registry of all loaded or registered slots. */
    private static final Registry<Slot> REGISTRY = new Registry<>();
    /** The configuration file storing all slot definitions. */
    private static final File FILE = new File(Relique.INSTANCE.getDataFolder().getParentFile().getParent(), "config/relique/slots.yml");

    /**
     * Retrieves a slot type by name.
     *
     * @param name the name of the slot (case-insensitive)
     * @return an {@link Optional} containing the slot if found
     */
    public static Optional<Slot> get(String name) {
        return Optional.ofNullable(REGISTRY.get(name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Registers a new slot type or overwrites an existing one.
     * <p>
     * Automatically saves all slots to the configuration file.
     *
     * @param name          the slot name
     * @param defaultAmount how many relics can be equipped in this slot
     * @param display       the {@link ItemStack} representing the empty slot in UI
     * @return the registered slot
     */
    public static Slot register(String name, int defaultAmount, ItemStack display) {
        Slot slot = new Slot(name, defaultAmount, display);
        REGISTRY.register(slot.getName(), slot);
        save();
        return REGISTRY.get(slot.getName());
    }

    /**
     * Returns all registered slot types.
     *
     * @return a collection of all loaded {@link Slot} definitions
     */
    public static Collection<Slot> all() {
        return REGISTRY.getAll().values();
    }

    /**
     * Checks if a slot with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if the slot exists, otherwise {@code false}
     */
    public static boolean exists(String name) {
        return REGISTRY.contains(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Loads all slot definitions from {@code slots.yml}.
     * <p>
     * If the file does not exist, it is created empty.
     */
    public static void load() {
        try {
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                FILE.createNewFile();
            }

            InputStream stream = new FileInputStream(FILE);
            Object root = YamlOps.INSTANCE.parse(stream);
            List<?> slots = root instanceof List<?> list ? list : Collections.singletonList(root);
            for (Object slotData : slots) {
                if (slotData instanceof Map<?, ?>) {
                    Slot slot = Slot.CODEC.decode(YamlOps.INSTANCE, slotData);
                    REGISTRY.register(slot.getName(), slot);
                }
            }
        } catch (Codec.CodecException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves all currently registered slot definitions to {@code slots.yml}.
     */
    public static void save() {
        try {
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                FILE.createNewFile();
            }

            List<Map<String, Object>> slotList = new ArrayList<>();

            for (Slot slot : all()) {
                slotList.add(Map.of(
                        "display", Codecs.ITEM_STACK.encode(YamlOps.INSTANCE, slot.display),
                        "amount", slot.getAmount(),
                        "name", slot.getName()
                ));
            }

            try (FileWriter writer = new FileWriter(FILE, false)) {
                new Yaml().dump(slotList, writer);
            }
        } catch (IOException | Codec.CodecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents a relic slot definition.
     * <p>
     * Each slot has a name, an amount (max number of slots od this type),
     * and a display item shown in the player's relic UI when the slot is empty.
     */
    public static final class Slot {
        /** Codec for serializing and deserializing slot definitions. */
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(
                Codecs.STRING.fieldOf("name", Slot::getName),
                Codecs.INT.fieldOf("amount", Slot::getAmount),
                Codecs.ITEM_STACK.fieldOf("display", Slot::getDisplay),
                Slot::new
        );

        private final String name;
        private int amount;
        private final ItemStack display;

        /**
         * Creates a new relic slot.
         *
         * @param name   the slot name (stored in lowercase)
         * @param amount how many slots of this type will be available in the gui (by default)
         * @param display the {@link ItemStack} to represent this slot visually
         */
        public Slot(String name, int amount, ItemStack display) {
            this.name = name.toLowerCase(Locale.ROOT);
            this.amount = amount;
            this.display = display;
        }

        /**
         * Gets the lowercase slot name.
         *
         * @return the slot name
         */
        public String getName() { return name; }
        /**
         * Gets how many slots of this type will be available in the gui.
         *
         * @return the slot numbers
         */
        public int getAmount() { return amount; }
        /**
         * Gets the {@link ItemStack} used to represent an empty slot.
         *
         * @return the display item
         */
        public ItemStack getDisplay() {
            return display;
        }
        /**
         * Sets the number of slots that appear in the gui for this type.
         *
         * @param amount the new slot amount
         */
        public void setAmount(int amount) { this.amount = amount; }
    }
}