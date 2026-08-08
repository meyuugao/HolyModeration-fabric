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
    /**
     * Independent hardware fingerprints, each SHA-256 of a single stable source.
     * Sent to the backend so a ban can match on ANY one of them — surviving partial
     * hardware swaps (replace 4 of 5 components, the remaining 1 still triggers a ban).
     */
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

            // 1. Motherboard SMBIOS UUID
            addFingerprint("mb", cs.getHardwareUUID());

            // 2. Baseboard serial number
            if (cs.getBaseboard() != null) {
                addFingerprint("bb", cs.getBaseboard().getSerialNumber());
            }

            // 3. CPU identifier (stable across OS reinstalls)
            CentralProcessor cpu = hal.getProcessor();
            if (cpu != null && cpu.getProcessorIdentifier() != null) {
                addFingerprint("cpu", cpu.getProcessorIdentifier().getProcessorID());
            }

            // 4. First non-removable disk serial (system drive typically)
            List<HWDiskStore> disks = hal.getDiskStores();
            if (disks != null) {
                for (HWDiskStore disk : disks) {
                    String name = disk.getName() == null ? StringUtils.EMPTY : disk.getName().toLowerCase();
                    if (name.contains("usb")) continue;
                    if (addFingerprint("disk", disk.getSerial())) break;
                }
            }

            // 5. First physical NIC MAC address
            List<NetworkIF> nics = hal.getNetworkIFs();
            if (nics != null) {
                for (NetworkIF nic : nics) {
                    String mac = nic.getMacaddr();
                    // skip loopback / virtual adapters that often report empty or 00:00:00:00:00:00
                    if (!isUsable(mac) || mac.replace("0", "").replace(":", "").isEmpty()) continue;
                    if (addFingerprint("nic", mac)) break;
                }
            }
        } catch (Exception ignored) {
            // partial collection is still usable; whatever was gathered stays in the list
        }
    }

    /**
     * Hashes a raw value and appends to the list. Returns true if a usable value was added.
     */
    private boolean addFingerprint(String tag, String raw) {
        if (!isUsable(raw)) return false;
        String hash = Hashing.sha256().hashString(tag + ":" + raw, StandardCharsets.UTF_8).toString();
        if (!hwids.contains(hash)) {
            hwids.add(hash);
            return true;
        }
        return false;
    }

    /**
     * Unmodifiable view of all fingerprints.
     */
    public List<String> getHwidsView() {
        return Collections.unmodifiableList(hwids);
    }
}
