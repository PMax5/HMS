import services.OBUService;

public class OBUApp {
    public static void main(String[] args) {
        System.out.println("On Board Unit Initializing...");
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        OBUService obuService = new OBUService(host, port);

        try {
            obuService.close();
        } catch (Exception e) {
            System.err.println("[OBU Service] Failed to shutdown OBU service: " + e.getMessage());
        }
    }
}
