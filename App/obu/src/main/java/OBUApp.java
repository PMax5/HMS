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
    private static final String BULK_DATA_SUBMIT_COMMAND = "bulkdatasubmit";
    private static final String ASYNC_BULK_DATA_SUBMIT_COMMAND = "asyncbulkdatasubmit";
    private static final String BULK_LOGIN_USER_COMMAND = "bulklogin";
    private static final String BULK_END_SHIFT_COMMAND = "bulkendshift";

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
                    System.out.println("[OBU Service] Running command: " + command);
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
                            case BULK_DATA_SUBMIT_COMMAND -> obuService.bulkSubmitUserData(
                                    Integer.parseInt(command.get(1)));
                            case ASYNC_BULK_DATA_SUBMIT_COMMAND -> obuService.asyncBulkSubmitUserData(
                                    Integer.parseInt(command.get(1))
                            );
                            case BULK_LOGIN_USER_COMMAND -> obuService.bulkLoginUser(
                                    command.get(1),
                                    command.get(2),
                                    Integer.parseInt(command.get(3))
                            );
                            case BULK_END_SHIFT_COMMAND -> obuService.bulkEndShift(
                                    Integer.parseInt(command.get(1))
                            );
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
            e.printStackTrace();
            System.err.println("[OBU Service] Something went wrong with the OBU service: " + e.getMessage());
            System.exit(-1);
        }
    }
}
