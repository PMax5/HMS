import exceptions.OBUException;
import services.OBUService;
import services.TestsService;

import java.io.IOException;
import java.util.List;

public class OBUApp {
    private static final String LOGIN_USER_COMMAND = "login";
    private static final String LOGOUT_USER_COMMAND = "logout";
    private static final String START_SHIFT_COMMAND = "startshift";
    private static final String END_SHIFT_COMMAND = "endshift";
    private static final String DATA_COMMAND = "data";
    private static final String DATA_SUBMIT_COMMAND = "datasubmit";

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
            TestsService testsService = new TestsService();

            List<List<String>> testCommands = testsService.getNextTestCommands();
            while(testCommands != null) {
                testsService.createOutputFile();
                for (List<String> command: testCommands) {
                    try {
                        switch (command.get(0)) {
                            case LOGIN_USER_COMMAND -> obuService.loginUser(command.get(1), command.get(2));
                            case LOGOUT_USER_COMMAND -> obuService.logoutUser();
                            case START_SHIFT_COMMAND -> obuService.startShift(Integer.parseInt(command.get(1)),
                                    Integer.parseInt(command.get(2)));
                            case END_SHIFT_COMMAND -> obuService.endShift();
                            case DATA_COMMAND -> {
                                obuService.addBpm(Integer.parseInt(command.get(1)));
                                obuService.addDrowsiness(Integer.parseInt(command.get(2)));
                                obuService.addSpeed(Integer.parseInt(command.get(3)));
                                obuService.addTimestamp(Integer.parseInt(command.get(4)));
                            }
                            case DATA_SUBMIT_COMMAND -> obuService.submitUserData();
                        }
                    } catch(OBUException e) {
                        testsService.writeToOutputFile("[OBU Service Exception] " + e.getMessage());
                    }
                }

                testCommands = testsService.getNextTestCommands();
            }

            obuService.close();
        } catch (IOException e) {
            System.err.println("[OBU Service] Error with files: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[OBU Service] Failed to shutdown OBU service: " + e.getMessage());
        }
    }
}
