package com.smartcomplaint.config;

import com.smartcomplaint.model.Category;
import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.ComplaintRepository;
import com.smartcomplaint.repository.UserRepository;
import com.smartcomplaint.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final com.smartcomplaint.repository.NoticeRepository noticeRepository;

    public DataInitializer(UserRepository userRepository, ComplaintRepository complaintRepository, com.smartcomplaint.repository.NoticeRepository noticeRepository) {
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
        this.noticeRepository = noticeRepository;
    }

    @Override
    public void run(String... args) {
        // Seed Administrator
        String adminEmail = "r.haresh11.8.2007@gmail.com";
        String[] previousAdminEmails = new String[]{"Swethadurai48@gmail.com", "admin@system.com"};

        // Find existing admin (by new email, previous emails, or existing ADMIN role)
        User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseGet(() -> {
                    for (String oldEmail : previousAdminEmails) {
                        var opt = userRepository.findByEmail(oldEmail.trim().toLowerCase());
                        if (opt.isPresent()) {
                            return opt.get();
                        }
                    }
                    return userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.ADMIN)
                            .findFirst()
                            .orElseGet(User::new);
                });

        admin.setName("System Administrator");
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPassword(UserService.hashPassword("SRM@484"));
        admin.setRole(Role.ADMIN);
        if (admin.getDepartment() == null) {
            admin.setDepartment("Campus Administration");
        }
        userRepository.save(admin);
        log.info("Admin account configured: {}", adminEmail);

        // Remove/disable any obsolete admin accounts so they can no longer authenticate
        for (String oldEmail : previousAdminEmails) {
            userRepository.findByEmail(oldEmail.trim().toLowerCase()).ifPresent(oldUser -> {
                if (!oldUser.getId().equals(admin.getId())) {
                    userRepository.delete(oldUser);
                    log.info("Removed obsolete admin account: {}", oldEmail);
                }
            });
        }

        // Seed demo Student if no students exist
        String demoStudentEmail = "student@campus.edu";
        if (userRepository.findByEmail(demoStudentEmail).isEmpty()) {
            User demoStudent = new User();
            demoStudent.setName("Alex Rivera");
            demoStudent.setEmail(demoStudentEmail);
            demoStudent.setPassword(UserService.hashPassword("student123"));
            demoStudent.setRole(Role.STUDENT);
            demoStudent.setStudentId("STU-2024-042");
            demoStudent.setDepartment("Computer Science");
            demoStudent.setRoomOrHostel("Block B, Room 304");
            userRepository.save(demoStudent);
            log.info("Demo Student account created: {} / student123", demoStudentEmail);

            // Seed demo complaints if none exist
            if (complaintRepository.count() == 0) {
                Complaint c1 = new Complaint(
                        "Wi-Fi connection keeps dropping in 3rd Floor study hall",
                        "The 5GHz access point resets every 15 minutes during peak evening study hours.",
                        Category.WIFI,
                        "Library, 3rd Floor Study Hall",
                        demoStudent
                );
                c1.setStatus(ComplaintStatus.IN_PROGRESS);
                c1.setAdminResponse("Network engineering team dispatched to replace switch on Floor 3.");
                complaintRepository.save(c1);

                Complaint c2 = new Complaint(
                        "Low water pressure in Block B 2nd floor restrooms",
                        "Water pressure has dropped significantly since yesterday morning.",
                        Category.WATER,
                        "Hostel Block B, 2nd Floor",
                        demoStudent
                );
                c2.setStatus(ComplaintStatus.PENDING);
                complaintRepository.save(c2);

                Complaint c3 = new Complaint(
                        "Flickering LED tube light in Classroom 102",
                        "The main front light is flickering and causing glare during morning lectures.",
                        Category.ELECTRICITY,
                        "Academic Building 1, Classroom 102",
                        demoStudent
                );
                c3.setStatus(ComplaintStatus.RESOLVED);
                c3.setAdminResponse("Replaced fixture and ballasts on Sept 20.");
                complaintRepository.save(c3);

                log.info("Sample complaints successfully initialized.");
            }
        }

        // Seed initial hostel notices if none exist
        if (noticeRepository.count() == 0) {
            java.time.LocalDateTime base = java.time.LocalDateTime.now();
            noticeRepository.save(new com.smartcomplaint.model.Notice(
                    "Mess Menu for This Week",
                    "The updated autumn season mess menu will take effect this Monday. Special South Indian and Continental counters are active during dinner services.",
                    "Dining",
                    true,
                    base.minusDays(2)
            ));
            noticeRepository.save(new com.smartcomplaint.model.Notice(
                    "Hostel Maintenance Schedule",
                    "Routine water pipeline and electrical breaker inspections are scheduled this Saturday between 10:00 AM and 01:00 PM. Please submit any room repair tickets in advance.",
                    "Maintenance",
                    true,
                    base.minusDays(4)
            ));
            noticeRepository.save(new com.smartcomplaint.model.Notice(
                    "No Ragging - Strict Action",
                    "SRM maintains a zero-tolerance policy against ragging. Any harassment should be reported immediately to the Anti-Ragging squad or Chief Warden desk.",
                    "Discipline",
                    true,
                    base.minusDays(7)
            ));
            noticeRepository.save(new com.smartcomplaint.model.Notice(
                    "Room Allotment Updates",
                    "Room change requests for the ongoing semester have concluded. Final verified room keys and biometric permissions have been synchronized at the entry gates.",
                    "Administration",
                    false,
                    base.minusDays(10)
            ));
            noticeRepository.save(new com.smartcomplaint.model.Notice(
                    "Cleanliness Drive - Thank You!",
                    "Thank you to all residents who participated in the campus green initiative. Separate bins for recyclables and dry waste are now positioned on all floors.",
                    "Community",
                    false,
                    base.minusDays(12)
            ));
            log.info("Official hostel notices successfully seeded into PostgreSQL.");
        }
    }
}
