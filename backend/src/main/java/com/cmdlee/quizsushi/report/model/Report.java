package com.cmdlee.quizsushi.report.model;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.domain.model.TimeBaseEntity;
import com.cmdlee.quizsushi.report.model.enums.ReasonType;
import com.cmdlee.quizsushi.report.model.enums.ReportStatusType;
import com.cmdlee.quizsushi.report.model.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Report extends TimeBaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "report_seq"
    )
    @SequenceGenerator(
            name = "report_seq",
            sequenceName = "report_seq",
            allocationSize = 1
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReasonType reason;

    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    private Long targetId;

    private boolean read = false;

    @Enumerated(EnumType.STRING)
    private ReportStatusType status = ReportStatusType.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private QuizsushiMember reporter;

    @Builder
    public Report(Long id, ReasonType reason, String title, String message,
                  ReportTargetType targetType, Long targetId,
                  boolean read, ReportStatusType status, QuizsushiMember reporter) {
        this.id = id;
        this.reason = reason;
        this.title = title;
        this.message = message;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.reporter = reporter;
    }

    public void reading() {
        this.read = true;
    }

    public void updateStatus(ReportStatusType status) {
        this.status = status;
    }


}