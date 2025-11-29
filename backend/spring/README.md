
# 🍣 QuizSushi - Backend
<p >
  <img width="800" height="500" alt="QuizSushi 메인 화면" src="https://github.com/user-attachments/assets/336586b3-5ab2-41c4-b246-87ff26d2adda" />
</p>   
자격증 공부를 하며 기출문제를 반복해서 풀고다 반복 횟수가 늘어날수록 지루하게 느껴졌고     

기출문제를 찾고 정리하는 데 오히려 시간이 낭비되고 있다는 생각이 들었습니다.

**단순히 문제를 푸는 것보다, 직접 만들어보는 과정이 훨씬 더 깊은 이해가 될 수 있다는** 생각을하게 되었습니다.  
**더 다양한 문제를 접하고, 능동적으로 학습할 수 있는 방법이 필요하다고** 느낀 것이 QuizSushi의 시작이었습니다.

하지만 기존 퀴즈 플랫폼들은 다음과 같은 한계가 있었습니다.  
- 사용자가 문제를 직접 만들고 공유하기 어렵고  
- AI나 실시간 경쟁 요소가 없으며  
- 사용자 간 피드백이나 지식 흐름도 원활하지 않았습니다.

> “문제를 여럿이서 풀어보면 어떨까?”  
> “AI가 만들어주는 문제를 바로 풀 수는 없을까?”  
> “내가 만든 문제를 다른 사람들이 푸는 걸 확인하면 더 재밌지 않을까?”

이러한 고민을 해결하고자 **QuizSushi**를 만들었습니다.  
사용자가 직접 퀴즈를 만들고 풀며 지식을 나누는 플랫폼으로  
**AI 기반 문제 생성**, ~~실시간 서바이벌 퀴즈 챌린지를~~ 통해  
**혼자만이 아닌, 함께 학습하는 경험을** 제공합니다.


<br>   


### 📂 관련 링크

