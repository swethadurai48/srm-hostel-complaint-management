package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.model.Notice;
import com.smartcomplaint.repository.NoticeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notice>>> getAllNotices() {
        List<Notice> notices = noticeRepository.findAllByOrderByPublishedAtDesc();
        return ResponseEntity.ok(ApiResponse.ok("Notices retrieved", notices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Notice>> getNoticeById(@PathVariable Long id) {
        return noticeRepository.findById(id)
                .map(n -> ResponseEntity.ok(ApiResponse.ok("Notice retrieved", n)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Notice not found")));
    }
}
