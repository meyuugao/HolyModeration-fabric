package me.yuugao.holymoderation.client.manager;

import static me.yuugao.holymoderation.client.manager.ChatManager.clientMessage;
import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.util.service.ServiceLocator;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class SoundManager {
    public static void playSound(String soundName, int volume) {
        if (ServiceLocator.getConfigManager().getConfig().soundsEnabled) {
            try {
                Path soundPath = Paths.get("C:\\HolyModeration\\Sounds", soundName);
                byte[] audioData = Files.readAllBytes(soundPath);

                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioData)));

                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float minGain = gainControl.getMinimum();
                    float maxGain = gainControl.getMaximum();
                    float gain = minGain + (volume / 100.0f) * (maxGain - minGain);
                    gainControl.setValue(gain);
                }

                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                clientMessage(RED + BOLD + "Исключение в HolyModeration/playSound: " + e);
            }
        }
    }
}