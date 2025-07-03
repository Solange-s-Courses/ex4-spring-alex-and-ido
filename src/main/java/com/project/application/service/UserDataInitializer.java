package com.project.application.service;

import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Value("${app.initialize-test-users:false}") // Default to false if not specified
    private boolean initializeTestUsers;

    private static final List<String> FIRST_NAMES = Arrays.asList(
            "alex", "john", "emily", "david", "sarah", "michael", "jessica", "chris", "ashley", "daniel",
            "lisa", "matthew", "amanda", "james", "jennifer", "ryan", "michelle", "andrew", "stephanie", "joshua",
            "melissa", "kevin", "nicole", "brian", "amy", "anthony", "anna", "mark", "laura", "steven",
            "rachel", "paul", "helen", "kenneth", "maria", "edward", "rebecca", "jason", "sandra", "thomas",
            "nancy", "robert", "donna", "charles", "carol", "patrick", "janet", "gary", "kimberly", "douglas"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
            "smith", "johnson", "williams", "brown", "jones", "garcia", "miller", "davis", "rodriguez", "martinez",
            "hernandez", "lopez", "gonzalez", "wilson", "anderson", "thomas", "taylor", "moore", "jackson", "martin",
            "lee", "perez", "thompson", "white", "harris", "sanchez", "clark", "ramirez", "lewis", "robinson",
            "walker", "young", "allen", "king", "wright", "scott", "torres", "nguyen", "hill", "flores",
            "green", "adams", "nelson", "baker", "hall", "rivera", "campbell", "mitchell", "carter", "roberts"
    );

    private final Random random = new Random();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (initializeTestUsers) {
            initializeTestUsersData();
        } else {
            System.out.println("user initialization for testing is disabled.");
        }
    }

    private void initializeTestUsersData() {
        try {
            // Check if test users already exist (skip if database already has many users)
            long userCount = userRepository.count();
            if (userCount >= 10) { // Assuming you have admin + a few test users already
                System.out.println("test users already exist. skipping initialization.");
                return;
            }

            System.out.println("Initializing test users...");

            int createdCount = 0;
            int maxAttempts = 60; // Reduced attempts since we only need 30 users
            int attempts = 0;

            while (createdCount < 30 && attempts < maxAttempts) {
                attempts++;

                try {
                    String firstName = getRandomFirstName();
                    String lastName = getRandomLastName();
                    String email = generateEmail(firstName, lastName, createdCount);
                    String phone = generatePhone();
                    String password = generatePassword();

                    // Check if email or phone already exists
                    if (userRepository.findByEmailAddress(email).isPresent() ||
                            userRepository.findByPhoneNumber(phone).isPresent()) {
                        continue; // Try again with different data
                    }

                    // Create user
                    User user = new User();
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmailAddress(email);
                    user.setPhoneNumber(phone);
                    user.setPassword(password);
                    user.setDateOfIssue(generateRandomDate());

                    // Set role to "user" only
                    Role userRole = roleService.findByName("user")
                            .orElse(roleService.getDefaultUserRole());
                    user.setRole(userRole);

                    // Save user
                    userRepository.save(user);
                    createdCount++;

                    if (createdCount % 10 == 0) {
                        System.out.println("Created " + createdCount + " test users...");
                    }

                } catch (Exception e) {
                    System.err.println("Error creating user " + attempts + ": " + e.getMessage());
                }
            }

            System.out.println("Test user initialization completed. Created " + createdCount + " users with 'user' role.");

        } catch (Exception e) {
            System.err.println("Failed to initialize test users: " + e.getMessage());
        }
    }

    private String getRandomFirstName() {
        return FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
    }

    private String getRandomLastName() {
        return LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
    }

    private String generateEmail(String firstName, String lastName, int userNumber) {
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "email.com"};
        String domain = domains[random.nextInt(domains.length)];

        // Create variations to avoid duplicates
        String baseEmail = firstName + "." + lastName;
        if (userNumber > 15) { // Adjusted for 30 users instead of 50
            baseEmail = firstName + lastName + (userNumber - 15);
        }

        return baseEmail + "@" + domain;
    }

    private String generatePhone() {
        // Generate 10-digit phone number starting with area codes
        String[] areaCodes = {"050", "052", "053", "054", "055", "058"};
        String areaCode = areaCodes[random.nextInt(areaCodes.length)];

        StringBuilder phone = new StringBuilder(areaCode);
        for (int i = 0; i < 7; i++) {
            phone.append(random.nextInt(10));
        }
        return phone.toString();
    }

    private String generatePassword() {
        String[] passwords = {
                "password123", "123456789", "qwerty123", "admin2024", "user12345",
                "welcome123", "test12345", "demo12345", "sample123", "default123"
        };
        return passwords[random.nextInt(passwords.length)];
    }

    private LocalDateTime generateRandomDate() {
        // Generate random date within the last 2 years
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoYearsAgo = now.minusYears(2);

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(twoYearsAgo, now);
        long randomDays = Math.abs(random.nextLong()) % daysBetween;

        return twoYearsAgo.plusDays(randomDays);
    }
}