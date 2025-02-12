package xyz.article;

public class WhenClose {
    public WhenClose() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

        }));
    }
}
