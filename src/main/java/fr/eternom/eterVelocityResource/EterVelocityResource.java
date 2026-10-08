package fr.eternom.eterVelocityResource;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.eternom.eterVelocityLib.EterVelocityLib;
import fr.eternom.eterVelocityLib.core.Config;
import fr.eternom.eterVelocityLib.helper.Messages;
import fr.eternom.eterVelocityLib.orchestrator.PoolCommand;
import fr.eternom.eterVelocityLib.orchestrator.ServerPool;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * EterVelocityResource : les serveurs « monde ressource » jetables (famille « eterresource » de l'orchestrateur
 * d'EterVelocityLib). Chaque nouveau serveur génère un monde neuf, remplacé au bout de max-lifetime-hours. Les joueurs
 * d'un serveur qu'on supprime vont sur un autre monde ressource (leur temps continue) ; s'il n'y en a pas, ils sont
 * renvoyés au lobby (EterVelocityLobby). L'accès, les règles et les bonus sont dans EterResource, côté Paper.
 */
@Plugin(id = "etervelocityresource", name = "EterVelocityResource", version = "1.0.4", authors = {"NadTum"},
        description = "Mondes ressources jetables (orchestrateur)",
        dependencies = {@Dependency(id = "etervelocitylib")})
public final class EterVelocityResource {

    public static final String PERMISSION = "etervelocityresource.admin";

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private ServerPool pool;

    @Inject
    public EterVelocityResource(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        Config config;
        Messages messages;
        try {
            config = new Config(EterVelocityResource.class, dataDirectory);
            messages = EterVelocityLib.get().messages(EterVelocityResource.class, dataDirectory, logger);
        } catch (IOException | RuntimeException e) {
            // Sans le détail : une erreur YAML recopie la ligne fautive, qui peut être une clé du panel
            logger.error("config.yml illisible (vérifie la syntaxe YAML), EterVelocityResource désactivé");
            return;
        }
        if (!config.getBoolean("orchestrator.enabled", false)) {
            logger.info("Orchestrateur des mondes ressources désactivé (orchestrator.enabled)");
            return;
        }
        // Pas de repli propre : un autre monde ressource (choisi par le moteur), sinon renvoi au lobby à la suppression
        pool = new ServerPool(this, proxy, logger, config, dataDirectory, "eterresource",
                List.of("eter_servers", "eterresource_worlds"), except -> Optional.empty());
        CommandManager commands = proxy.getCommandManager();
        commands.register(commands.metaBuilder("eterresourcepool").plugin(this).build(),
                new PoolCommand(pool, messages, "eterresourcepool", PERMISSION));
        pool.start();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (pool != null) {
            pool.stop();
        }
    }
}
