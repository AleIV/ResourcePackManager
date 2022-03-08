package me.aleiv.core.paper.commands;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import lombok.NonNull;
import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.ResourcePack;
import net.md_5.bungee.api.ChatColor;

@CommandAlias("rp|resourcepack")
@CommandPermission("admin.perm")
public class ResourcePackCMD extends BaseCommand {

    private @NonNull Core instance;

    public ResourcePackCMD(Core instance) {
        this.instance = instance;

        var manager = instance.getCommandManager();
        var resourcePackManager = instance.getResourcePackManager();

        manager.getCommandCompletions().registerAsyncCompletion("bool", c -> {
            return ImmutableList.of("true", "false");
        });

        manager.getCommandCompletions().registerStaticCompletion("url", "url");
        manager.getCommandCompletions().registerStaticCompletion("name", "name");
        manager.getCommandCompletions().registerStaticCompletion("hash", "hash");

        manager.getCommandCompletions().registerAsyncCompletion("resourcepacks", c -> {
            return resourcePackManager.getResourcePacks().keySet().stream().toList();
        });

    }

    @Subcommand("send")
    @CommandCompletion("@resourcepacks @players")
    public void send(CommandSender sender, String rp, @Flags("other") Player player){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();
        if(resourcepacks.containsKey(rp)){
            
            resourcePackManager.askTexturePack(player);
            sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack " + rp + " sent to " + player.getName());

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is not registered.");
        }

    }

    @Subcommand("list")
    public void list(CommandSender sender){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();
        sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack list: " + ChatColor.WHITE + resourcepacks.keySet().toString());

    }


    @Subcommand("send all")
    @CommandCompletion("@resourcepacks")
    public void sendAll(CommandSender sender, String rp){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();
        if(resourcepacks.containsKey(rp)){
            
            Bukkit.getOnlinePlayers().forEach(player ->{
                resourcePackManager.askTexturePack(player);
            });
            sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack " + rp + " sent to all players.");

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is not registered.");
        }

        

    }

    @Subcommand("create")
    @CommandCompletion("@name @url @hash")
    public void create(CommandSender sender, String rp, String url, byte[] hash){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();

        if(!resourcepacks.containsKey(rp)){
            
            resourcepacks.put(rp, new ResourcePack(rp, url, hash));
            sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack " + rp + " URL: " + url + " HASH: " + hash + " created.");

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is already registered.");
        }

    }

    @Subcommand("delete")
    @CommandCompletion("@resourcepacks")
    public void delete(CommandSender sender, String rp){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();

        if(rp.equals("global")){
            sender.sendMessage(ChatColor.RED + "Global ResourcePack can't be deleted.");
            return;
        }

        if(resourcepacks.containsKey(rp)){
            
            resourcepacks.remove(rp);
            sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack " + rp + " deleted.");

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is not registered.");
        }

    }

    @Subcommand("edit")
    @CommandCompletion("@resourcepacks @url @hash")
    public void edit(CommandSender sender, String rp, String url, byte[] hash){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();

        if(resourcepacks.containsKey(rp)){
            
            var resourcePack = resourcepacks.get(rp);
            resourcePack.setResoucePackURL(url);
            resourcePack.setResourcePackHash(hash);
            sender.sendMessage(ChatColor.of("#fae19e") + "ResourcePack " + rp + " URL: " + url + " HASH: " + hash + " edited.");

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is not registered.");
        }

    }

    @Subcommand("enable")
    @CommandCompletion("@bool")
    public void enable(CommandSender sender, boolean bool){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();

        var global = resourcepacks.get("global");
        global.setEnabled(!global.isEnabled());

        if(global.isEnabled()){
            sender.sendMessage(ChatColor.of("#fae19e") + "Global ResourcePack disabled.");
        }else{
            sender.sendMessage(ChatColor.of("#fae19e") + "Global ResourcePack enabled.");
        }

    }

    @Subcommand("global")
    @CommandCompletion("@resourcepacks")
    public void global(CommandSender sender, String rp){
        var resourcePackManager = instance.getResourcePackManager();
        var resourcepacks = resourcePackManager.getResourcePacks();

        if(resourcepacks.containsKey(rp)){
            
            var global = resourcepacks.get("global");
            var resourcePack = resourcepacks.get(rp);
            global.setResoucePackURL(resourcePack.getResoucePackURL());
            global.setResourcePackHash(resourcePack.getResourcePackHash());
            sender.sendMessage(ChatColor.of("#fae19e") + "Global ResourcePack changed to " + rp + ".");

        }else{
            sender.sendMessage(ChatColor.RED + "ResourcePack is not registered.");
        }
    }

}
