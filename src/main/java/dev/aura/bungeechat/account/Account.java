package dev.aura.bungeechat.account;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dev.aura.bungeechat.api.account.BungeeChatAccount;
import dev.aura.bungeechat.api.enums.ChannelType;
import dev.aura.bungeechat.permission.Permission;
import dev.aura.bungeechat.permission.PermissionManager;
import dev.aura.bungeechat.util.DummyPlayer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

@Data
@EqualsAndHashCode(of = "uuid")
public class Account implements BungeeChatAccount {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID uuid;
    @Getter(lazy = true)
    private final ProxiedPlayer proxiedPlayer = (ProxiedPlayer) BungeecordAccountManager.getCommandSender(this)
            .orElse(new DummyPlayer(uuid));
    private ChannelType channelType;
    private boolean vanished;
    @Getter(AccessLevel.NONE)
    private boolean messanger;
    @Getter(AccessLevel.NONE)
    private boolean socialSpy;
    @Getter(AccessLevel.NONE)
    private boolean localSpy;
    private final BlockingQueue<UUID> ignored;
    private Timestamp mutedUntil;
    private Optional<String> storedPrefix;
    private Optional<String> storedSuffix;

    protected Account(ProxiedPlayer player) {
        this(player.getUniqueId());
    }

    protected Account(UUID uuid) {
        this.uuid = uuid;
        channelType = ChannelType.LOCAL;
        vanished = false;
        messanger = true;
        socialSpy = false;
        localSpy = false;
        ignored = new LinkedBlockingQueue<>();
        mutedUntil = new Timestamp(0);
        storedPrefix = Optional.empty();
        storedSuffix = Optional.empty();
    }

    protected Account(UUID uuid, ChannelType channelType, boolean vanished, boolean messanger, boolean socialSpy,
            boolean localSpy, BlockingQueue<UUID> ignored, Timestamp mutedUntil, Optional<String> storedPrefix,
            Optional<String> storedSuffix) {
        this.uuid = uuid;
        this.channelType = channelType;
        this.vanished = vanished;
        this.messanger = messanger;
        this.socialSpy = socialSpy;
        this.localSpy = localSpy;
        this.ignored = ignored;
        this.mutedUntil = mutedUntil;
        this.storedPrefix = storedPrefix;
        this.storedSuffix = storedSuffix;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean hasMessangerEnabled() {
        return messanger;
    }

    @Override
    public boolean hasSocialSpyEnabled() {
        if (socialSpy && !PermissionManager.hasPermission(this, Permission.COMMAND_SOCIALSPY)) {
            setSocialSpy(false);
        }

        return socialSpy;
    }

    @Override
    public boolean hasLocalSpyEnabled() {
        if (localSpy && !PermissionManager.hasPermission(this, Permission.COMMAND_LOCALSPY)) {
            setLocalSpy(false);
        }

        return localSpy;
    }

    public boolean hasIgnored(ProxiedPlayer player) {
        return hasIgnored(player.getUniqueId());
    }

    @Override
    public void addIgnore(UUID uuid) {
        ignored.add(uuid);
    }

    public void addIgnore(ProxiedPlayer player) {
        this.addIgnore(player.getUniqueId());
    }

    @Override
    public void removeIgnore(UUID uuid) {
        ignored.remove(uuid);
    }

    public void removeIgnore(ProxiedPlayer player) {
        this.removeIgnore(player.getUniqueId());
    }

    @Override
    public String getName() {
        return getProxiedPlayer().getName();
    }

    @Override
    public String getDisplayName() {
        return getProxiedPlayer().getDisplayName();
    }

    @Override
    public int getPing() {
        return getProxiedPlayer().getPing();
    }

    @Override
    public String getServerName() {
        try {
            return getServerInfo().getName();
        } catch (NullPointerException e) {
            return "unknown";
        }
    }

    @Override
    public String getServerIP() {
        try {
            return getServerInfo().getAddress().toString();
        } catch (NullPointerException e) {
            return "unknown";
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private ServerInfo getServerInfo() {
        ProxiedPlayer player = getProxiedPlayer();
        Server server = player.getServer();

        if (server == null)
            return player.getReconnectServer();
        else
            return server.getInfo();
    }
}
