import models.DataLog;
import models.ShiftLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import services.DataService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataTest extends BaseTest {

    private final DataService dataService = this.initService();
    private static final String USERNAME = "driver1";

    @Test
    public void startShift() {
        String shiftId = dataService.startShift(USERNAME, 778, 2488);
        dataService.removeActiveShift(USERNAME);

        assertNotNull(shiftId);
    }

    @Test
    public void startShiftWithInvalidRoute() {
        String shiftId = dataService.startShift(USERNAME, 799, 2488);
        dataService.removeActiveShift(USERNAME);

        assertNull(shiftId);
    }

    @Test
    public void startShiftWithInvalidVehicle() {
        String shiftId = dataService.startShift(USERNAME, 778, 4567);
        dataService.removeActiveShift(USERNAME);

        assertNull(shiftId);
    }

    @Test
    public void submitShiftData() {
        String shiftId = dataService.startShift(USERNAME, 778, 2488);
        DataLog dataLog = dataService.submitUserData(
                USERNAME,
                778,
                2488,
                Arrays.asList(100, 120),
                Arrays.asList(50, 50),
                Arrays.asList(40, 40),
                Arrays.asList(1681730915L, 1681730916L)
        );

        dataService.removeActiveShift(USERNAME);

        assertNotNull(shiftId);
        assertEquals(dataLog.getUserId(), USERNAME);
        assertEquals(dataLog.getRouteId(), 778);
        assertEquals(dataLog.getVehicleId(), 2488);
    }

    @Test
    public void submitShiftDataWithWrongVBS() {
        List<Integer> bpmValues = Arrays.asList(250, 120);
        List<Integer> drowsinessValues = Arrays.asList(500, 50);
        List<Integer> speedValues = Arrays.asList(300, 40);
        List<Long> timestampValues = Arrays.asList(1681730915L, 1681730916L);

        String shiftId = dataService.startShift(USERNAME, 778, 2488);
        DataLog dataLog = dataService.submitUserData(
                USERNAME,
                778,
                2488,
                bpmValues,
                drowsinessValues,
                speedValues,
                timestampValues
        );

        dataService.removeActiveShift(USERNAME);

        assertNotNull(shiftId);
        assertEquals(dataLog.getUserId(), USERNAME);
        assertEquals(dataLog.getRouteId(), 778);
        assertEquals(dataLog.getVehicleId(), 2488);
        assertEquals(dataLog.getBpmValues(), Collections.singletonList(120));
        assertEquals(dataLog.getDrowsinessValues(), Collections.singletonList(50));
        assertEquals(dataLog.getSpeedValues(), Collections.singletonList(40));
        assertEquals(dataLog.getTimestampValues(), Collections.singletonList(1681730916L));
    }

    @Test
    public void getDataLogs() {
        List<DataLog> dataLogs = dataService.getDataLogsForUser(USERNAME);

        assertNotNull(dataLogs);
    }

    @Test
    public void getDataLogsWithWrongUsername() {
        List<DataLog> dataLogs = dataService.getDataLogsForUser("driver3");

        assertEquals(dataLogs, new ArrayList<>());
    }

    @Test
    public void getStatisticsShiftLogs() {
        List<ShiftLog> shiftLogs = dataService.getShiftLogsForUser(USERNAME);

        assertNotNull(shiftLogs);
    }

    @Test
    public void getStatisticsShiftLogsWithWrongUsername() {
        List<ShiftLog> shiftLogs = dataService.getShiftLogsForUser("driver3");

        assertEquals(shiftLogs, new ArrayList<>());
    }

    @Test
    public void endShiftWithNoData() {
        String shiftId = dataService.startShift(USERNAME, 778, 2488);

        ShiftLog shiftLog = dataService.endShift(USERNAME);
        dataService.removeActiveShift(USERNAME);

        assertNotNull(shiftId);
        assertNull(shiftLog);
    }

    @Test
    public void endShift() {
        String shiftId = dataService.startShift(USERNAME, 778, 2488);

        List<Integer> bpmValues = Arrays.asList(120, 120);
        List<Integer> drowsinessValues = Arrays.asList(50, 50);
        List<Integer> speedValues = Arrays.asList(40, 40);
        List<Long> timestampValues = Arrays.asList(1681730915L, 1681730916L);

        DataLog dataLog = dataService.submitUserData(
                USERNAME,
                778,
                2488,
                bpmValues,
                drowsinessValues,
                speedValues,
                timestampValues
        );

        ShiftLog shiftLog = dataService.endShift(USERNAME);
        dataService.removeActiveShift(USERNAME);

        assertNotNull(shiftId);
        assertEquals(shiftLog.getShiftId(), shiftId);
        assertEquals(shiftLog.getUserId(), USERNAME);
        assertEquals(shiftLog.getRouteId(), 778);
        assertEquals(shiftLog.getVehicleId(), 2488);
    }

    @Test
    public void endShiftWithInvalidUsername() {
        String shiftId = dataService.startShift(USERNAME, 778, 2488);
        dataService.removeActiveShift(USERNAME);

        ShiftLog shiftLog = dataService.endShift(USERNAME);


        assertNotNull(shiftId);
        assertNull(shiftLog);
    }
}
