# Team2 최종 프로젝트 "갈래"

백엔드: 
* 장지예(프로젝트 리더): 로그인/회원가입, 소셜로그인, 이메일 인증 기능(Redis)
* 김정환(프로젝트 총무): 게시글 CRUD, 검색, 좋아요 랭킹 정렬, Swagger UI
* 김용글(프로젝트 서기): 댓글/대댓글 CRUD, CI/CD
* 문승태(프로젝트 발표): 날짜별 계획 및 세부일정 CRUD, 사진 관련 기능

## 프로젝트 간략 설명
2-30대의 여행 계획자들을 위한 여행 계획 세우기 및 공유 서비스

### 적용 기술

- 아키텍쳐
![project_architecture](https://github.com/Jang-JIye/Team2_Final/assets/53979008/b4c80832-f4bb-45c9-b423-9940d5c83a73)

- 프론트엔드: React, Tailwind, Recoil, React Router, Vercel, Axios, React Query
- 백엔드: Java Spring Boot, JPA, QueryDSL, Gradle
- 인터페이스: Swagger UI, POSTMAN
- 서버: AWS EC2
- 캐시: Redis
- 데이터베이스: MySQL(AWS RDS), AWS S3
- 인증/인가: JWT, Spring Security, OAuth
- 테스트: JUnit5, Mockito
- 배포(CI/CD): Github Actions

### ERD
![final_erd](https://github.com/Jang-JIye/Team2_Final/assets/53979008/37895b8a-2fe0-4da7-945b-17d419e6071b)

### 사용법
1. `application.properties` 에서 하기 설정 필요
```
# MySQL + AWS RDS 정보
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# JWT관련 정보
jwt.secret.key=
ADMIN_TOKEN=

#이메일 관련 정보
# Gmail SMTP
spring.mail.host=smtp.gmail.com
# Gmail SMTP
spring.mail.port=587
# Gmail hanghae99**
spring.mail.username=
# Gmail
spring.mail.password=
# SMTP
spring.mail.properties.mail.smtp.auth=true
# SMTP StartTLS
spring.mail.properties.mail.smtp.starttls.enable=true

# Springdoc properties(Swagger UI관련)
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html

#S3(사용할 S3 Bucket정보)
cloud.aws.credentials.access-key=
cloud.aws.credentials.secret-key=
cloud.aws.stack.auto=false
cloud.aws.region.static=
cloud.aws.s3.bucket=

# Social Kakao(사용할 카카오 API정보)
kakaoClientId=
kakaoRedirectUri=

# Kakao map(카카오맵 API키)
kakaoRestApiKey=

# Redis
spring.data.redis.host=svc.sel5.cloudtype.app
spring.data.redis.port=

### 프로젝트 상세정보:
배포된 웹 사이트: https://gallae-trip.com
프로젝트 기획 문서(노션): https://wind-song-8bf.notion.site/2-2571118e6ef94d2082bc372fca7260d8
