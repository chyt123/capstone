import org.apache.commons.daemon.support.DaemonLoader;

public class TestApplication {
    public static void main(String[] args) throws Exception {
        DaemonLoader.load("ycheng.service.Application", args);
        DaemonLoader.start();
    }
}
