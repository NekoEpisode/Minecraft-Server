package xyz.article.api.entity.player;

import java.util.EnumSet;

public enum PlayerInfoAction {
    ADD_PLAYER(0x01),
    INITIALIZE_CHAT(0x02),
    UPDATE_GAME_MODE(0x04),
    UPDATE_LISTED(0x08),
    UPDATE_LATENCY(0x10),
    UPDATE_DISPLAY_NAME(0x20),
    UPDATE_LIST_PRIORITY(0x40),
    UPDATE_HAT(0x80);

    private final int mask;

    PlayerInfoAction(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public static EnumSet<PlayerInfoAction> fromMask(int mask) {
        EnumSet<PlayerInfoAction> actions = EnumSet.noneOf(PlayerInfoAction.class);
        for (PlayerInfoAction action : values()) {
            if ((mask & action.getMask()) != 0) {
                actions.add(action);
            }
        }
        return actions;
    }

    public static int toMask(EnumSet<PlayerInfoAction> actions) {
        int mask = 0;
        for (PlayerInfoAction action : actions) {
            mask |= action.getMask();
        }
        return mask;
    }
}