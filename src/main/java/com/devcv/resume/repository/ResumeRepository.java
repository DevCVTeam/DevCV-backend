package com.devcv.resume.repository;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.CompanyType;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.domain.enumtype.StackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 판매내역 이력서 상세 조회
    @Query("SELECT r FROM Resume r WHERE r.resumeId = :resumeId AND r.member.memberId = :memberId")
    Optional<Resume> findByIdAndMemberId(Long resumeId, Long memberId);

    //이력서 조회
    @EntityGraph(attributePaths = {"member", "category", "imageList"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Resume> findByResumeId(Long resumeId);

    // 전체 이력서 조회
    @Query("SELECT rm, AVG(COALESCE(rv.grade, 0)), COUNT(rv) " +
            "FROM Resume rm LEFT JOIN Review rv ON rv.resume = rm " +
            "WHERE rm.status = :status " +
            "GROUP BY rm")
    Page<Object[]> findByStatus(ResumeStatus status, Pageable pageable);

    // 회원별 이력서 조회
    @Query("SELECT rm FROM Resume rm WHERE rm.member.memberId = :memberId AND rm.delFlag = false")
    List<Resume> findByMember(Long memberId);

    // 직무 & 회사별 이력서 조회
    @Query("SELECT rm, AVG(COALESCE(rv.grade, 0)), COUNT(rv) " +
            "FROM Resume rm LEFT JOIN Review rv ON rv.resume = rm " +
            "WHERE rm.category.stackType = :stackType AND rm.category.companyType = :companyType AND rm.status = :status " +
            "GROUP BY rm")
    Page<Object[]> findByCategory_StackTypeAndCategory_CompanyTypeAndStatus(StackType stackType, CompanyType companyType, ResumeStatus status, Pageable pageable);

    // 직무별 이력서 조회
    @Query("SELECT rm, AVG(COALESCE(rv.grade, 0)), COUNT(rv) " +
            "FROM Resume rm LEFT JOIN Review rv ON rv.resume = rm " +
            "WHERE rm.category.stackType = :stackType AND rm.status = :status " +
            "GROUP BY rm")
    Page<Object[]> findByCategory_StackTypeAndStatus(StackType stackType, ResumeStatus status, Pageable pageable);

    // 회사별 이력서 조회
    @Query("SELECT rm, AVG(COALESCE(rv.grade, 0)), COUNT(rv) " +
            "FROM Resume rm LEFT JOIN Review rv ON rv.resume = rm " +
            "WHERE rm.category.companyType = :companyType AND rm.status = :status " +
            "GROUP BY rm")
    Page<Object[]> findByCategory_CompanyTypeAndStatus(CompanyType companyType, ResumeStatus status, Pageable pageable);

    // 상세 이력서 조회
    @Query("SELECT r, AVG(COALESCE(rv.grade, 0)), COUNT(rv) " +
            "FROM Resume r LEFT JOIN Review rv ON rv.resume = r " +
            "WHERE r.resumeId = :resumeId AND r.status = 'regCompleted' " +
            "GROUP BY r")
    List<Object[]> findByIdAndStatus(Long resumeId);

    //----------------등록완료 default인 메서드 start-----------------
    default Page<Object[]> findApprovedResumes(Pageable pageable) {
        return findByStatus(ResumeStatus.regcompleted, pageable);
    }
    default Page<Object[]> findApprovedResumesByStackTypeAndCompanyType(StackType stackType, CompanyType companyType, Pageable pageable) {
        return findByCategory_StackTypeAndCategory_CompanyTypeAndStatus(stackType, companyType, ResumeStatus.regcompleted, pageable);
    }

    default Page<Object[]> findApprovedResumesByStackType(StackType stackType, Pageable pageable) {
        return findByCategory_StackTypeAndStatus(stackType, ResumeStatus.regcompleted, pageable);
    }

    default Page<Object[]> findApprovedResumesByCompanyType(CompanyType companyType, Pageable pageable) {
        return findByCategory_CompanyTypeAndStatus(companyType, ResumeStatus.regcompleted, pageable);
    }

    //----------------판매승인이 default인 메서드 end-----------------

    // 이력서 삭제
    @Modifying
    @Query("UPDATE Resume r set r.delFlag = :flag where r.resumeId = :resumeId")
    void updateToDelete(Long resumeId, boolean flag);

}
