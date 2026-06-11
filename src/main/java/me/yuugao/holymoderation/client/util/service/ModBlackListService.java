package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ModBlackListService {
    private final NetService netService;
    private final AsyncExecutor asyncExecutor;

    private final Map<String, Set<String>> playerToHwids = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> hwidToPlayers = new ConcurrentHashMap<>();

    public CompletableFuture<Void> updateLists() {
        return asyncExecutor.runAsync("ModBlackListService/updateList", () -> {
            List<AbstractMap.SimpleEntry<String, String>> list = netService.getBanLists();
            if (list == null) return;

            Map<String, Set<String>> newPlayerToHwids = new ConcurrentHashMap<>();
            Map<String, Set<String>> newHwidToPlayers = new ConcurrentHashMap<>();

            for (var entry : list) {
                String player = entry.getKey();
                String hwid = entry.getValue();

                if (player == null || hwid == null) continue;

                newPlayerToHwids
                        .computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet())
                        .add(hwid);

                newHwidToPlayers
                        .computeIfAbsent(hwid, k -> ConcurrentHashMap.newKeySet())
                        .add(player);
            }

            playerToHwids.clear();
            playerToHwids.putAll(newPlayerToHwids);

            hwidToPlayers.clear();
            hwidToPlayers.putAll(newHwidToPlayers);
        });
    }

    public boolean isBannedByNickname(String nickname) {
        return playerToHwids.containsKey(nickname);
    }

    public boolean isBannedByHwid(String hwid) {
        return hwidToPlayers.containsKey(hwid);
    }
}