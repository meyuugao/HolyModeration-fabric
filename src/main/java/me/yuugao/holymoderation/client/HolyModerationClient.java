package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.manager.SoundManager;
import me.yuugao.holymoderation.client.util.State;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class HolyModerationClient implements ClientModInitializer {
    public static final Config CONFIG = new Config();
    public static final State STATE = new State();
    public static final EventBus EVENT_BUS = new EventBus();

    private static final Logger LOGGER = LoggerFactory.getLogger("HolyModeration/Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Registering resource reload listener for shader initialization.");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            private final Identifier id = new Identifier("holymoderation", "shader_loader");

            @Override
            public Identifier getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
                LOGGER.info("Starting shader reload prepare phase.");
                return CompletableFuture.completedFuture(null)
                        .thenCompose(synchronizer::whenPrepared)
                        .thenRunAsync(() -> {
                            try {
                                //tip:init with manager
                                //LOGGER.info("Shader loaded in apply phase successfully.");
                            } catch (Exception e) {
                                //LOGGER.error("Failed to initialize shader in apply phase: {}", e.getMessage(), e);
                            }
                        }, applyExecutor);
            }
        });
        HudRenderCallback.EVENT.register((drawContext, delta) -> {
            //LOGGER.debug("Rendering HUD with rounded rect");
        });

        registerEventListeners();
    }

    private void registerEventListeners() {
        EVENT_BUS.register(new SoundManager());
    }
}