package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.hash.Hashing;
import lombok.Getter;
import oshi.SystemInfo;
import oshi.hardware.*;

@Getter
@Singleton
public class HwidService {
    private final List<String> hwids = new ArrayList<>();

    private static boolean isUsable(String value) {
        return value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value.trim());
    }

    public void calculateHwid() {
        hwids.clear();
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            ComputerSystem cs = hal.getComputerSystem();

            addFingerprint("mb", cs.getHardwareUUID());

            if (cs.getBaseboard() != null) {
                addFingerprint("bb", cs.getBaseboard().getSerialNumber());
            }

            CentralProcessor cpu = hal.getProcessor();
            if (cpu != null && cpu.getProcessorIdentifier() != null) {
                addFingerprint("cpu", cpu.getProcessorIdentifier().getProcessorID());
            }


            List<HWDiskStore> disks = hal.getDiskStores();
            if (disks != null) {
                for (HWDiskStore disk : disks) {
                    String name = disk.getName() == null ? StringUtils.EMPTY : disk.getName().toLowerCase();
                    if (name.contains("usb")) continue;
                    if (addFingerprint("disk", disk.getSerial())) break;
                }
            }

            List<NetworkIF> nics = hal.getNetworkIFs();
            if (nics != null) {
                for (NetworkIF nic : nics) {
                    String mac = nic.getMacaddr();

                    if (!isUsable(mac) || mac.replace("0", "").replace(":", "").isEmpty()) continue;
                    if (addFingerprint("nic", mac)) break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean addFingerprint(String tag, String raw) {
        if (!isUsable(raw)) return false;
        String hash = Hashing.sha256().hashString(tag + ":" + raw, StandardCharsets.UTF_8).toString();
        if (!hwids.contains(hash)) {
            hwids.add(hash);
            return true;
        }
        return false;
    }

    public List<String> getHwidsView() {
        return Collections.unmodifiableList(hwids);
    }
}