- 🔗 **Frontend Repository**: [quizsushi-fe (Next.js)](https://github.com/dlaudfuf33/quizsushi-fe)
- 🚀 **배포 주소**: [https://quizsushi.cmdlee.com](https://quizsushi.cmdlee.com)


---

## 🛠️ 사용 기술

| 카테고리 | 라이브러리 | 설명 |
| :--- | :--- | :--- |
| **Framework**       | Spring Boot (v3.2.5)          | Spring 기반 웹 애플리케이션 프레임워크 |
|                     | Spring Data JPA               | ORM 기반 DB 연동 |
|                     | Spring AI                     | Spring에서 AI API 연동을 위한 통합 추상화 |
|                     | Spring Security               | 인증/인가 프레임워크 |
|                     | Spring OAuth2 Client          | Google, Kakao 소셜 로그인 연동 |
| **Security**        | JWT (io.jsonwebtoken)         | JSON Web Token 기반 인증 및 인가 |
|                     | Refresh Token + Redis         | IP + User-Agent 기반 리프레시 토큰 식별 및 상태 관리 |
| **Database**        | PostgreSQL                    | 메인 관계형 데이터베이스 |
|                     | Redis                         | 캐시, 실시간 챌린지 세션, 토큰 저장 |
| **Infrastructure**  | NGINX                         | 리버스 프록시 및 HTTPS 인증 처리 |
|                     | MinIO                         | S3 호환 오브젝트 스토리지 |
|                     | Docker                        | 컨테이너 환경 구성 및 배포 |
|                     | GitHub Actions                | GitHub 기반 배포 자동화 워크플로우 구성 |
| **AI**  | Vertex AI Gemini              | Google Cloud의 생성형 AI 모델 |
|                     | Ollama                        | 로컬 환경에서의 경량 생성형 AI 통합 |
| **Documentation**   | SpringDoc OpenAPI             | Swagger v3 기반 API 명세 자동화 |
| **Build Tool**      | Gradle                        | 의존성 관리 및 빌드 도구 |

<br>   



## 📜 아키택처
<img width="882" height="575" alt="qst drawio" src="https://github.com/user-attachments/assets/d76a7904-a211-490a-9b15-58900a51cb5c" />



---

## ✨ 주요 기능

- **AI 퀴즈 생성**: 텍스트만으로 간편하게 객관식, 주관식 퀴즈를 생성할 수 있습니다.
- **실시간 서바이벌 퀴즈 챌린지**: 여러 사용자와 함께 참여하는 실시간 퀴즈 대결 기능.
- **사용자 생성 퀴즈**: 누구나 자신만의 퀴즈를 만들어 공유하고, 다른 사용자의 퀴즈를 풀이할 수 있습니다.
- **안전한 인증 시스템**: OAuth2 소셜 로그인 + JWT 기반 인증으로 보안과 편의성을 모두 확보.
- **관리자 대시보드**: 사용자/문제/신고 통계 등을 시각화하여 관리할 수 있는 전용 페이지.
- **MinIO 기반 미디어 관리**: 퀴즈에 포함되는 이미지, 영상 등을 객체 스토리지 기반으로 처리합니다.

<br>



## 📝 주요 사항

### 팩토리 패턴 도입 ([QuizFactory](https://github.com/dlaudfuf33/quizsushi-be/blob/main/src/main/java/com/cmdlee/quizsushi/quiz/domain/factory/DefaultQuizFactory.java): / [QuestionFactory](https://github.com/dlaudfuf33/quizsushi-be/blob/main/src/main/java/com/cmdlee/quizsushi/quiz/domain/factory/QuizQuestionFactory.java))
퀴즈 생성 시 클라이언트로부터 전달받은 데이터가 복잡한 구조(퀴즈 본문, 다수의 문제, 각 문제의 선택지 및 정답)로 이루어져 있었고, 이를 엔티티로 매핑하는 과정에서 서비스 계층의 책임이 컸습니다.
- 객체 생성 책임을 명확히 분리하고, 문제 번호 중복과 같은 도메인 규칙을 외부에 노출하지 않기 위해 팩토리 패턴을 도입하였습니다. QuizFactory / QuestionFactory를 통해 생성 과정을 캡슐화하고, 서비스는 전처리된 데이터를 전달만 하도록 구조를 수정했습니다.
- 이렇게 함으로써 생성 로직이 각 객체 내부에 응집되었고, 테스트 가능한 단위로 분리되었으며, 전체 서비스 코드의 가독성과 유지보수성이 크게 향상되었습니다.

<details>
<summary> 팩토리 패턴 도입 이전 코드 보기</summary>
  
```java
@Transactional
public void createQuiz(CreateQuizRequest request, Long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        String mediaKey = NanoIdUtils.randomNanoId();
        Quiz quiz = Quiz.builder()
            .creator(member)
            .title(request.getTitle())
            .category(category)
            .description(request.getDescription())
            .useSubject(request.isUseSubject())
            .mediaKey(mediaKey)
            .questionCount(request.getQuestions().size())
            .build();

        for (CreateQuestionRequest qReq : request.getQuestions()) {
            Question question = Question.builder()
                    .no(qReq.getNo())
                    .subject(qReq.getSubject())
                    .type(qReq.getType())
                    .questionText(qReq.getQuestion())
                    .options(qReq.getOptions())
                    .correctIndexes(qReq.getCorrectAnswer())
                    .correctAnswerText(qReq.getCorrectAnswerText())
                    .explanation(qReq.getExplanation())
                    .build();
                                
            quiz.addQuestions(question);
        }
        quizRepository.save(quiz);
    }
```
</details>

<details>
<summary> 팩토리 패턴 적용 이후 코드 보기</summary>

```java
@Transactional
    public CreatedQuizResponse createQuiz(CreateQuizRequest request, Long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        String mediaKey = NanoIdUtils.randomNanoId();

        QuizCreationData data = quizPreprocessingService.process(request, member, category, mediaKey);
        Quiz quiz = quizFactory.create(data);
        quizRepository.save(quiz);

        List<QuestionCreationData> questionDataList = questionPreprocessingService.processForCreate(request.getQuestions(), quiz.isUseSubject(), mediaKey);
        List<Question> questions = questionDataList.stream().map(qData -> questionFactory.create(qData, quiz)).toList();
        questionRepository.saveAll(questions);
        return new CreatedQuizResponse(quiz.getId());
    }
```
</details>

<img width="800" height="600"  alt="createQuiz" src="https://github.com/user-attachments/assets/6caabc91-bea7-4c26-a4b0-e364511bfac2" />



--- 


###  실시간 퀴즈 세션 상태 저장 구조 ([ChallengeSession](https://github.com/dlaudfuf33/quizsushi-be/blob/main/src/main/java/com/cmdlee/quizsushi/quiz/challenge/model/ChallengeSession.java))
실시간 퀴즈 챌린지 기능에 문제 출제, 유저 응답, 채팅, 점수 등 게임 진행 중 발생하는 다양한 상태들을 관리할 수 있는 구조가 필요했습니다.   
이 상태들은 실시간으로 변화하고 여러 유저가 동시에 접근하므로, 단순 컬렉션 기반으로는 안정성을 확보하기 어렵다 생각이 들었습니다.   

- 게임 상태, 진행로그, 채팅로그를 정확하게 저장하고 누락이 없도록 처리가 필요했습니다.
- 유저의 상호작용이 몰릴 때에도 클라이언트에 정확한 최신 상태 전송이 필요했습니다.


- ChallengeSession 객체를 Redis에 JSON 형태로 저장하도록 구조를 변경했습니다.
- 내부 필드 중 동시 접근 가능성이 높은 리스트/맵 구조는 다음과 같이 동시성 제어가 가능한 컬렉션으로 교체하였습니다:
  ```java
  private Map<String, PlayerStatus> players = new ConcurrentHashMap<>();
  private List<BroadcastMessage> broadcastLog = Collections.synchronizedList(new ArrayList<>());
  private List<ChatMessageRequest> chatLog = Collections.synchronizedList(new ArrayList<>());
  ```

- 이 구조가 실제로도 멀티스레드 환경에서 안정적으로 동작하는지를 검증하기 위해 멀티스레드 환경에서 동시 채팅 시에도 모든 메시지가 정확히 기록되는지를 테스트를 작성했습니다.
  ```java
      @DisplayName("동시 채팅 시에도 모든 메시지가 정확히 기록된다")
    @Test
    void receiveChat_concurrentAccess_shouldHandleWithoutErrors() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String sessionId = "sessionId2025";

        when(challengeSessionRedisService.getSession(sessionId)).thenReturn(testSession);
        for (int i = 0; i < threadCount; i++) {
            QuizsushiMember member = QuizsushiMember.builder()
                    .id((long) i)
                    .nickname("user-" + i)
                    .profileImage("avatar.png")
                    .build();
            PlayerStatus status = new PlayerStatus(member, 100);
            testSession.getPlayers().put(String.valueOf(i), status);
        }

        // when
        for (int i = 0; i < threadCount; i++) {
            final int userNum = i;
            executor.submit(() -> {
                try {
                    ChatMessageRequest message = new ChatMessageRequest();
                    message.setSessionId(sessionId);
                    message.setSenderId(String.valueOf(userNum));
                    message.setNickname("user-" + userNum);
                    message.setContent("화이팅해요 " + userNum);
                    challengeGameService.receiveChat(message);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        List<ChatMessageRequest> logs = testSession.getChatLog();
        assertEquals(threadCount, logs.size(), "모든 메시지가 누락 없이 기록되어야 함");

        for (int i = 0; i < threadCount; i++) {
            String expected = "화이팅해요 " + i;
            boolean exists = logs.stream().anyMatch(msg -> msg.getContent().equals(expected));
            assertTrue(exists, "메시지 누락 여부 확인: " + expected);
        }
    }
  ```
  > 10명의 사용자가 동시에 채팅을 전송했을 때 모든 메시지가 누락 없이 로그에 저장되는지를 검증했습니다.
  > 이 테스트를 통해 동시 접근 상황에서도 Collections.synchronizedList 구조가 안정적으로 동작한다는 것을 확인했습니다.

  <img width="800" height="600" alt="chat" src="https://github.com/user-attachments/assets/f2ce420b-ca1b-4e37-b193-d4814a9afe95" />


---


###  자동 매칭 구조 ([MatchingQueueService](https://github.com/dlaudfuf33/quizsushi-be/blob/main/src/main/java/com/cmdlee/quizsushi/quiz/challenge/service/MatchingQueueService.java))
실시간 퀴즈 챌린지 기능에서, 유저가 참여를 요청하면 이를 일정 기준에 따라 자동으로 매칭시켜주는 로직이 필요했습니다.
- 여러 사용자가 매칭을 요청할 경우 누가 먼저 들어왔는지 파악하고 공정한 순서대로 매칭시켜야 했습니다.
- 최소 2명 이상이 모였을 때 또는 30초 이상 대기한 경우 와 같이 유연한 조건을 설정해 매칭을 시작할 수 있어야 했습니다.
- 초기에는 참여자가 적을 것이기 때문에, 혼자서도 도전할 수 있도록 조건을 완화하여 빠르게 게임을 시작할 수 있어야 했습니다.

이러한 요구사항을 단순한 List 나 SortedSet 컬렉션 같은 JVM 메모리 기반 구조로 처리하기에는
- 서버 인스턴스가 재시작되거나 다중 인스턴스로 확장될 경우, JVM 내 컬렉션은 상태 공유가 불가능합니다
- 삽입 순서를 기준으로 오래된 유저를 추출하거나 조건에 맞는 유저만 골라내기 위해서는 전체 순회가 필요하며 약 O(n)

이러한 이유로, Redis의 Sorted Set(ZSet) 을 기반으로 한 구조를 사용했습니다.
- 유저 매칭 등록
Redis ZSet에 memberId를 key로, 현재 시간을 score로 저장합니다.
  ```java
    public void enqueue(long memberId) {
          String key = String.valueOf(memberId);
          Boolean exists = redisTemplate.opsForZSet().score(MATCHING_QUEUE_KEY, key) != null;
          if (Boolean.TRUE.equals(exists)) return;
  
          redisTemplate.opsForZSet().add(MATCHING_QUEUE_KEY, key, Instant.now().getEpochSecond());
      }
  ```
  > 이미 대기 중인 유저는 중복 등록 방지 시간 순 정렬은 score를 기준으로 이뤄집니다.   
  > JVM 리스트 기반이라면 삽입 시 중복 체크 + 정렬 비용이 필요하지만, ZSet은 O(log n)으로 해결됩니다.

- 주기적 매칭 검사   
3초마다 실행되며, 대기열 상태를 조회하여 매칭 조건을 만족하는 경우 세션을 시작합니다.
  ```java
  @Scheduled(fixedRate = 3000)
    public void tryMatch() {
        long now = Instant.now().getEpochSecond();
        Set<ZSetOperations.TypedTuple<String>> matchingPool = redisTemplate.opsForZSet()
                .rangeWithScores(MATCHING_QUEUE_KEY, 0, -1);

        if (matchingPool == null || matchingPool.isEmpty()) return;

        List<String> matched = new ArrayList<>();
        long firstWaited = -1L;
        boolean has30SecWaiter = false;

        for (ZSetOperations.TypedTuple<String> entry : matchingPool) {
            String memberId = entry.getValue();
            messagingTemplate.convertAndSend("/topic/matching/" + memberId,
                    MatchingMessageResponse.from(MatchStatusType.MATCHING));

            Double score = entry.getScore();
            if (memberId == null || score == null) continue;

            long waitedSec = now - score.longValue();
            if (firstWaited == -1) {
                firstWaited = waitedSec;
            }
            if (waitedSec >= 3) {
                has30SecWaiter = true;
            }
            matched.add(memberId);
        }

        boolean canMatchByGroup = matched.size() >= 3 && firstWaited >= 5;
        boolean canMatchByTimeout = has30SecWaiter;

        if (canMatchByGroup || canMatchByTimeout) {
            List<String> toBeMatched = matched.subList(0, Math.min(matched.size(), MAX_PLAYERS));
            toBeMatched.forEach(id -> redisTemplate.opsForZSet().remove(MATCHING_QUEUE_KEY, id));
            try {
                createSession(toBeMatched);
            } catch (GlobalException e) {
                for (String id : toBeMatched) {
                    long memberId = Long.parseLong(id);
                    enqueue(memberId);
                    messagingTemplate.convertAndSend("/topic/matching/" + memberId,
                            MatchingMessageResponse.of(MatchStatusType.MATCHING, null, "현재 세션이 꽉 찼습니다. 대기 중입니다."));
                }
            }
        }
    }
  ```
  > ZRANGE로 score 기준으로 정렬된 전체 대기열을 가져오고 ZREM으로 매칭된 유저를 제거합니다.   
  > 리스트 기반이라면 조건에 맞는 유저 필터링에 O(n) Redis ZSet은 삽입, 정렬, 삭제 모두 O(log n)

  <img width="800" height="600" alt="match" src="https://github.com/user-attachments/assets/ad87b185-71af-48c1-878a-b93586d84f21" />


  

### AI 연동 구조 개선

AI 모델을 활용해 퀴즈를 생성하는 기능을 제공하고 있으며 서비스 환경과 비용 문제를 고려하여 구조를 점진적으로 개선해왔습니다.   
일반 퀴즈 생성과 실시간 챌린지 문제 생성은 요구 조건이 다르다는 점을 인지하고, 각각에 최선의 전략을 선택하게 되었습니다.   

1. 병목 문제 발생
QuizSushi의 배포 환경은 외장 GPU가 없는 저전력 미니 PC로 구성되어 있으며, 로컬 모델 구동시 CPU 기반 연산으로만 실행됩니다.
요청이 몰리면 지연이 심각해 10분 이상 걸리는 현상이 발생했습니다.
    <img width="800" height="600" alt="Mermaid Chart - Create complex, visual diagrams with text  A smarter way of creating diagrams -2025-07-24-045206" src="https://github.com/user-attachments/assets/5adc0c2a-1aac-4e0f-a29c-6e9662545d4a" />



2. 수평 확장 시도
이 문제를 해결하기 위해 구조 개선을 고민했습니다:
- Ollama가 요청을 한 번에 하나씩만 처리한다면 여러 인스턴스를 두고 요청을 나누면 병목을 줄일 수 있지 않을까? 라는 생각을 했습니다.
  - 병목 문제를 해소하기 위해, Raspberry Pi에 분산 배포하는 방안을 실험해보았으나 초경량 모델만 구동이 가능했고
  - 다음으로 Docker 기반으로 동일 머신 내에서 여러 Ollama 컨테이너를 띄우고 라운드로빈을 모방한 방식을 사용했습니다.
    ```java
      @Component
      @RequiredArgsConstructor
      public class AiInstanceRouter {

        private final AiProperties aiProperties;
        private final AtomicInteger index = new AtomicInteger(0);
    
        public String nextUrl() {
            List<String> urls = aiProperties.getUrls();
            int i = index.getAndUpdate(n -> (n + 1) % urls.size());
            return urls.get(i);
        }
      }
    ```
    > 등록된 Ollama 인스턴스의 주소목록을 순서대로 순회하며 요청을 나눕니다.
    <img width="800" height="600" alt="Mermaid Chart - Create complex, visual diagrams with text  A smarter way of creating diagrams -2025-07-24-061839" src="https://github.com/user-attachments/assets/b3cfc9a0-58e1-4051-a485-fa1798889dfc" />

  - 구조적으로 분산은 가능했지만, CPU 자원을 공유하는 한계로 인해 컨테이너 수를 늘려도 성능이 비례하지 않고 오히려 역효과가 발생했습니다.

  
3. Gemini를 병행 도입 ([GeminiAiService](https://github.com/dlaudfuf33/quizsushi-be/blob/main/src/main/java/com/cmdlee/quizsushi/ai/service/GeminiAiService.java))
실시간 퀴즈 챌린지 기능은 빠른 응답성과 고품질 생성이 필수였습니다.
CPU 기반 구조로는 이를 만족하기 어려웠고, 수평 확장도 한계에 도달했기 때문에 Google Vertex AI Gemini를 병행 도입하였습니다.
- Gemini는 Google의 멀티모달 LLM으로, Vertex AI 플랫폼을 통해 API 형태로 연동할 수 있습니다.   
- 퀴즈 챌린지의 다음 문제 생성 시, Gemini에게 프롬프트를 전달해 실시간으로 퀴즈를 생성합니다.
  ```java
    public ChallengeQuiz generateChallengeQuizWithGemini(int level) {
          String basePrompt = dbPromptTemplateProvider.getTemplate("quizChallenge_generation_gemini");
          String randomType = QUESTION_TYPES.get(ThreadLocalRandom.current().nextInt(QUESTION_TYPES.size()));
          String finalPrompt = basePrompt.replace("{{randomType}}", randomType)
                  .replace("{{level}}", String.valueOf(level));
  
          for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
              try {
                  return callAndParse(finalPrompt);
              } catch (GlobalException e) {
                  log.warn("[GeminiAiService] Attempt {} failed: {}", attempt, e.getMessage());
                  if (attempt == MAX_RETRIES) throw e;
              } catch (Exception e) {
                  log.error("[GeminiAiService] Unexpected error on attempt {}", attempt, e);
                  if (attempt == MAX_RETRIES) throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED, e);
              }
          }
  
          throw new GlobalException(ErrorCode.AI_EMPTY_RESPONSE);
      }
  ```
  <img width="800" height="600" alt="Mermaid Chart - Create complex, visual diagrams with text  A smarter way of creating diagrams -2025-07-24-062517" src="https://github.com/user-attachments/assets/60c1237f-cac4-474f-bf4e-98659e4ce321" />




<br>

## 🚦주요 API 목록

| Method | Endpoint | 설명 |
| :--- | :--- | :--- |
| `POST` | `/api/ai/quizzes/questions` | AI를 통해 퀴즈 문제 생성 |
| `POST` | `/api/quizzes` | 사용자 퀴즈 생성 |
| `GET` | `/api/quizzes/{id}` | 퀴즈 상세 정보 조회 |
| `POST` | `/api/quizzes/{id}/results` | 퀴즈 풀이 결과 제출 |
| `GET` | `/api/auth/{provider}/login` | 소셜 로그인 시작 (provider: `google`, `kakao`) |
| `POST` | `/api/auth/logout` | 로그아웃 |
| `GET` | `/api/members/me` | 내 정보 조회 |
| `Message` | `/matching/join` | (WebSocket) 챌린지 매칭 참가 |
| `Message` | `/challenge/answer` | (WebSocket) 챌린지 정답 제출 |
| `GET` | `/cmdlee-qs/dashboard/data` | (관리자) 대시보드 통계 데이터 조회 |

<br>

## 🔖 커밋 규칙

| 타입 | 설명 |
| :--- | :--- |
| `feat` | 새로운 기능 추가 또는 기존 기능 명세 변경 |
| `fix` | 버그 수정 또는 의도한 동작이 안 되던 부분 수정 |
| `refactor` | 리팩토링 (기능 변화 없이 코드 구조/품질 개선) |
| `chore` | 빌드, 설정 파일, 인프라 관련 잡일 |
| `docs` | 문서 또는 주석만 변경한 경우 |
| `test` | 테스트 코드 추가 또는 수정 |
| `style` | 코드 포맷팅, 세미콜론, 공백 등 변경 (기능 변화 없음) |
| `perf` | 성능 개선 |
| `ci` | CI 설정 및 관련 스크립트 변경 |
