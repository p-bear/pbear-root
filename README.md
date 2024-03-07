# PBear Project Season2

[![Wercker](https://img.shields.io/badge/spring--boot-3.2.2-%236DB33F.svg?style=flat-square&logo=springboot&color=%236DB33F)](https://spring.io/projects/spring-boot)
[![Wercker](https://img.shields.io/badge/mariaDB-10.6.16-%234479A1.svg?style=flat&logo=mariadb&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/Apache_Kafka-7.3.2-black.svg?style=flat&logo=apachekafka&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/mongoDB-6.0.12-%2300684A.svg?style=flat&logo=mongodb&logoColor=%2300ED64)]()
[![Wercker](https://img.shields.io/badge/redis-6.0.16-%23D82C20.svg?style=flat&logo=redis&logoColor=%23D82C20)]()
[![Wercker](https://img.shields.io/badge/docker-24.0.7-%231D63ED.svg?style=flat&logo=docker&logoColor=%23E5F2FC)]()
[![Wercker](https://img.shields.io/badge/Apache_zookeeper-7.3.2-black.svg?style=flat&logo=apache&logoColor=white)]()


## Project Overview

### Architecture
![Overview Image](https://github.com/p-bear/charts.draw.io/blob/main/Season2Overview.png?raw=true)


### Involved Library Stack

- JDK 17
- Spring Boot 3.2.2
- Spring Webflux
- Spring Cloud Gateway
- Spring Authorization Server
- auto-service *(AnnotationProcessor Support)*
- R2DBC
- Reactor-Kafka
- zookeeper-discovery *(Service Discovery)*
- Micrometer *(Tracing)* *(+prometheus)*

## Key Idea

### One Source Multi-Build

#### 목적
- 프로젝트마다 중복되는 코드와 작업들을 최소화 // Project Structure 참고

#### HOW-TO
- Gradle의 Multi-Project 구조 구성

1. **Gradle Plugin으로 Docker Image Build를 Root에 구성**하여, 하위 프로젝트에게 자동으로 적용. 이를 통해 Jenkins Pipeline의 parameter만 변경하여 신규 프로젝트를 빌드할 수 있도록 구성
2. **AnnotationProcessor를 통해 자동생성 된 Server Enum**으로 service discovery 간소화
3. **EnvironmentPostProcessor로 ClassPath의 모든 Yaml 설정을 import**하여 공통으로 쓰이는 Server Properties를 공통으로 사용
4. starter project 통해 각 Service에서 필요한 공통 기능 implementation

### Auto Service Discovery

#### 목적
- MSA 구조에서 각 Service의 Runtime 접속정보를 실시간으로 Discover 하기 위함


#### HOW-TO

1. Zookeeper Client를 통해 Service의 Runtime 접속정보를 자동 등록
2. **AnnotationProcessor를 통해 자동생성 된 Server Enum**으로 service Targeting
3. 각 서버의 rest endpoint prefix를 구성할 수 있도록 함


### Gateway with Passport (작업중)

#### 목적
- Authentication Context를 Gateway에서 통합 처리하여 각 Service에게 제공


#### HOW-TO
1. Authentication은 별도의 OAuth Server에서 담당 
2. OAuth Server는 토큰을 검증 및 passport(Autentication Context)를 제공하는 기능 구현
3. Gateway는 Request의 AccessToken이 있으면 OAuth Server로 Passport를 받아서 Forwarding
4. Authorization은 각 Service에서 담당

#### Constraint
- 확장성을 위해 Autorization을 각 Service가 책임지며, 공통 인증/인가가 불가
- Gateway는 Service의 인증/인가 Error Response를 무조건 OAuth로 redirect 해주어야 한다.


## Project Structure

```
pbear-root
├──pbear-spring
│   ├──pbear-app-oauth          # OAuth Server Application
│   ├──pbear-app-user           # User Server Application
│   ... (to be continue)
│   ├──pbear-devtool            # Custom AnnotationProcessor for project
│   ├──pbear-lib                # Common Modules & Properties
│   │
│   ├──pbear-starter-kafka      # starter library for kafka
│   │                             - Producer/Consumer Properties Provider
│   │                             - Common Event Sender (TOBE)
│   ├──pbear-starter-r2dbc      # starter library for rdb
│   │                             - Common R2DBC Config bean
│   ├──pbear-starter-webflux    # starter library for webflux
│   │                             - Service Discovery Module (for REST Client)
│   │                             - REST Global Exception Handling
│   │                             - WebClient Config
│   ... (to be continue)
│   └──build.gradle             # gradle script for Spring
├──build.gradle                 # gradle script for Global
├──settings.gradle              # gradle Settings
└──README.md
```
```// TODO: Project Structure Description 자동 생성 및 insert 모듈```

## Run Project

1. Prepare Infrastructure
   - Zookeeper, MariaDB... etc
2. Fill Application-secret.yml
```
r2dbc:
  host: 
  port: 
  user: 
  password: 
spring:
  cloud:
    zookeeper:
      connect-string:
```
3. run main class in project


## API Spec

- Postman을 통한 관리


## CI / CD

### CI
- Jenkins Pipeline
  - Source From GitHub
  - cp secret Yaml to Workspace
  - Gradle Build
  - build DockerImage with Gradle Plugin

### CD
- Jenkins Pipeline
  - Stop and Remove Current Container
  - run Container with target version


## Links
### for more information

- Blog [Project Season2 (2023.12~)](https://p-bear.tistory.com/category/%5B%EC%8B%9C%EC%A6%8C2%5D%20%EA%B0%9C%EC%9D%B8%EC%84%9C%EB%B2%84%20%EA%B0%9C%EB%B0%9C)
  - GitHub: [Project Season2](https://github.com/p-bear/pbear-root)
- Blog: [Main](https://p-bear.tistory.com/)
- Blog: [Tech Reviews](https://p-bear.tistory.com/category/%EA%B0%9C%EB%B0%9C%20%EC%9D%BC%EC%A7%80)
- Blog: [Project Season1 (2022.10~2023.9)](https://p-bear.tistory.com/category/%5B%EC%99%84%5D%20%EA%B0%9C%EC%9D%B8%EC%84%9C%EB%B2%84%20%EA%B0%9C%EB%B0%9C?page=1)
  - GitHub: [main-server](https://github.com/p-bear/main-functional-server)
  - GitHub: [Gateway + Auth Server](https://github.com/p-bear/gateway-auth-server)
  - GitHub: [Session Connector Server](https://github.com/p-bear/session-connector-server)
  - GitHub: [data collect server](https://github.com/p-bear/data-collect-server)