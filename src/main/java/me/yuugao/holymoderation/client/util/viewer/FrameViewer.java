package me.yuugao.holymoderation.client.util.viewer;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.NetService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FrameViewer {
    private static final Path VIEWER_DIR = Path.of(System.getProperty("user.home"), "HolyModeration", "Viewer");
    private static final Path VIEWER_JAR = VIEWER_DIR.resolve("Viewer.jar");
    private final LoggerService loggerService;
    private final NetService netService;

    public void open() {
        try {
            Files.createDirectories(VIEWER_DIR);
            if (!Files.exists(VIEWER_JAR)) {
                if (!netService.downloadViewerJar(VIEWER_JAR).get()) {
                    loggerService.info("Скачивание проигрывателя провалилось!");
                    return;
                }
            }

            runViewer();
        } catch (IOException | ExecutionException | InterruptedException e) {
            loggerService.exception("Исключение в FrameViewer/open: %s".formatted(e));
        }
    }

    private void runViewer() throws IOException {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java").toString();

        new ProcessBuilder(javaBin, "-jar", VIEWER_JAR.toAbsolutePath().toString())
                .directory(VIEWER_DIR.toFile())
                .start();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}