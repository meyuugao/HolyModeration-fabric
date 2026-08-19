package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Getter
@Singleton
public class SoundService {
    private final ConfigManagerService configManagerService;
    private final LoggerService loggerService;

    private final Path soundsDir = Paths.get(System.getProperty("user.home"), "HolyModeration", "Sounds");

    public void playSound(String soundName) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

        if (settingsConfig.isSoundsEnabled()) {
            try {
                String custom = settingsConfig.getCustomSounds().get(soundName);
                Path path = (custom != null && !custom.isBlank())
                        ? Paths.get(custom)
                        : soundsDir.resolve(soundName);

                if (!Files.exists(path)) {
                    path = soundsDir.resolve(soundName);
                }
                if (!Files.exists(path)) {
                    return;
                }

                byte[] audioData = Files.readAllBytes(path);
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
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                loggerService.exception("Исключение в SoundService/playSound: %s".formatted(e));
            }
        }
    }
}