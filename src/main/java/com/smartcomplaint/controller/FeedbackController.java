package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.FeedbackRequest;
import com.smartcomplaint.model.Feedback;
import com.smartcomplaint.model.User;
import com.smartcomplaint.repository.FeedbackRepository;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    public FeedbackController(FeedbackRepository feedbackRepository, UserService userService) {
        this.feedbackRepository = feedbackRepository;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        return userService.findById(userId).orElse(null);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Feedback>> submitFeedback(
            @Valid @RequestBody FeedbackRequest req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to submit feedback"));
        }

        Feedback fb = new Feedback(req.getComplaintId(), user, req.getRating(), req.getMessage());
        Feedback saved = feedbackRepository.save(fb);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feedback submitted successfully", saved));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Feedback>>> getMyFeedbacks(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to view your feedback"));
        }

        List<Feedback> list = feedbackRepository.findByStudentOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(ApiResponse.ok("Feedback retrieved", list));
    }
}
