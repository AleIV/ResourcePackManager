package me.aleiv.core.paper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Data;
import lombok.NonNull;
import me.aleiv.core.paper.objects.ResourcePack;
import net.md_5.bungee.api.ChatColor;

@Data
public class ResourcePackManager implements Listener {

    private @NonNull JavaPlugin javaPlugin;
    private List<UUID> waitingPlayers;

    private String questionMessage;
    private String kickMessage;
    private String failKickMessage;
    private String downloadingMessage;
    private String downloadedMessage;

    private String bypassPerm;
    private boolean force;

    HashMap<String, ResourcePack> resourcePacks =  new HashMap<>();

    /**
     * <p>Nothing will work if {@link #setResoucePackURL(String)} and {@link #setResourcePackHash(byte[])} are not set.</p>
     * <br>
     * <p>Players can bypass the resource pack check by adding the permission {@link #setBypassPerm(String)}.</p>
     * <br>
     * <p>Messages can be changed with {@link #setQuestionMessage(String)}, {@link #setKickMessage(String)}, {@link #setFailKickMessage(String)}, {@link #setDownloadingMessage(String)}, {@link #setDownloadedMessage(String)}.</p>
     * <br>
     * <p>Remember that by default, the player will be forced to download the resource pack. To change this, use {@link #setForce(boolean)}. <bold>Only applies on 1.18+</bold></p>
     *
     * @param javaPlugin Actual plugin instance.
     */
    public ResourcePackManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.waitingPlayers = new ArrayList<>();

        this.questionMessage = ChatColor.translateAlternateColorCodes('&', "");
        this.kickMessage = ChatColor.translateAlternateColorCodes('&', "&cNecesitas el paquete de texturas para continuar.");
        this.failKickMessage = ChatColor.translateAlternateColorCodes('&', "&cHa ocurrido un fallo descargado el paquete de texturas.");
        this.downloadingMessage = ChatColor.translateAlternateColorCodes('&', "");
        this.downloadedMessage = ChatColor.translateAlternateColorCodes('&', "");

        this.force = true;

        resourcePacks.put("global", new ResourcePack("global", null, null));

