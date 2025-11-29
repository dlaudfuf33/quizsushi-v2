--  회원 정보 테이블 (기본 사용자 계정 정보)
CREATE SEQUENCE IF NOT EXISTS quizsushi_member_seq START 1 INCREMENT 1;
CREATE TABLE quizsushi_member
(
    id            BIGINT PRIMARY KEY   DEFAULT nextval('quizsushi_member_seq'), -- 회원 ID
    email         VARCHAR(100),                                                 -- 이메일
    nickname      VARCHAR(50),                                                  -- 닉네임
    profile_image VARCHAR(100),                                                 -- 프로필이미지 Url
    birth_date    DATE,                                                         -- 생년월일
    gender        VARCHAR(10),                                                  -- 성별 (M/F 등)
    plan_tier     VARCHAR(50) NOT NULL DEFAULT 'FREE',                          -- 요금제 티어 (ENUM)
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,               -- 생성일
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,               -- 수정일
    is_deleted    BOOLEAN              DEFAULT FALSE,                           -- 탈퇴 여부
    is_ban        BOOLEAN              DEFAULT FALSE                            -- 탈퇴 여부
);

--  SNS 로그인 연동 정보 테이블 (OAuth용)
CREATE SEQUENCE IF NOT EXISTS oauth_accounts_seq START 1 INCREMENT 1;
CREATE TABLE oauth_account
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('oauth_accounts_seq'),  -- 고유 ID
    member_id   BIGINT REFERENCES quizsushi_member (id) ON DELETE CASCADE, -- 연동된 사용자 ID
    provider    VARCHAR(10),                                               -- 로그인 제공자 (예: "kakao", "google")
    provider_id VARCHAR(100),                                              -- SNS 내부 유저 ID
    UNIQUE (provider, provider_id)
);

--  카테고리 정보 테이블
CREATE SEQUENCE IF NOT EXISTS category_seq START 1 INCREMENT 1;
CREATE TABLE category
(
    id          BIGINT PRIMARY KEY   DEFAULT nextval('category_seq'), -- 카테고리 ID
    title       VARCHAR(50) NOT NULL,                                 -- 카테고리 이름
    description TEXT        NOT NULL,                                 -- 설명
    icon        VARCHAR(10) NOT NULL,                                 -- 아이콘 이름
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- 생성일
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP        -- 수정일
);

--  퀴즈셋 정보 테이블
CREATE SEQUENCE IF NOT EXISTS quiz_seq START 1 INCREMENT 1;
CREATE TABLE quiz
(
    id             BIGINT PRIMARY KEY        DEFAULT nextval('quiz_seq'),                -- 퀴즈 ID
    member_id      BIGINT REFERENCES quizsushi_member (id) ON DELETE CASCADE,            -- 회원 작성자
    use_subject    BOOLEAN          NOT NULL,                                            -- 과목(소분류) 사용 여부
    media_key      VARCHAR(100)     NOT NULL UNIQUE,                                     -- 썸네일 키 or 미디어 키
    question_count INTEGER          NOT NULL DEFAULT 1,                                  -- 문제 수
    category_id    INTEGER          NOT NULL REFERENCES category (id) ON DELETE CASCADE, -- 카테고리 ID
    title          VARCHAR(100)     NOT NULL,                                            -- 제목
    description    TEXT                      DEFAULT '',                                 -- 설명
    rating         FLOAT            NOT NULL DEFAULT 0,                                  -- 평균 평점
    rating_count   INTEGER          NOT NULL DEFAULT 0,                                  -- 평점 수
    view_count     INTEGER          NOT NULL DEFAULT 0,                                  -- 조회수
    solve_count    INTEGER          NOT NULL DEFAULT 0,                                  -- 풀이수
    average_score  DOUBLE PRECISION NOT NULL DEFAULT 0,                                  -- 평균 점수
    created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--  문제 테이블 (퀴즈 내 개별 문항)
CREATE SEQUENCE IF NOT EXISTS question_seq START 1 INCREMENT 50;
CREATE TABLE question
(
    id                  BIGINT PRIMARY KEY   DEFAULT nextval('question_seq'),        -- 문제 ID
    quiz_id             BIGINT      NOT NULL REFERENCES quiz (id) ON DELETE CASCADE, -- 소속 퀴즈 ID
    no                  INTEGER     NOT NULL,                                        -- 퀴즈 내 번호
    subject             VARCHAR(50),                                                 -- 과목/소분류
    type                VARCHAR(10) NOT NULL,                                        -- 문제 유형
    question_text       TEXT        NOT NULL,                                        -- 문제 본문
    options             TEXT,                                                        -- 객관식 보기 (최대 10개 예상, JSON 문자열)
    correct_answers     TEXT,                                                        -- 객관식 정답 인덱스 (JSON 배열)
    correct_answer_text TEXT,                                                        -- 주관식 정답 텍스트
    explanation         TEXT,                                                        -- 해설
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 객관식 문제 테이블
CREATE TABLE question_multiple
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('question_seq'), -- 문제 ID
    quiz_id       BIGINT      NOT NULL REFERENCES quiz (id),          -- 소속 퀴즈 ID
    question_text TEXT        NOT NULL,                               -- 문제 본문
    explanation   TEXT,                                               -- 해설
    question_no   INTEGER     NOT NULL,                               -- 퀴즈 내 번호
    question_type VARCHAR(50) NOT NULL,                               -- 문제 유형
    subject       VARCHAR(255)                                        -- 과목/주제
);

-- 객관식 보기 옵션 테이블
CREATE SEQUENCE IF NOT EXISTS multiple_option_seq START 1 INCREMENT 50;
CREATE TABLE multiple_option
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('multiple_option_seq'),                 -- 문제 ID
    question_id BIGINT       NOT NULL REFERENCES question_multiple (id) ON DELETE CASCADE, -- 문제 ID
    text        VARCHAR(255) NOT NULL,                                                     -- 보기 내용
    is_correct  BOOLEAN      NOT NULL                                                      -- 정답 여부
);

