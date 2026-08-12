package me.yuugao.holymoderation.client.util.service.state;

import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Singleton
public class PlayerStateService {
    private String checkoutPlayer = StringUtils.EMPTY;
    private String spyPlayer = StringUtils.EMPTY;
    private String spyPlayerStatus = StringUtils.EMPTY;
    private String spyPlayerActivity = StringUtils.EMPTY;

    public void reset() {
        this.checkoutPlayer = StringUtils.EMPTY;
        this.spyPlayer = StringUtils.EMPTY;
        this.spyPlayerStatus = StringUtils.EMPTY;
        this.spyPlayerActivity = StringUtils.EMPTY;
    }
}
