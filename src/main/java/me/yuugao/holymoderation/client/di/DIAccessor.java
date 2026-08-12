package me.yuugao.holymoderation.client.di;

public class DIAccessor {
    private static DIContainer di;

    public static void initialize(DIContainer container) {
        di = container;
    }

    public static DIContainer getDI() {
        return di;
    }
}