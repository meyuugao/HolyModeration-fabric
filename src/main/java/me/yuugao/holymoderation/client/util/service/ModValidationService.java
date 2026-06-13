package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ModValidationService {
    private final NetService netService;
    private final LoggerService loggerService;

    private volatile Map<String, Set<String>> whiteListHwidToPlayers = Map.of();
    private volatile Map<String, Set<String>> whiteListPlayerToHwids = Map.of();

    private volatile Map<String, Set<String>> blackListHwidToPlayers = Map.of();
    private volatile Map<String, Set<String>> blackListPlayerToHwids = Map.of();

    public CompletableFuture<Void> updateLists() {
        return CompletableFuture.allOf(
                netService.getWhiteList().thenAccept(this::updateWhiteList),
                netService.getBlackList().thenAccept(this::updateBlackList)
        ).exceptionally(throwable -> {
            loggerService.info("Исключение в ModValidationService/updateLists: %s".formatted(throwable));
            return null;
        });
    }

    private void updateWhiteList(List<AbstractMap.SimpleEntry<String, String>> source) {
        whiteListHwidToPlayers = buildHwidMap(source);
        whiteListPlayerToHwids = buildPlayerMap(source);
    }

    private void updateBlackList(List<AbstractMap.SimpleEntry<String, String>> source) {
        blackListHwidToPlayers = buildHwidMap(source);
        blackListPlayerToHwids = buildPlayerMap(source);
    }

    private Map<String, Set<String>> buildHwidMap(List<AbstractMap.SimpleEntry<String, String>> source) {
        return buildMap(source, AbstractMap.SimpleEntry::getValue, AbstractMap.SimpleEntry::getKey);
    }

    private Map<String, Set<String>> buildPlayerMap(List<AbstractMap.SimpleEntry<String, String>> source) {
        return buildMap(source, AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue);
    }

    private Map<String, Set<String>> buildMap(List<AbstractMap.SimpleEntry<String, String>> source,
                                              Function<AbstractMap.SimpleEntry<String, String>, String> keyExtractor,
                                              Function<AbstractMap.SimpleEntry<String, String>, String> valueExtractor) {
        Map<String, Set<String>> map = new HashMap<>();

        for (var entry : source) {
            String key = keyExtractor.apply(entry);
            String value = valueExtractor.apply(entry);

            if (key == null || value == null) continue;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        }

        return map;
    }

    public boolean isWhiteListEnabled() {
        return !whiteListHwidToPlayers.isEmpty() || !whiteListPlayerToHwids.isEmpty();
    }

    public boolean isAllowedByHwid(String hwid) {
        return whiteListHwidToPlayers.containsKey(hwid);
    }

    public boolean isAllowedByNickname(String nickname) {
        return whiteListPlayerToHwids.containsKey(nickname);
    }

    public boolean isBannedByHwid(String hwid) {
        return blackListHwidToPlayers.containsKey(hwid);
    }

    public boolean isBannedByNickname(String nickname) {
        return blackListPlayerToHwids.containsKey(nickname);
    }
}