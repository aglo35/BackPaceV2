package application;

import application.profile.Goal;
import application.profile.GoalRepository;
import application.profile.Goals;
import application.profile.GoalsRepository;
import application.team.*;
import application.team.current_day_attendance.CurrentDayAttendance;
import application.team.current_day_attendance.CurrentDayAttendanceRepository;
import application.user.PaceUser;
import application.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot main application.Application
 */
@SpringBootApplication
public class Application {

    private static final String HTTP_GRAPH_FACEBOOK_COM_ALLAR_PICTURE_TYPE_LARGE = "http://graph.facebook" + "" +
            ".com/1273703759309879/picture?type=large";
    private static final String ALLAR_ACCESS_TOKEN =
            "EAAD08lC2fhMBACwwwp8S5wIIKVI7kGaxDd1Reddwmd8rVNXwp7RhR4K2ZCZAl8iegIFZAedy32wod80kk1HN2Ki19ZADUPRSW3l16MvmChZAjATzK6tOAJTeuBShYmVL7T86haatcHQ9gjuyGe6TDRrQvZBsSZAXn5FRzWjGpQP7ZCLsEBZA2UVCIhxzVvpXcDb0ZD";

    private static final String ALLAR_USER_ID = "1273703759309879";


    public static String ROOT = "upload-dir";

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
    }

    /**
     * We can do our initialization logic and testing here.
     */
    @Bean
    public CommandLineRunner user(UserRepository userRepository, ShortTableRowRepository shortTableRowRepository,
                                  GoalRepository goalRepository, GoalsRepository goalsRepository, TeamRepository
                                              teamRepository, CurrentDayAttendanceRepository
                                              currentDayAttendanceRepository, DateRepository dateRepository) {
        return (args) -> {
            createFileUploadDirectory();

            PaceUser paceUserAllar = getPaceUserAllar();

//            @@@@@ initGoals @@@@@
            initGoals(goalRepository);

            Goals goals = new Goals();
            Map<String, ArrayList<Goal>> goalMap = new HashMap<>();

            ArrayList<Goal> goalArrayList = (ArrayList<Goal>) getInitGoals();
            ArrayList<Goal> gymnasticsGoals = new ArrayList<>();
            ArrayList<Goal> cheerleadingGoals = new ArrayList<>();

            for (Goal goal : goalArrayList) {
                if (goal.getCategory().equals("gymnastics")) {
                    gymnasticsGoals.add(goal);
                } else if (goal.getCategory().equals("cheerleading")) {
                    cheerleadingGoals.add(goal);
                }
            }

            goalMap.put("gymnastics", gymnasticsGoals);
            goalMap.put("cheerleading", cheerleadingGoals);
            goals.setGoalMap(goalMap);

            goalsRepository.save(goals);

            paceUserAllar.setGoals(goals);

//            @@@@@ Võrgurühm @@@@@
            Team teamV6rguryhm = new Team();
            teamV6rguryhm.setTeamName("Võrgurühm");

            ArrayList<ShortTableRow> shortTableRowsV6rguryhm = getShortTableRowsV6rguryhm();
            teamV6rguryhm.setFullScoresTableList(shortTableRowsV6rguryhm);

//            @@@@@ Harrastajad @@@@@
            Team teamHarrastajad = new Team();
            teamHarrastajad.setTeamName("Harrastajad");

            ArrayList<ShortTableRow> shortTableRowsHarrastajad = getShortTableRowsHarrastajad();
            teamHarrastajad.setFullScoresTableList(shortTableRowsHarrastajad);

//            @@@@@ J2relkasv @@@@@
            Team teamJ2relkasv = new Team();
            teamJ2relkasv.setTeamName("Järelkasv");

            ArrayList<ShortTableRow> shortTableRowsJ2relkasv = getShortTableRowsJ2relkasv();
            teamJ2relkasv.setFullScoresTableList(shortTableRowsJ2relkasv);

//            @@@@@ Kossurühm @@@@@
            Team teamKossuryhm = new Team();
            teamKossuryhm.setTeamName("Kossurühm");

            ArrayList<ShortTableRow> shortTableRowsKossuryhm = getShortTableRowsKossyryhm(shortTableRowRepository);
            teamKossuryhm.setFullScoresTableList(shortTableRowsKossuryhm);

//            @@@@@ Saltopoisid @@@@@
            Team teamSaltopoisid = new Team();
            teamSaltopoisid.setTeamName("Saltopoisid");

            ArrayList<ShortTableRow> shortTableRowsSaltopoisid = getShortTableRowsSaltopoisid(shortTableRowRepository);
            teamSaltopoisid.setFullScoresTableList(shortTableRowsSaltopoisid);

//            @@@@@ initAttendance @@@@@
            Date date = new Date("20", "4", "2017");
            dateRepository.save(date);
            Date date2 = new Date("21", "4", "2017");
            dateRepository.save(date2);

            CurrentDayAttendance currentDayAttendance = new CurrentDayAttendance(2, 5, date);
            CurrentDayAttendance currentDayAttendance2 = new CurrentDayAttendance(10, 5, date2);
            currentDayAttendanceRepository.save(currentDayAttendance);
            currentDayAttendanceRepository.save(currentDayAttendance2);

            List<CurrentDayAttendance> currentMonthAttendanceList = teamSaltopoisid.getCurrentMonthAttendance();
            currentMonthAttendanceList.add(currentDayAttendance);
            currentMonthAttendanceList.add(currentDayAttendance2);

            teamSaltopoisid.setCurrentMonthAttendance(currentMonthAttendanceList);

//            Save team to database
            teamRepository.save(teamKossuryhm);
            teamRepository.save(teamSaltopoisid);
            teamRepository.save(teamV6rguryhm);
            teamRepository.save(teamHarrastajad);
            teamRepository.save(teamJ2relkasv);

//            Add to user to team
            List<Team> teamList = new ArrayList<>();
            teamList.add(teamSaltopoisid);
            paceUserAllar.setTeamList(teamList);

//            Save user to database
            userRepository.save(paceUserAllar);

            // fetch all customers
            log.info("Users found with findByFacebookId():");
            log.info("-------------------------------");
            log.info(userRepository.findByFacebookId(ALLAR_USER_ID).toString());
            log.info("Users found with findAll():");
            log.info("-------------------------------");
            for (PaceUser paceUser : userRepository.findAll()) {
                log.info(paceUser.toString());
            }
            log.info("");
        };
    }

    private ArrayList<ShortTableRow> getShortTableRowsJ2relkasv() {

        return new ArrayList<>();
    }

    private ArrayList<ShortTableRow> getShortTableRowsHarrastajad() {

        return new ArrayList<>();
    }

    private ArrayList<ShortTableRow> getShortTableRowsV6rguryhm() {

        return new ArrayList<>();
    }

    private PaceUser getPaceUserAllar() {
        PaceUser paceUserAllar = new PaceUser("Allar", ALLAR_USER_ID);
        paceUserAllar.setAccessToken(ALLAR_ACCESS_TOKEN);

        paceUserAllar.setPicture(HTTP_GRAPH_FACEBOOK_COM_ALLAR_PICTURE_TYPE_LARGE);
        return paceUserAllar;
    }

    private void createFileUploadDirectory() {
        boolean isDirCreated = new File(ROOT).mkdir();

        if (isDirCreated) {
            System.out.println("Directory created!");
        }
    }

    private void initGoals(GoalRepository goalRepository) {
        List<Goal> initGoals = getInitGoals();
        initGoals.forEach(goalRepository::save);
    }

    private ArrayList<ShortTableRow> getShortTableRowsKossyryhm(ShortTableRowRepository shortTableRowRepository) {
        ShortTableRow shortTableRow1 = new ShortTableRow(1, "Marin", "Gold", 1270);
        ShortTableRow shortTableRow2 = new ShortTableRow(2, "Marianne", "Gold", 1250);
        ShortTableRow shortTableRow3 = new ShortTableRow(3, "Britta", "Gold", 1900);

        ArrayList<ShortTableRow> shortTableRows = new ArrayList<>();
        shortTableRows.add(shortTableRow1);
        shortTableRows.add(shortTableRow2);
        shortTableRows.add(shortTableRow3);

        shortTableRows.forEach(shortTableRowRepository::save);

        return shortTableRows;
    }

    private ArrayList<ShortTableRow> getShortTableRowsSaltopoisid(ShortTableRowRepository shortTableRowRepository) {
        ShortTableRow shortTableRow2 = new ShortTableRow(2, "Georg", "Gold", 980);
        ShortTableRow shortTableRow3 = new ShortTableRow(3, "Mario", "Gold", 950);

        ArrayList<ShortTableRow> shortTableRows = new ArrayList<>();
        shortTableRows.add(shortTableRow2);
        shortTableRows.add(shortTableRow3);

        shortTableRows.forEach(shortTableRowRepository::save);

        return shortTableRows;
    }

    private List<Goal> getInitGoals() {

        Goal goal1 = new Goal();
        goal1.setCategory("gymnastics");
        goal1.setTitle("Salto backward");
        goal1.setPoints(50);


        Goal goal2 = new Goal();
        goal2.setCategory("gymnastics");
        goal2.setTitle("Round off");
        goal2.setPoints(30);


        Goal goal3 = new Goal();
        goal3.setCategory("gymnastics");
        goal3.setTitle("Flic flac");
        goal3.setPoints(50);


        Goal goal4 = new Goal();
        goal4.setCategory("cheerleading");
        goal4.setTitle("Toss cupie");
        goal4.setPoints(80);


        Goal goal5 = new Goal();
        goal5.setCategory("cheerleading");
        goal5.setTitle("Walk-in-hands");
        goal5.setPoints(10);

        Goal goal6 = new Goal();
        goal6.setCategory("cheerleading");
        goal6.setTitle("Liberty");
        goal6.setPoints(50);

        List<Goal> goals = new ArrayList<>();
        goals.add(goal1);
        goals.add(goal2);
        goals.add(goal3);
        goals.add(goal4);
        goals.add(goal5);
        goals.add(goal6);

        return goals;
    }

    //    Required to enable cross-origin resource sharing
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/*");
                registry.addMapping("/api/*/*");
            }
        };
    }
}
