import models.Profile;
import models.ShiftLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import services.ProfilerService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfilerTest extends BaseTest {

    private final ProfilerService profilerService = this.initService();
    private final List<Profile> profiles = new ArrayList<>();
    private static final String USERNAME = "driver1";
    private static String NORMAL_PROFILE;
    private static String STRESSFUL_PROFILE;
    private static String STRESSFUL_N_PROFILE;

    @BeforeAll
    public void registerProfiles() {
        Profile profile1 = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(778),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        Profile profile2 = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_MORNING"),
                Collections.singletonList(750),
                Arrays.asList("HIGH_TRAFFIC", "THIN_ROADS", "CRIMINAL_AREA"),
                1
        );

        Profile profile3 = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(750),
                Arrays.asList("HIGH_TRAFFIC", "THIN_ROADS", "CRIMINAL_AREA"),
                1
        );

        NORMAL_PROFILE = profile1.getId();
        STRESSFUL_PROFILE = profile2.getId();
        STRESSFUL_N_PROFILE = profile3.getId();

        profiles.add(profile1);
        profiles.add(profile2);
        profiles.add(profile3);

        System.out.println("Profile 1 --> " + profile1.getId());
        System.out.println("Profile 2 --> " + profile2.getId());
        System.out.println("Profile 3 --> " + profile3.getId());
    }

    @Test
    public void registerProfileWithWrongAge() {
        Profile profile = profilerService.registerProfile(
                15,
                70,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(778),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongGender() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                null,
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(778),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongHours() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                "MALE",
                -5,
                25,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(778),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongShift() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                null,
                Collections.singletonList(778),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongRoute() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                null,
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongRouteValue() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(799),
                Arrays.asList("LOW_TRAFFIC", "LARGE_ROADS", "REGULAR_AREA"),
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void registerProfileWithWrongRouteCharacteristics() {
        Profile profile = profilerService.registerProfile(
                25,
                30,
                "MALE",
                8,
                12,
                Collections.singletonList("SHIFT_NIGHT"),
                Collections.singletonList(778),
                null,
                1
        );

        if (profile != null) {
            profiles.add(profile);
        }

        assertNull(profile);
    }

    @Test
    public void setUserProfile() {
        boolean result = profilerService.setProfile("driver1", profiles.get(1).getId());

        assertTrue(result);
    }

    @Test
    public void setUserProfileWithInvalidId() {
        boolean result = profilerService.setProfile("driver1", "thisisaninvalidid");

        assertFalse(result);
    }

    @Test
    public void getProfiles() {
        List<Profile> profiles = profilerService.getProfiles();

        assertNotNull(profiles);
        assertEquals(profiles.size(), this.profiles.size());
    }

    @Test
    public void keepProfileWhenSupposedTo() {
        String uuid = UUID.randomUUID().toString();
        ShiftLog shiftLog = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                50,
                50,
                1681730911L
        );

        ShiftLog shiftLog1 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                30,
                50,
                1681730912L
        );

        ShiftLog shiftLog2 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                30,
                50,
                1681730913L
        );

        ShiftLog shiftLog3 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                30,
                50,
                1681730914L
        );

        try {
            profilerService.setProfile(USERNAME, STRESSFUL_PROFILE);
            profilerService.analyizeDriverData(USERNAME, uuid, Arrays.asList(shiftLog, shiftLog1, shiftLog2, shiftLog3));
            Profile profile = profilerService.getUserProfile(USERNAME, profiles);

            assertEquals(profile.getId(), STRESSFUL_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changeProfileWhenSupposedTo() {
        String uuid = UUID.randomUUID().toString();
        ShiftLog shiftLog = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730911L
        );

        ShiftLog shiftLog1 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730912L
        );

        ShiftLog shiftLog2 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730913L
        );

        ShiftLog shiftLog3 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730914L
        );

        try {
            profilerService.setProfile(USERNAME, STRESSFUL_PROFILE);
            profilerService.analyizeDriverData(USERNAME, uuid, Arrays.asList(shiftLog, shiftLog1, shiftLog2, shiftLog3));
            Profile profile = profilerService.getUserProfile(USERNAME, profiles);

            assertEquals(profile.getId(), NORMAL_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changeRouteToLowTraffic() {
        String uuid = UUID.randomUUID().toString();
        ShiftLog shiftLog = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730911L
        );

        ShiftLog shiftLog1 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730912L
        );

        ShiftLog shiftLog2 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730913L
        );

        ShiftLog shiftLog3 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                160,
                30,
                50,
                1681730914L
        );

        try {
            profilerService.setProfile(USERNAME, STRESSFUL_N_PROFILE);
            profilerService.analyizeDriverData(USERNAME, uuid,
                    Arrays.asList(shiftLog, shiftLog1, shiftLog2, shiftLog3));
            Profile profile = profilerService.getUserProfile(USERNAME, profiles);

            assertEquals(profile.getId(), NORMAL_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changeRouteToMorningShift() {
        String uuid = UUID.randomUUID().toString();
        ShiftLog shiftLog = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                70,
                50,
                1681730911L
        );

        ShiftLog shiftLog1 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                70,
                50,
                1681730912L
        );

        ShiftLog shiftLog2 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                70,
                50,
                1681730913L
        );

        ShiftLog shiftLog3 = new ShiftLog(
                USERNAME,
                uuid,
                4245,
                750,
                100,
                70,
                50,
                1681730914L
        );

        try {
            profilerService.setProfile(USERNAME, NORMAL_PROFILE);
            profilerService.analyizeDriverData(USERNAME, uuid, Arrays.asList(shiftLog, shiftLog1, shiftLog2, shiftLog3));
            Profile profile = profilerService.getUserProfile(USERNAME, profiles);

            assertEquals(profile.getId(), STRESSFUL_PROFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public void deleteAllProfiles() {
        for (Profile profile: profiles) {
            boolean result = profilerService.deleteProfileById(profile.getId());

            if (!result) {
                System.err.println("[Profiler Tests] Failed to delete test profiles.");
            }
        }
    }

}