-- 단답형 문제 테이블
CREATE TABLE question_shorts
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('question_seq'), -- 문제 ID
    quiz_id       BIGINT      NOT NULL REFERENCES quiz (id),          -- 소속 퀴즈 ID
    question_text TEXT        NOT NULL,                               -- 문제 본문
    explanation   TEXT,                                               -- 해설
    question_no   INTEGER     NOT NULL,                               -- 퀴즈 내 번호
    question_type VARCHAR(50) NOT NULL,                               -- 문제 유형
    subject       VARCHAR(255)                                        -- 과목/주제
);

-- 단답형 정답 문자열 테이블
CREATE TABLE shorts_answer
(
    question_id BIGINT       NOT NULL REFERENCES question_shorts (id) ON DELETE CASCADE, -- 문제 ID
    answer      VARCHAR(255) NOT NULL,                                                   -- 정답 문자열
    PRIMARY KEY (question_id, answer)
);

-- 순서 맞추기 문제 테이블
CREATE TABLE question_ordering
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('question_seq'), -- 문제 ID
    quiz_id       BIGINT      NOT NULL REFERENCES quiz (id),          -- 소속 퀴즈 ID
    question_text TEXT        NOT NULL,                               -- 문제 본문
    explanation   TEXT,                                               -- 해설
    question_no   INTEGER     NOT NULL,                               -- 퀴즈 내 번호
    question_type VARCHAR(50) NOT NULL,                               -- 문제 유형
    subject       VARCHAR(255)                                        -- 과목/주제
);

-- 순서 보기 옵션 테이블
CREATE SEQUENCE IF NOT EXISTS ordering_option_seq START 1 INCREMENT 50;
CREATE TABLE ordering_option
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('ordering_option_seq'),                 -- 문제 ID
    question_id BIGINT       NOT NULL REFERENCES question_ordering (id) ON DELETE CASCADE, -- 문제 ID
    text        VARCHAR(255) NOT NULL,                                                     -- 보기 내용
    ordering    INTEGER      NOT NULL                                                      -- 정답 순서
);

-- 매칭 문제 테이블
CREATE TABLE question_matching
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('question_seq'), -- 문제 ID
    quiz_id       BIGINT      NOT NULL REFERENCES quiz (id),          -- 소속 퀴즈 ID
    question_text TEXT        NOT NULL,                               -- 문제 본문
    explanation   TEXT,                                               -- 해설
    question_no   INTEGER     NOT NULL,                               -- 퀴즈 내 번호
    question_type VARCHAR(50) NOT NULL,                               -- 문제 유형
    subject       VARCHAR(255)                                        -- 과목/주제
);

