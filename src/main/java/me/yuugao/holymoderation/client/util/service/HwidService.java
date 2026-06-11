package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

@Getter
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class HwidService {
    private String hwid = StringUtils.EMPTY;

    public void calculateHwid() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            ComputerSystem computerSystem = systemInfo.getHardware().getComputerSystem();
            String hardwareUuid = computerSystem.getHardwareUUID();
            if (hardwareUuid != null && !hardwareUuid.isEmpty() && !"unknown".equalsIgnoreCase(hardwareUuid)) {
                hwid = hardwareUuid;
            } else {
                hwid = StringUtils.EMPTY;
            }
        } catch (Throwable t) {
            hwid = t.toString();
        }
    }
}