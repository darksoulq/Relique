# A Curios-Like Plugin for PaperMC

> [!IMPORTANT]
> This plugin Depends on [AbyssalLib](https://github.com/darksoulq/AbyssalLib)

Relique adds a trinket system similar to ones added by Curios (and its fabric parallel Trinkets) allowing plugin devs to make equipable relics.

# Server Owners
For server owners, Relique comes with a config file for tweaking the number of slots (and add their own slot types) and changing the display item for the slot.

> The config is located at server/config/relique/slots.yml

### Changing amount and display of slot types
inside the file you should see something like
```YAML
- name: head
  amount: 1
  display: {'abyssallib:relique:head': 1}
- name: chest
  amount: 1
  display: {'abyssallib:relique:chest': 1}
- name: belt
  amount: 1
  display: {'abyssallib:relique:belt': 1}
- name: ring
  amount: 2
  display: {'abyssallib:relique:ring': 1}
- name: charm
  amount: 2
  display: {'abyssallib:relique:charm': 1}
```

for changing the number of slots simply change the "amount" value.

as for display item, change the display: parameter (do not mess with the : 1, thats the amount of item and is meant to be 1)

> "display" can be changed to something like 'minecraft:hoe' or 'nexo:namespace:itemid' or as in the above config, to 'abyssallib:namespace:itemid', this supports any item that the AbyssalLib item bridge supports.

### Per Player Slots
You can assign a different number of slots to each player using permission, The permission format is:
`relique.<slot_name>.<number>`

# Developers
For developers the API is pretty easy to use.

Repository and Dependency:
```Gradle
repositories {
    // other repos
    maven {
        url = uri("https://jitpack.io/")
    }
}
```

```Gradle
dependencies {
    compileOnly("com.github.darksoulq:Relique:<version>")
}
```

the version is the tag of the release in GitHub Releases

### Creating a Relic:
Simply extend and implement the Relic class:
```Java
public class TestRelic extends Relic {

    public TestRelic(Identifier id) {
        super(id, Relique.HEAD);
    }

    @Override
    public void onEquip(Player wearer, ItemStack item) {
        wearer.sendMessage("Worn!");
    }

    @Override
    public void onUnEquip(Player wearer, ItemStack item) {

    }

    @Override
    public void onTick(Player wearer, ItemStack item) {
        wearer.sendMessage("Tick!");
    }
}
```

> Relique.HEAD is the default "head" slot, if you have a custom slot make sure to use Slots.get("slot_name"), it returns the actually registered Slot as some other plugin may have registered a slot with the same name as yours before.
> Slot registration will be shown below

You now have to register this relic, otherwise it will NOT work (Relic registration should be done AFTER slot registration, and must be done in onEnable)
```Java
public class Relics {
    public static final DeferredRegistry<Relic> RELICS = DeferredRegistry.create(Relic.RELICS, "plugin_id");
    
    public static final Holder<Relic> TEST = RELICS.register("test", TestRelic::new);
}
```

now in onEnable call `Relics.RELICS.apply();`

To apply this Relic to an item you have two ways:

1. If making an Item using the AbyssalLib Item class:

    This is as simple as calling `item.setData(new RelicComponent(Relic))` (or `this.setData(new RelicComponent(Relic))` if adding in the constructor)
2. If using an ItemStack:

    Call `RelicItem.applyRelic(ItemStack, Relic)` and optionally `RelicItem.applyDefaultLore(stack)` after calling `applyRelic` if you wish to use the default `Slot: slot_name` format added to start of lore.

### Creating custom slots:
To create a slot simply call `Slots.register("name", defaultAmount, displayItem)`, it is recommended to store the returned Slots.Slot as it will return an already existing Slot if one with the same name had been registered already.

### Other Info
There are also some other useful methods inside `RelicManager`, `PlayerData` and `RelicItem` (to get PlayerData simply call the `#get(Player)` method), such as `RelicItem.isRelic(ItemStack)`, `RelicItem.getRelic(ItemStack)`, `RelicManager.getRelic(Player, Slot, Index)`

# Credits
[Curios](https://github.com/TheIllusiveC4/Curios) and [Trinkets](https://github.com/emilyploszaj/trinkets) for the plugin idea.