package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Singleton;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Singleton
public class HudOverrideService {
    private String selectedId;
}
