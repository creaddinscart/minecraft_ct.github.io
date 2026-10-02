package example;

import com.ct.client.loader.CTMod;
import com.ct.client.loader.CTModContext;

public final class ExampleMod implements CTMod {
    @Override
    public void onInitialize(CTModContext context) {
        context.log("Example Mod initialized");
        context.log("Game directory: " + context.gameDirectory());
        context.log("Mods directory: " + context.modsDirectory());
        context.log("Mod id: " + context.metadata().id());
        context.log("Mod version: " + context.metadata().version());
    }
}
