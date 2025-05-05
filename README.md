# EntityHarvestAPI
## What is this?
EntityHarvestAPI is a simple API for modders to add custom harvesting mechanics to their modded entities.  
It also included some basic implementations of the API, which can play it in the game.  

## Builtin Features
In default, this mod provides some builtin features:  
* Harvesting falling blocks  
* Using tools to destroy boats and mine carts
* Harvesting ths upper shell of Shulker by using pickaxes
* Harvesting Skeletons' skull by using silk touch enchanted pickaxes  
* Harvesting End Crystals by using silk touch enchanted pickaxes  

Furthermore, all the builtin features can be disabled in config file.

## API Usage
* Modded Entity  
If you want to add harvest mechanics to your modded entity, you just need to implement Harvestable interface.  
The API will automatically execute the implemented methods.
```java
import net.quepierts.entityharvest.api.Harvestable;

public class ExampleEntityClass extends Entity
        implements Harvestable {
    
    @Override
    public boolean canHarvest(Player player) {
        // Your implement
    }

    @Override
    public void onDestroyed(Player player) {
        // Your implement
    }

    @Override
    public void onDestroying(Player player, int tick) {
        // Your implement
    }

    @Override
    public float getProgress(Player player) {
        // Your implement
    }
}
```

* Vanilla Entity  
If you want to add harvest mechanics to vanilla entity, you need to register a **HarvestWrapper**. _(You also can mixin an entity class and implement Harvestable interface, but it's not recommended)_     
By subscribe **EntityHarvestEvent** to register a **HarvestWrapper** for a vanilla entity.  
Here's an example of registering a **HarvestWrapper** for EndCrystal.
```java
import net.quepierts.entityharvest.api.HarvestWrapper;

public class ExampleCrystalHarvestWrapper implements HarvestWrapper<EndCrystal> {
    @Override
    public boolean canHarvest(Player player) {
        // Your implement
    }

    @Override
    public void onDestroyed(Player player) {
        // Your implement
    }

    @Override
    public void onDestroying(Player player, int tick) {
        // Your implement
    }

    @Override
    public float getProgress(Player player) {
        // Your implement
    }
}
```

```java
// Subscribe the event EntityHarvestEvent
import net.quepierts.entityharvest.api.RegisterHarvestWrapperEvent;

@EventBusSubscriber(modid = YourMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ExampleHarvestWrapper {
    @SubscribeEvent
    private static void onRegister(final RegisterHarvestWrapperEvent event) {
        event.register(EndCrystal.class, new ExampleCrystalHarvestWrapper());
    }
}
```