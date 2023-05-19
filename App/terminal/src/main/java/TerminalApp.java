import exceptions.TerminalException;
import services.TerminalService;
import services.TestsService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TerminalApp {
    private static final String LOGIN_USER_COMMAND = "login";
    private static final String LOGOUT_USER_COMMAND = "logout";
    private static final String REGISTER_USER_COMMAND = "registerUser";
    private static final String REGISTER_PROFILE_COMMAND = "registerProfile";
    private static final String GET_PROFILES_COMMAND = "getProfiles";
    private static final String SET_USER_PROFILE_COMMAND = "setUserProfile";

    public static void main(String[] args) {
        System.out.println("Terminal Initializing...");
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        TerminalService terminalService = new TerminalService(host, port);

        try {
            TestsService testsService = new TestsService();

            List<List<String>> testCommands = testsService.getNextTestCommands();
            while(testCommands != null) {
                testsService.createOutputFile();
                for (List<String> command: testCommands) {
                    System.out.println("[OBU Service] Running command: " + command);
                    try {
                        switch (command.get(0)) {
                            case LOGIN_USER_COMMAND -> terminalService.loginUser(command.get(1), command.get(2));
                            case LOGOUT_USER_COMMAND -> terminalService.logoutUser();
                            case REGISTER_USER_COMMAND -> terminalService.registerUser(
                                    command.get(1),
                                    command.get(2),
                                    Integer.parseInt(command.get(3)),
                                    command.get(4),
                                    command.get(5),
                                    command.get(6)
                            );
                            case REGISTER_PROFILE_COMMAND -> terminalService.registerProfile(
                                    Integer.parseInt(command.get(1)),
                                    Integer.parseInt(command.get(2)),
                                    command.get(3),
                                    Integer.parseInt(command.get(4)),
                                    Integer.parseInt(command.get(5)),
                                    Arrays.asList(command.get(6).split(",")),
                                    Arrays.stream(command.get(7).split(",")).map(Integer::parseInt)
                                            .collect(Collectors.toList()),
                                    Arrays.asList(command.get(8).split(",")),
                                    Integer.parseInt(command.get(9))
                            );
                            case GET_PROFILES_COMMAND -> terminalService.getProfiles();
                            case SET_USER_PROFILE_COMMAND -> terminalService.setUserProfile(
                                    command.get(1),
                                    command.get(2)
                            );
                        }
                    } catch(TerminalException e) {
                        testsService.writeToOutputFile("[OBU Service Exception] " + e.getMessage());
                    }
                }

                testCommands = testsService.getNextTestCommands();
            }

            terminalService.close();
        } catch (IOException e) {
            System.err.println("[OBU Service] Error with files: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[OBU Service] Something went wrong with the OBU service: " + e.getMessage());
            System.exit(-1);
        }
    }
}
