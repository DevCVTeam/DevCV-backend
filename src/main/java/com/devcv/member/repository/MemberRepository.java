package com.devcv.member.repository;

import com.devcv.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
*   어노테이션 정리
*   @Transactional : 하위 메서드 실행 실패시 DB데이터 쿼리수행 최근데이터로 자동롤백
*   @Query : CrudRepository의 기본 메서드 커스텀
*   @Modifying( clearAutomatically= true) : 영속성 컨텍스트 자동 clear.
*/
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = "SELECT * FROM tb_member WHERE email=:email", nativeQuery = true)
    Member findMemberByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM tb_member WHERE member_id=:memberId", nativeQuery = true)
    Member findMemberBymemberId(@Param("memberId") Long memberId);

    @Query(value = "SELECT * FROM tb_member WHERE member_name=:memberName AND phone=:phone", nativeQuery = true)
    List<Member> findMemberBymemberNameAndPhone(@Param("memberName") String memberName, @Param("phone") String phone);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_member SET password=:password WHERE member_id=:memberId", nativeQuery = true)
    int updatePasswordBymemberId(@Param("password") String password, @Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_member SET member_name=:memberName, email=:email, password=:password, nickname=:nickname," +
            " phone=:phone, address=:address, company=:company, job=:job, stack=:stack WHERE member_id=:memberId", nativeQuery = true)
    int updateMemberBymemberId(@Param("memberName") String memberName, @Param("email") String email, @Param("password") String password,
                               @Param("nickname") String nickname, @Param("phone") String phone, @Param("address") String address,
                               @Param("company") String company, @Param("job") String job, @Param("stack") String stack,
                               @Param("memberId") Long memberId);
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_member SET member_name=:memberName, nickname=:nickname," +
            " phone=:phone, address=:address, company=:company, job=:job, stack=:stack WHERE member_id=:memberId", nativeQuery = true)
    int updateSocialMemberBymemberId(@Param("memberName") String memberName, @Param("nickname") String nickname, @Param("phone") String phone,
                                     @Param("address") String address, @Param("company") String company, @Param("job") String job,
                                     @Param("stack") String stack, @Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_member SET refresh_token=:refreshToken WHERE member_id=:memberId", nativeQuery = true)
    int updateRefreshTokenBymemberId(@Param("memberId") Long memberId,@Param("refreshToken") String refreshToken);
}