        Bukkit.getPluginManager().registerEvents(this, javaPlugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {

        var globalRP = resourcePacks.get("global");
        if(globalRP.isEnabled()){
            waitingPlayers.add(e.getPlayer().getUniqueId());

            this.askTexturePack(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        waitingPlayers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResourcePack(PlayerResourcePackStatusEvent e) {
        Player player = e.getPlayer();
        if (bypassPerm != null && player.hasPermission(bypassPerm)) return;

        switch (e.getStatus()) {
            case FAILED_DOWNLOAD -> player.kickPlayer(this.failKickMessage);
            case SUCCESSFULLY_LOADED -> {
                player.sendMessage(this.downloadedMessage);
                this.waitingPlayers.remove(player.getUniqueId());
                Bukkit.getPluginManager().callEvent(new PlayerLoadedResourcePackEvent(player));
            }
            case ACCEPTED -> player.sendMessage(this.downloadingMessage);
            case DECLINED -> {
                /*if (this.force) {
                    this.askTexturePack(player);
                } else {
                    player.kickPlayer(this.kickMessage);
                    Bukkit.getPluginManager().callEvent(new PlayerDeniedResourcePackEvent(player));
                }*/
                player.kickPlayer(this.kickMessage);
                Bukkit.getPluginManager().callEvent(new PlayerDeniedResourcePackEvent(player));
            }
        }
    }

    public void askTexturePack(Player player) {
        var globalRP = resourcePacks.get("global");
        var resoucePackURL = globalRP.getResoucePackURL();
        var resourcePackHash = globalRP.getResourcePackHash();

        if (resoucePackURL == null || resourcePackHash == null) return;

        /*if (this.getVersionNumber() >= 18) {
            try {
                player.setResourcePack(this.resoucePackURL, this.resourcePackHash, this.questionMessage, this.force);
                return;
            } catch (NoSuchMethodError ignored) {}
        }*/
        player.setResourcePack(resoucePackURL, resourcePackHash);
    }

    /**
     * Function to know if a player is waiting for the resource pack to be downloaded or loaded.
     *
     * @param player Player to check
     * @return True if the player is waiting, false if player already has the resource pack loaded.
     */
    public boolean isPlayerWaiting(Player player) {
        return waitingPlayers.contains(player.getUniqueId());
    }

    /**
     * @return List of the UUIDs of the players waiting for the resource pack to be downloaded or loaded.
     */
    public List<UUID> getWaitingPlayers() {
        return new ArrayList<>(waitingPlayers);
    }

    /**
     * All players will be forced to download the resource pack again. <bold>All waiting players will be kicked!</bold>
     */
    public void forceRedownload() {
        this.waitingPlayers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.kickPlayer(this.failKickMessage);
            }
        });
        Bukkit.getOnlinePlayers().forEach(this::askTexturePack);
    }

    /**
     * @return Message showed when the player is asked to accept the resource pack.
     */
    public String getQuestionMessage() {
        return questionMessage;
    }

    /**
     * @return Message showed when the player is kicked by denying the resource pack.
     */
    public String getKickMessage() {
        return kickMessage;
    }

    /**
     * @return Message showed when the player is kicked by failing to download the resource pack. (Not users fault)
     */
    public String getFailKickMessage() {
        return failKickMessage;
    }

    /**
     * @return Message showed when the player accepted the resource pack an starts downloading it.
     */
    public String getDownloadingMessage() {
        return downloadingMessage;
    }

    /**
     * @return Message showed when the player successfully loaded the resource pack.
     */
    public String getDownloadedMessage() {
        return downloadedMessage;
    }

    /**
     * @return Permission required to bypass the resource pack check.
     */
    public String getBypassPerm() {
        return bypassPerm;
    }

    /**
     * @return URL of the resource pack.
     */
    public String getResoucePackURL() {
        var globalRP = resourcePacks.get("global");
        return globalRP.getResoucePackURL();
    }

    /**
     * @return Hash of the resource pack in {@code byte[]}.
     */
    public byte[] getResourcePackHashBytes() {
        var globalRP = resourcePacks.get("global");
        return globalRP.getResourcePackHash();
    }

    /**
     * @return Hash of the resource pack.
     */
    public String getResourcePackHash() {
        var globalRP = resourcePacks.get("global");
        return this.encodeUsingBigIntegerToString(globalRP.getResourcePackHash());
    }

    /**
     * @return If the resource pack should be forced to download or not. Only applies on 1.18+
     */
    public boolean isForce() {
        return force;
    }

    /**
     * @param questionMessage Message showed when the player is asked to accept the resource pack.
     */
    public void setQuestionMessage(String questionMessage) {
        this.questionMessage = questionMessage;
    }

    /**
     * @param kickMessage Message showed when the player is kicked by denying the resource pack.
     */
    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    /**
     * @param failKickMessage Message showed when the player is kicked by failing to download the resource pack. (Not users fault)
     */
    public void setFailKickMessage(String failKickMessage) {
        this.failKickMessage = failKickMessage;
    }

    /**
     * @param downloadingMessage Message showed when the player accepted the resource pack an starts downloading it.
     */
    public void setDownloadingMessage(String downloadingMessage) {
        this.downloadingMessage = downloadingMessage;
    }

    /**
     * @param downloadedMessage Message showed when the player successfully loaded the resource pack.
     */
    public void setDownloadedMessage(String downloadedMessage) {
        this.downloadedMessage = downloadedMessage;
    }

    /**
     * @param bypassPerm Permission required to bypass the resource pack check.
     */
    public void setBypassPerm(String bypassPerm) {
        this.bypassPerm = bypassPerm;
    }

    /**
     * @param resoucePackURL URL of the resource pack.
     */
    public void setResoucePackURL(String resoucePackURL) {
        var globalRP = resourcePacks.get("global");
        globalRP.setResoucePackURL(resoucePackURL);
    }

    /**
     * @param resourcePackHash Hash of the resource pack. <br>Must be SHA1.
     */
    public void setResourcePackHash(String resourcePackHash) {
        var globalRP = resourcePacks.get("global");

        globalRP.setResourcePackHash(decodeUsingBigInteger(resourcePackHash));
    }

    /**
     * @param resourcePackHash Hash of the resource pack. <br>Must be SHA1. <br>Must be 20 bytes long. <br>Must be in hexadecimal format.
     */
    public void setResourcePackHash(byte[] resourcePackHash) {
        var globalRP = resourcePacks.get("global");
        globalRP.setResourcePackHash(resourcePackHash);
    }

    /**
     * @param force If the resource pack should be forced to download or not. Only applies on 1.18+
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Event fired when a player has successfully loaded the resource pack in his/her game.
     */
    public static class PlayerLoadedResourcePackEvent extends PlayerEvent {

        private static HandlerList handlerList;

        public PlayerLoadedResourcePackEvent(Player who) {
            super(who);
            handlerList = new HandlerList();
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    /**
     * Event fired when a player has denied accepting the resource pack and has been kicked.
     */
    public static class PlayerDeniedResourcePackEvent extends PlayerEvent {

        private static HandlerList handlerList;

        public PlayerDeniedResourcePackEvent(Player who) {
            super(who);
            handlerList = new HandlerList();
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public int getVersionNumber() {
        String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        return Integer.parseInt(split[1]);
    }

    public String encodeUsingBigIntegerToString(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return bigInteger.toString(16);
    }

    private byte[] decodeUsingBigInteger(String hexString) {
        byte[] byteArray = new BigInteger(hexString, 16)
                .toByteArray();
        if (byteArray[0] == 0) {
            byte[] output = new byte[byteArray.length - 1];
            System.arraycopy(
                    byteArray, 1, output,
                    0, output.length);
            return output;
        }
        return byteArray;
    }

}