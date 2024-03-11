# Project Asset Management

[![Wercker](https://img.shields.io/badge/spring--boot-3.2.2-%236DB33F.svg?style=flat-square&logo=springboot&color=%236DB33F)](https://spring.io/projects/spring-boot)
[![Wercker](https://img.shields.io/badge/mariaDB-10.6.16-%234479A1.svg?style=flat&logo=mariadb&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/Apache_Kafka-7.3.2-black.svg?style=flat&logo=apachekafka&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/mongoDB-6.0.12-%2300684A.svg?style=flat&logo=mongodb&logoColor=%2300ED64)]()
[![Wercker](https://img.shields.io/badge/docker-24.0.7-%231D63ED.svg?style=flat&logo=docker&logoColor=%23E5F2FC)]()
[![Wercker](https://img.shields.io/badge/Apache_zookeeper-7.3.2-black.svg?style=flat&logo=apache&logoColor=white)]()

## Project Overview
![Overview Image](https://github.com/p-bear/charts.draw.io/blob/main/Season2ProjectAssetManagement.drawio.png?raw=true)


### Involved Library Stack

- JDK 17
- Spring Boot 3.2.2
- Spring Webflux
- auto-service *(AnnotationProcessor Support)*
- R2DBC
- MongoDB
- Reactor-Kafka
- zookeeper-discovery *(Service Discovery)*
- Micrometer *(Tracing)* *(+prometheus)*


## Key Idea

### BFF

#### 목적
- frontend를 위한 로직과 비즈니스 로직을 분리하기 위함

#### HOW-TO
- Multi-project 구조로 프로젝트 분리
- Core Project는 순수 Data 기준으로 비즈니스 로직을 구성
- BFF Project는 외부 API를 기준으로 비즈니스 로직을 구성

#### Benefit
- Data를 기준으로 한 core project로 data에 대한 확장성 및 가용성을 컨트롤 할 수 있음
- BFF에서 front와의 소통을 구성하여, front의 확장성을 가져감

### NoSQL

#### 목적
- 유사한 Object를 확장성있게 설계하기 위함

#### HOW-TO
- MongoDB 도입을 통해 Object를 저장
- Object의 상속구조를 통해 공통 속성 및 개별속성을 분리
- 상속으로 불러온 data를 JAVA 다형성을 통해 확장성을 확보

### CQRS *(Half)*

#### 목적


## API Spec

- Postman을 통한 관리


## CI / CD

- 참고: [Main README](https://github.com/p-bear/pbear-root/blob/master/README.md)


## Links
### for more information

- Blog: [자산관리 서비스](https://p-bear.tistory.com/category/%5B%EC%8B%9C%EC%A6%8C2%5D%20%EA%B0%9C%EC%9D%B8%EC%84%9C%EB%B2%84%20%EA%B0%9C%EB%B0%9C/%EC%9E%90%EC%82%B0%EA%B4%80%EB%A6%AC%20%EC%84%9C%EB%B9%84%EC%8A%A4)