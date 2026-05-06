# Custom Validators
<link-summary>Guide to creating and registering custom item validation logic for slots</link-summary>

Validators act as the gatekeepers for equipment slots. They dictate whether a specific `ItemStack` is legally allowed to be equipped into a given slot by a specific `LivingEntity`.

By default, every slot uses the `relique:tag` validator, which restricts items based on the AbyssalLib tag system. However, developers can easily create and register their own custom validation logic (e.g., level requirements, class restrictions, or soulbound mechanics).

### For Developers: Creating a Validator
To create a custom validator, you must implement the `RelicValidator` functional interface. This interface requires a single method: `isValid(String slotId, ItemStack item, LivingEntity entity)`.

Because Relique utilizes AbyssalLib's registry system, you register your custom validators using a `DeferredRegistry` targeted at `RelicRegistries.VALIDATORS`.

#### Example: Level Requirement Validator
Below is an example of creating a validator that restricts players from equipping an item unless they have at least 30 experience levels.

```Java
import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry;
import com.github.darksoulq.relique.api.RelicValidator;
import com.github.darksoulq.relique.core.RelicRegistries;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MyAddonValidators {

    // 1. Create a DeferredRegistry targeting the Relique Validator registry
    public static final DeferredRegistry<RelicValidator> VALIDATORS = DeferredRegistry.create(RelicRegistries.VALIDATORS, "my_addon");

    // 2. Register your custom validator
    public static final RelicValidator LEVEL_REQ = VALIDATORS.register("level_requirement", id -> (slotId, item, entity) -> {
        
        // If the entity is a player, enforce the level requirement
        if (entity instanceof Player player) {
            return player.getLevel() >= 30;
        }
        
        // Non-players (like custom mobs) bypass this check
        return true;
    });
}
```

<note>
Do not forget to call <code>MyAddonValidators.VALIDATORS.apply()</code> in your plugin's <code>onEnable()</code> method to actively inject your validator into the Relique ecosystem!
</note>

---

### For Server Owners: Using Custom Validators
Once a developer has registered a custom validator, server owners can assign it to any slot using the slot's JSON configuration file.

To apply the validator, simply add its namespaced key (the plugin's namespace + the validator's registered name) to the `validators` array.

#### JSON Example
Applying our newly created `my_addon:level_requirement` validator to the default `ring` slot:

```json
{
  "operation": "SET",
  "size": 2,
  "validators": [
    "relique:tag",
    "my_addon:level_requirement"
  ]
}
```

**How Multiple Validators Work:**
If a slot contains multiple validators, an item **must pass ALL of them** to be equipped. In the example above, the item must belong to the `relique:ring` tag AND the player must be level 30 or higher.