-- 매칭 쌍 테이블
CREATE SEQUENCE IF NOT EXISTS matching_pair_seq START 1 INCREMENT 50;
CREATE TABLE matching_pair
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('matching_pair_seq'),                   -- 문제 ID
    question_id BIGINT       NOT NULL REFERENCES question_matching (id) ON DELETE CASCADE, -- 문제 ID
    left_text   VARCHAR(255) NOT NULL,                                                     -- 왼쪽 항목
    right_text  VARCHAR(255) NOT NULL                                                      -- 오른쪽 항목
);


--  사용자 평점 테이블
CREATE SEQUENCE IF NOT EXISTS quiz_rating_seq START 1 INCREMENT 1;
CREATE TABLE quiz_rating
(
    id         BIGINT PRIMARY KEY DEFAULT nextval('quiz_rating_seq'),     -- 평점 ID
    quiz_id    BIGINT    NOT NULL REFERENCES quiz (id) ON DELETE CASCADE, -- 퀴즈 ID
    member_id  BIGINT    NOT NULL,                                        -- 익명 사용자 식별 키
    rating     INTEGER   NOT NULL CHECK (rating BETWEEN 1 AND 5),         -- 평점
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (quiz_id, member_id)
);

--  AI 프롬프트 템플릿 테이블
CREATE SEQUENCE IF NOT EXISTS ai_prompts_seq START 1 INCREMENT 1;
CREATE TABLE ai_prompts
(
    id          BIGINT PRIMARY KEY   DEFAULT nextval('ai_prompts_seq'),
    name        VARCHAR(50) NOT NULL UNIQUE, -- 프롬프트 키
    template    TEXT        NOT NULL,        -- 프롬프트 본문
    description VARCHAR(255),                -- 설명
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);


--  회원 퀴즈 풀이 기록 테이블
CREATE SEQUENCE IF NOT EXISTS member_quiz_solve_log_seq START 1 INCREMENT 1;
CREATE TABLE member_quiz_solve_log
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('member_quiz_solve_log_seq'),
    member_id    BIGINT REFERENCES quizsushi_member (id),        -- 사용자 ID
    quiz_id      BIGINT REFERENCES quiz (id) ON DELETE SET NULL, -- 퀴즈 ID
    score        INTEGER,                                        -- 점수
    submitted_at TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    -- 제출 시각
);

-- 비회원 퀴즈 풀이 기록 테이블
CREATE SEQUENCE IF NOT EXISTS guest_quiz_solve_log_seq START 1 INCREMENT 1;
CREATE TABLE guest_quiz_solve_log
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('guest_quiz_solve_log_seq'),
    quiz_id      BIGINT REFERENCES quiz (id) ON DELETE SET NULL,
    guest_ip     TEXT,
    guest_ua     TEXT,
    score        INTEGER,
    submitted_at TIMESTAMP          DEFAULT CURRENT_TIMESTAMP
);

-- 관리자
CREATE SEQUENCE IF NOT EXISTS admin_member_seq START 1 INCREMENT 1;
CREATE TABLE admin_member
(
    id         BIGINT PRIMARY KEY          DEFAULT nextval('admin_member_seq'),
    alias      VARCHAR(50) UNIQUE NOT NULL,
    username   VARCHAR(50) UNIQUE NOT NULL,
    password   VARCHAR(255)       NOT NULL,
    role       VARCHAR(20)        NOT NULL DEFAULT 'VIEWER', -- 1|2|4->7<15
    created_at TIMESTAMP                   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--  사용자 신고 테이블
CREATE SEQUENCE IF NOT EXISTS report_seq START 1 INCREMENT 1;
CREATE TABLE report
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('report_seq'),
    reason      VARCHAR(20),                             -- 신고 이유
    title       TEXT      NOT NULL,                      -- 제목
    message     TEXT      NOT NULL,                      -- 메시지
    reporter_id BIGINT REFERENCES quizsushi_member (id), -- 신고자 ID
    target_type VARCHAR(20),                             -- ex) QUIZ, MEMBER 등
    target_id   BIGINT,
    read        boolean   NOT NULL DEFAULT false,
    status      VARCHAR(20),
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE IF NOT EXISTS challenge_toggle_seq START 1 INCREMENT 1;
CREATE TABLE challenge_toggle
(
    id         BIGINT PRIMARY KEY DEFAULT nextval('challenge_toggle_seq'),
    is_enabled BOOLEAN   NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES admin_member (id)
)
