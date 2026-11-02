package com.smartcomplaint.service;

import com.smartcomplaint.dto.LoginRequest;
import com.smartcomplaint.dto.RegisterRequest;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new student.
     */
    @Transactional
    public User registerStudent(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists: " + email);
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setPassword(hashPassword(req.getPassword()));
        user.setRole(Role.STUDENT);
        user.setStudentId(req.getStudentId());
        user.setDepartment(req.getDepartment());
        user.setRoomOrHostel(req.getRoomOrHostel());

        return userRepository.save(user);
    }

    /**
     * Authenticate user (Student or Admin).
     */
    public User authenticate(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String hashed = hashPassword(req.getPassword());
        if (!user.getPassword().equals(hashed)) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Create an admin user if not exists (used during startup initialization).
     */
    @Transactional
    public User createAdminIfNotExists(String name, String email, String password) {
        String cleanEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(cleanEmail).orElseGet(() -> {
            User admin = new User();
            admin.setName(name);
            admin.setEmail(cleanEmail);
            admin.setPassword(hashPassword(password));
            admin.setRole(Role.ADMIN);
            return userRepository.save(admin);
        });
    }

    @Transactional
    public User updateProfile(Long userId, com.smartcomplaint.dto.ProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getName() != null && !req.getName().isBlank()) {
            user.setName(req.getName().trim());
        }
        if (req.getStudentId() != null) user.setStudentId(req.getStudentId().trim());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment().trim());
        if (req.getYearOfStudy() != null) user.setYearOfStudy(req.getYearOfStudy().trim());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber().trim());
        if (req.getHostel() != null) user.setHostel(req.getHostel().trim());
        if (req.getBlock() != null) user.setBlock(req.getBlock().trim());
        if (req.getRoomNumber() != null) user.setRoomNumber(req.getRoomNumber().trim());

        // Also update unified roomOrHostel string if component fields provided
        if (req.getRoomNumber() != null) {
            String combined = (req.getBlock() != null ? req.getBlock() + ", " : "") + "Room " + req.getRoomNumber();
            user.setRoomOrHostel(combined);
        }

        return userRepository.save(user);
    }

    /**
     * Secure SHA-256 password hashing with application salt.
     */
    public static String hashPassword(String rawPassword) {
        try {
            String salted = "SmartComplaintSalt:" + rawPassword;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
