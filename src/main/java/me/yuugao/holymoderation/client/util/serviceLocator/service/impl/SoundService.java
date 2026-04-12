package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;


import me.yuugao.holymoderation.client.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

import lombok.Getter;

@Getter
public class SoundService extends Service {
    private final Path soundsDir = Paths.get(System.getProperty("user.home"), "HolyModeration", "Sounds");

    public void playSound(String soundName) {
        ConfigManager configManager = ServiceLocator.getConfigManager();

        SettingsConfig settingsConfig = configManager.getSettingsConfig();

        if (settingsConfig.isSoundsEnabled()) {
            try {

                byte[] audioData = Files.readAllBytes(soundsDir.resolve(soundName));

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