package com.devcv.admin.presentation;

import com.devcv.admin.application.AdminService;
import com.devcv.admin.dto.DeletedEventResponse;
import com.devcv.admin.dto.PaginatedAdminResumeResponse;
import com.devcv.event.domain.Event;
import com.devcv.event.domain.dto.EventRequest;
import com.devcv.resume.domain.dto.ResumeDto;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/admin/*")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 이력서 상태 변경
    @PatchMapping("/resumes/{resumeId}/{status}")
    public ResponseEntity<String> adminUpdateResumeStatus(@PathVariable Long resumeId, @PathVariable ResumeStatus status) {
        adminService.updateStatus(resumeId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resumes/pending")
    public ResponseEntity<PaginatedAdminResumeResponse> getPendingResumes(
            @RequestParam("page") int page,
            @RequestParam("size") int size) {
        PaginatedAdminResumeResponse pendingResumes = adminService.getResumesByStatus("pending", page, size);
        return ResponseEntity.ok(pendingResumes);
    }

    @GetMapping("/resumes/modified")
    public ResponseEntity<PaginatedAdminResumeResponse> getModifiedResumes(
            @RequestParam("page") int page,
            @RequestParam("size") int size) {
        PaginatedAdminResumeResponse modifiedResumes = adminService.getResumesByStatus("modified", page, size);
        return ResponseEntity.ok(modifiedResumes);
    }

    // 이력서 상세 조회
    @GetMapping("/resumes/{resume-id}")
    public ResponseEntity<ResumeDto> getAdminResumes(@PathVariable("resume-id") Long resumeId) {
        ResumeDto adminResumeDto = adminService.getResume(resumeId);
        return ResponseEntity.ok(adminResumeDto);
    }

    @PostMapping("/events")
    public ResponseEntity<Object> createEvent(@RequestBody @Valid EventRequest eventRequest) {
        Event event = adminService.createEvent(eventRequest);
        return ResponseEntity.created(URI.create(String.valueOf(event.getId()))).build();
    }

    @DeleteMapping("/events/{event-id}")
    public ResponseEntity<DeletedEventResponse> deleteEvent(@PathVariable("event-id") Long eventId) {
        Event deletedEvent = adminService.deleteEvent(eventId);
        DeletedEventResponse response = DeletedEventResponse.of(deletedEvent);
        return ResponseEntity.ok(response);
    }
}