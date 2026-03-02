package me.yuugao.holymoderation.client.util.serviceLocator.service;


import me.yuugao.holymoderation.client.config.SettingsConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class SoundService extends Service {
    public void playSound(String soundName) {
        ConfigManager configManager = ServiceLocator.getConfigManager();

        SettingsConfig settingsConfig = configManager.getSettingsConfig();

        if (settingsConfig.isSoundsEnabled()) {
            try {
                Path soundPath = Paths.get(System.getProperty("user.home"), "HolyModeration", "Config", soundName);
                byte[] audioData = Files.readAllBytes(soundPath);

                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData)));

                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float minGain = gainControl.getMinimum();
                    float maxGain = gainControl.getMaximum();
                    float gain = minGain + (settingsConfig.getSoundsVolume() / 100.0f) * (maxGain - minGain);
                    gainControl.setValue(gain);
                }

                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                loggerService.exception("Исключение в SoundService/playSound: %s".formatted(e));
            }
        }
    }
}