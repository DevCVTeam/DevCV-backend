package com.devcv.member.domain.dto;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.enumtype.CompanyType;
import com.devcv.member.domain.enumtype.JobType;
import com.devcv.member.domain.enumtype.RoleType;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.exception.NotNullException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class MemberModifyAllRequest {
    private Long memberId;
    private String memberName;
    private String nickName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private SocialType social;
    private RoleType memberRole;
    private CompanyType company;
    private JobType job;
    private List<String> stack;

    public MemberModifyAllRequest(Long memberId, String memberName, String nickName, String email, String password, String phone,
                                  String address, SocialType social, RoleType memberRole, CompanyType company, JobType job, List<String> stack){
        this.memberId = memberId;
        this.memberName = memberName;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.social = social;
        this.memberRole = memberRole;
        this.company = company;
        this.job = job;
        this.stack = stack;
        // NULL CHECK
        try {
            if(this.job == null || this.address == null || this.stack == null
                    || this.email == null || this.memberName == null || this.social == null
                    || this.company == null || this.phone == null || this.memberId == null
                    || this.nickName == null || this.password == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (NotNullException e){
            e.fillInStackTrace();
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
    }
}
