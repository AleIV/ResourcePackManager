package me.aleiv.core.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.PaperCommandManager;
import kr.entree.spigradle.annotations.SpigotPlugin;
import lombok.Getter;
import me.aleiv.core.paper.commands.ResourcePackCMD;
import me.aleiv.core.paper.objects.ResourcePack;
import me.aleiv.core.paper.utilities.JsonConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;

@SpigotPlugin
public class Core extends JavaPlugin {

    private static @Getter Core instance;
    private @Getter PaperCommandManager commandManager;
    private @Getter ResourcePackManager resourcePackManager;
    private @Getter static MiniMessage miniMessage = MiniMessage.get();
    private @Getter Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        instance = this;

        //MANAGER

        resourcePackManager = new ResourcePackManager(this);

        //COMMANDS
        
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new ResourcePackCMD(this));

    }

    @Override
    public void onDisable() {

    }

    public void pushJson(){
        try {
            var list = resourcePackManager.getResourcePacks();
            var jsonConfig = new JsonConfig("resourcepacks.json");
            var json = gson.toJson(list);
            var obj = gson.fromJson(json, JsonObject.class);
            jsonConfig.setJsonObject(obj);
            jsonConfig.save();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void pullJson(){
        try {
            var jsonConfig = new JsonConfig("resourcepacks.json");
            var list = jsonConfig.getJsonObject();
            var iter = list.entrySet().iterator();
            var map = resourcePackManager.getResourcePacks();
            var gson = instance.getGson();

            while (iter.hasNext()) {
                var entry = iter.next();
                var name = entry.getKey();
                var value = entry.getValue();
                var obj = gson.fromJson(value, ResourcePack.class);
                map.put(name, obj);

            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}