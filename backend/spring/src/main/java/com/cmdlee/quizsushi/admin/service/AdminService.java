package com.cmdlee.quizsushi.admin.service;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import com.cmdlee.quizsushi.admin.dto.request.AdminCreateRequest;
import com.cmdlee.quizsushi.admin.dto.request.AdminInfoUpdateRequest;
import com.cmdlee.quizsushi.admin.dto.response.AdminResponse;
import com.cmdlee.quizsushi.admin.dto.response.MemberPageResponse;
import com.cmdlee.quizsushi.admin.dto.response.MemberResponse;
import com.cmdlee.quizsushi.admin.dto.response.StatRawResponse;
import com.cmdlee.quizsushi.admin.repository.AdminMemberRepository;
import com.cmdlee.quizsushi.admin.repository.projection.StatRawProjection;
import com.cmdlee.quizsushi.global.auth.crypt.PasswordHasher;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.dto.response.AdminMeResponse;
import com.cmdlee.quizsushi.member.dto.response.MeResponse;
import com.cmdlee.quizsushi.member.repository.MemberRepository;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.repository.GuestQuizSolveLogRepository;
import com.cmdlee.quizsushi.quiz.repository.MemberQuizSolveLogRepository;
import com.cmdlee.quizsushi.quiz.repository.QuizRepository;
import com.cmdlee.quizsushi.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final QuizRepository quizRepository;
    private final MemberQuizSolveLogRepository memberQuizSolveLogRepository;
    private final GuestQuizSolveLogRepository guestQuizSolveLogRepository;
    private final ReportRepository reportRepository;
    private final AdminMemberRepository adminMemberRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(readOnly = true)
    public List<StatRawResponse> getStats(LocalDateTime start, LocalDateTime end, String truncType) {
        List<StatRawProjection> joinStats = memberRepository.findJoinStats(truncType, start, end);
        List<StatRawProjection> quizStats = quizRepository.findCreatedStats(truncType, start, end);
        List<StatRawProjection> memberSolveStats = memberQuizSolveLogRepository.findSolvedStats(truncType, start, end);
        List<StatRawProjection> guestSolveStats = guestQuizSolveLogRepository.findSolvedStats(truncType, start, end);
        List<StatRawProjection> reportStats = reportRepository.findReportStats(truncType, start, end);

        return Stream.of(joinStats, quizStats, memberSolveStats, guestSolveStats, reportStats).flatMap(Collection::stream).map(p -> new StatRawResponse(p.getLabel(), p.getTime(), p.getCount())).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        List<AdminMember> adminList = adminMemberRepository.findAll();
        return adminList.stream().map(AdminResponse::form).toList();
    }

    @Transactional
    public void createAdmin(AdminCreateRequest request) {
        AdminMember root = AdminMember.builder().alias(request.getAlias()).username(request.getUsername()).password(passwordHasher.hash(request.getRawPassword())).role(AdminRole.from(request.getRole())).build();
        adminMemberRepository.save(root);
    }

    @Transactional
    public void updateAdminRole(Long adminId, String role) {
        AdminMember admin = getAdminOrThrow(adminId);
        admin.updateRole(AdminRole.from(role));
    }

    @Transactional
    public void deleteAdmin(Long adminId) {
        AdminMember admin = getAdminOrThrow(adminId);
        adminMemberRepository.delete(admin);
    }

    @Transactional(readOnly = true)
    public MeResponse getAdminMe(long adminId) {
        AdminMember adminMember = getAdminOrThrow(adminId);
        return AdminMeResponse.from(adminMember);
    }

    @Transactional
    public void updateMyProfile(Long adminId, AdminInfoUpdateRequest request) {
        AdminMember admin = getAdminOrThrow(adminId);

        String alias = request.getNewAlias();
        String password = request.getNewRawPassword();
        String hashedPassword = (password != null && !password.isBlank()) ? passwordHasher.hash(password) : null;

        admin.updateInfo(alias, hashedPassword);
    }

    @Transactional(readOnly = true)
    public MemberPageResponse findMembers(String searchQuery, String status,
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String likeQuery = (searchQuery == null || searchQuery.isBlank()) ? null : "%" + searchQuery + "%";
        Page<QuizsushiMember> memberPage = memberRepository.searchMembers(likeQuery, status, pageable);

        List<MemberResponse> memberResponses = memberPage.stream()
                .map(MemberResponse::from)
                .toList();

        return new MemberPageResponse(
                memberResponses,
                memberPage.getNumber(),
                memberPage.getTotalPages(),
                memberPage.getTotalElements()
        );
    }

    @Transactional
    public void updateMemberStatus(long memberid) {
        QuizsushiMember member = getMemberOrThrow(memberid);
        member.banToggle();
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = getQuizOrThrow(quizId);
        quizRepository.delete(quiz);
    }

    private AdminMember getAdminOrThrow(Long adminId) {
        return adminMemberRepository.findById(adminId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
    }

    private QuizsushiMember getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
    }

    private Quiz getQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
