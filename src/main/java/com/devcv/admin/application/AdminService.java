package com.devcv.admin.application;

import com.devcv.admin.dto.AdminResumeList;
import com.devcv.admin.dto.PaginatedAdminResumeResponse;
import com.devcv.admin.repository.AdminResumeRepository;
import com.devcv.common.exception.BadRequestException;
import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.NotFoundException;
import com.devcv.event.domain.Event;
import com.devcv.event.domain.dto.EventRequest;
import com.devcv.event.repository.EventRepository;
import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.dto.ResumeDto;
import com.devcv.resume.domain.dto.ResumeResponse;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.exception.ResumeNotExistException;
import com.devcv.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final EventRepository eventRepository;
    private final AdminResumeRepository adminResumeRepository;
    private final ResumeRepository resumeRepository;

    public Event createEvent(EventRequest eventRequest) {
        Event event = Event.of(eventRequest.name(), eventRequest.eventCategory(), eventRequest.point(),
                eventRequest.startDate(), eventRequest.endDate());
        return eventRepository.save(event);
    }

    public Event deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
        if (event.getIsDeleted()) {
            throw new NotFoundException(ErrorCode.EVENT_NOT_EXIST);
        }
        event.delete();
        return event;
    }

    public PaginatedAdminResumeResponse getResumesByStatus(String input, int page, int size) {
        ResumeStatus status = ResumeStatus.valueOf(input);
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Resume> resumePage = adminResumeRepository.findByStatus(status, pageable);
        List<ResumeResponse> resumeList = resumePage.getContent().stream().map(ResumeResponse::from).collect(Collectors.toList());
        AdminResumeList adminResumeList = AdminResumeList.of(status , resumeList);
        return new PaginatedAdminResumeResponse(
                List.of(adminResumeList),
                resumePage.getTotalElements(),
                resumePage.getNumberOfElements(),
                resumePage.getNumber()+1,
                resumePage.getTotalPages(),
                resumePage.getSize(),
                page,
                resumePage.getTotalPages()
        );
    }

    public ResumeDto getResume(Long resumeId) {
        Resume resume = adminResumeRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));
        return ResumeDto.from(resume);
    }

    // 상태 변경
    // resumeStatus에 가능한 값 : approved, rejected
    public int updateStatus(Long resumeId, ResumeStatus resumeStatus) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        // 예외 처리 추가
        // regcompleted, modified 접근 불가
        if (resume.getStatus() == ResumeStatus.regcompleted
                || resumeStatus == ResumeStatus.modified || resumeStatus == ResumeStatus.regcompleted) {
            throw new BadRequestException(ErrorCode.RESUME_STATUS_EXCEPTION);
        }
        // deleted 접근 불가
        if (resume.getStatus() == ResumeStatus.deleted || resumeStatus == ResumeStatus.deleted) {
            throw new ResumeNotExistException(ErrorCode.RESUME_NOT_EXIST);
        }
        return adminResumeRepository.updateByresumeId(resumeId,resumeStatus);
    }
}