package me.yuugao.holymoderation.client.util.service.config.impl;

import me.yuugao.holymoderation.client.util.service.config.Config;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ApiConfig extends Config {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final String currentVersion = "2.10alpha"; //tip: измени перед релизом если нужно
    @Expose
    @Setter
    private String apiToken = StringUtils.EMPTY;
    @Expose
    private String vk = StringUtils.EMPTY;
    @Expose
    private String lastVkUpdate;
    @Expose
    private Map<String, Object> journalProfile = new HashMap<>();
    @Expose
    private String lastJournalProfileUpdate;
    @Expose
    private Map<String, Object> journalStats = new HashMap<>();
    @Expose
    private String lastJournalStatsUpdate;

    public ApiConfig() {
        super("api");
    }

    public void setVk(String vk) {
        this.vk = vk;
        this.lastVkUpdate = DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    public void setJournalProfile(Map<String, Object> journalProfile) {
        this.journalProfile = journalProfile;
        this.lastJournalProfileUpdate = DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    public void setJournalStats(Map<String, Object> journalState) {
        this.journalStats = journalState;
        this.lastJournalStatsUpdate = DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }
}