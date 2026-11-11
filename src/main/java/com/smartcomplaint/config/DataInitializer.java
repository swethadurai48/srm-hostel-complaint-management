package com.smartcomplaint.config;

import com.smartcomplaint.model.*;
import com.smartcomplaint.repository.*;
import com.smartcomplaint.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final NoticeRepository noticeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SlaConfigurationRepository slaConfigurationRepository;

    public DataInitializer(UserRepository userRepository,
                           ComplaintRepository complaintRepository,
                           NoticeRepository noticeRepository,
                           DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository,
                           SlaConfigurationRepository slaConfigurationRepository) {
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
        this.noticeRepository = noticeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.slaConfigurationRepository = slaConfigurationRepository;
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

        // Seed Enterprise SLA Configurations if none exist
        if (slaConfigurationRepository.count() == 0) {
            slaConfigurationRepository.save(new SlaConfiguration(Priority.URGENT, null, 4, 2));
            slaConfigurationRepository.save(new SlaConfiguration(Priority.HIGH, null, 12, 6));
            slaConfigurationRepository.save(new SlaConfiguration(Priority.MEDIUM, null, 24, 12));
            slaConfigurationRepository.save(new SlaConfiguration(Priority.LOW, null, 48, 24));
            log.info("Enterprise SLA configurations successfully seeded.");
        }

        // Seed Enterprise Departments if none exist
        if (departmentRepository.count() == 0) {
            Department elec = new Department("ELEC", "Electrical & Power Systems", "Campus electrical wiring, switchboards, lighting, and power backup maintenance.", admin);
            elec.setContactEmail("electrical.hostel@srmist.edu.in");
            elec.setContactPhone("Ext: 4401");
            elec.setBudget(75000.0);
            departmentRepository.save(elec);

            Department plumb = new Department("PLUMB", "Plumbing & Water Infrastructure", "Water supply, RO plants, restrooms, fixtures, and pipeline management.", admin);
            plumb.setContactEmail("plumbing.hostel@srmist.edu.in");
            plumb.setContactPhone("Ext: 4402");
            plumb.setBudget(60000.0);
            departmentRepository.save(plumb);

            Department net = new Department("NET", "IT & Campus Network", "Hostel Wi-Fi access points, fiber switches, and LAN connectivity support.", admin);
            net.setContactEmail("wifi.hostel@srmist.edu.in");
            net.setContactPhone("Ext: 4403");
            net.setBudget(90000.0);
            departmentRepository.save(net);

            Department house = new Department("HOUSE", "Housekeeping & Sanitation", "Corridor cleanliness, room sanitation, waste disposal, and hygiene.", admin);
            house.setContactEmail("housekeeping.hostel@srmist.edu.in");
            house.setContactPhone("Ext: 4404");
            house.setBudget(45000.0);
            departmentRepository.save(house);

            Department maint = new Department("MAINT", "Civil & Room Maintenance", "Carpentry, doors, windows, furniture repair, and civil construction.", admin);
            maint.setContactEmail("civil.hostel@srmist.edu.in");
            maint.setContactPhone("Ext: 4405");
            maint.setBudget(85000.0);
            departmentRepository.save(maint);

            Department mess = new Department("MESS", "Dining & Mess Quality", "Dining hall operations, food hygiene, caterer supervision, and nutrition.", admin);
            mess.setContactEmail("mess.hostel@srmist.edu.in");
            mess.setContactPhone("Ext: 4406");
            mess.setBudget(120000.0);
            departmentRepository.save(mess);

            Department sec = new Department("SEC", "Hostel Security & Safety", "Main gate control, visitor management, resident security, and night patrol.", admin);
            sec.setContactEmail("security.hostel@srmist.edu.in");
            sec.setContactPhone("Ext: 2222");
            sec.setBudget(50000.0);
            departmentRepository.save(sec);

            log.info("Enterprise Departments successfully initialized.");
        }

        // Seed Enterprise Employees if none exist
        if (employeeRepository.count() == 0) {
            Department elecDept = departmentRepository.findByCode("ELEC").orElse(null);
            Department plumbDept = departmentRepository.findByCode("PLUMB").orElse(null);
            Department netDept = departmentRepository.findByCode("NET").orElse(null);
            Department houseDept = departmentRepository.findByCode("HOUSE").orElse(null);
            Department maintDept = departmentRepository.findByCode("MAINT").orElse(null);

            Employee emp1 = new Employee("EMP-ELEC-01", "Rajesh Kumar", "rajesh.elec@srmist.edu.in", elecDept, "Senior Electrician");
            emp1.setPhone("+91 98401 23451");
            emp1.setSkills("High voltage wiring, Breaker repair, LED panels, DG sets");
            employeeRepository.save(emp1);

            Employee emp2 = new Employee("EMP-PLUMB-01", "S. Murugan", "murugan.plumb@srmist.edu.in", plumbDept, "Lead Plumber");
            emp2.setPhone("+91 98401 23452");
            emp2.setSkills("Pipeline leakages, RO filtration, Booster pumps, Restroom valves");
            employeeRepository.save(emp2);

            Employee emp3 = new Employee("EMP-NET-01", "Priya Sharma", "priya.it@srmist.edu.in", netDept, "Network Systems Engineer");
            emp3.setPhone("+91 98401 23453");
            emp3.setSkills("Cisco APs, Aruba controllers, Fiber splicing, DHCP & DNS triage");
            employeeRepository.save(emp3);

            Employee emp4 = new Employee("EMP-HOUSE-01", "A. Ramesh", "ramesh.clean@srmist.edu.in", houseDept, "Sanitation Supervisor");
            emp4.setPhone("+91 98401 23454");
            emp4.setSkills("Deep cleaning protocols, Waste management, Chemical inventory");
            employeeRepository.save(emp4);

            Employee emp5 = new Employee("EMP-MAINT-01", "K. Chandran", "chandran.maint@srmist.edu.in", maintDept, "Chief Carpenter & Civil Tech");
            emp5.setPhone("+91 98401 23455");
            emp5.setSkills("Locksmithing, Structural repairs, Aluminum fabrication, Furniture");
            employeeRepository.save(emp5);

            log.info("Enterprise Employees and Technicians successfully initialized.");
        }

        // Link existing complaints to Departments and compute SLA due dates if missing
        for (Complaint c : complaintRepository.findAll()) {
            boolean updated = false;
            if (c.getDepartment() == null) {
                Department targetDept = null;
                switch (c.getCategory()) {
                    case ELECTRICITY -> targetDept = departmentRepository.findByCode("ELEC").orElse(null);
                    case WATER -> targetDept = departmentRepository.findByCode("PLUMB").orElse(null);
                    case WIFI -> targetDept = departmentRepository.findByCode("NET").orElse(null);
                    case CLEANING -> targetDept = departmentRepository.findByCode("HOUSE").orElse(null);
                    case MAINTENANCE -> targetDept = departmentRepository.findByCode("MAINT").orElse(null);
                    case HOSTEL -> targetDept = departmentRepository.findByCode("MESS").orElse(null);
                    case TRANSPORT -> targetDept = departmentRepository.findByCode("SEC").orElse(null);
                    default -> targetDept = departmentRepository.findByCode("MAINT").orElse(null);
                }
                if (targetDept != null) {
                    c.setDepartment(targetDept);
                    updated = true;
                }
            }
            if (c.getSlaDueDate() == null && c.getCreatedAt() != null) {
                int hours = switch (c.getPriority() != null ? c.getPriority() : Priority.MEDIUM) {
                    case URGENT -> 4;
                    case HIGH -> 12;
                    case MEDIUM -> 24;
                    case LOW -> 48;
                };
                c.setSlaDueDate(c.getCreatedAt().plusHours(hours));
                updated = true;
            }
            if (updated) {
                complaintRepository.save(c);
            }
        }
    }
}
