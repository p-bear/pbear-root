# Project Subway Analyze

[![Wercker](https://img.shields.io/badge/spring--boot-3.2.2-%236DB33F.svg?style=flat-square&logo=springboot&color=%236DB33F)](https://spring.io/projects/spring-boot)
[![Wercker](https://img.shields.io/badge/Apache_Kafka-7.3.2-black.svg?style=flat&logo=apachekafka&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/ksqlDB-7.3.2-black.svg?style=flat&logo=apachekafka&logoColor=white)]()
[![Wercker](https://img.shields.io/badge/docker-24.0.7-%231D63ED.svg?style=flat&logo=docker&logoColor=%23E5F2FC)]()
[![Wercker](https://img.shields.io/badge/Apache_zookeeper-7.3.2-black.svg?style=flat&logo=apache&logoColor=white)]()

## Project Overview


### Involved Library Stack

- JDK 17
- Spring Boot 3.2.2
- Spring Webflux
- auto-service *(AnnotationProcessor Support)*
- Reactor-Kafka
- Kafka Streams
- ksqlDB
- Websocket
- zookeeper-discovery *(Service Discovery)*
- Micrometer *(Tracing)* *(+prometheus)*


## Service Desc

### 지하철 데이터 시각화

- 서울시 공공데이터 활용하여 Data Collect
- Kafka Streams를 활용한 Data Processing
- ksqlDB를 활용한 Stream 처리
- Websocket을 통한 DownStream 구성

### Architecture
![Architexture Image](https://github.com/p-bear/charts.draw.io/blob/main/subway/architecture.drawio.png?raw=true)

- Gateway Server: API Gateway
- Session Connector Server: Websocket Session Holder
- BFF Server: REST API for Client
- Business Server: Core Business


### Topology

#### Business Server Topology (Source)
![Business Server Topology](https://github.com/p-bear/charts.draw.io/blob/main/subway/subwayTopology.drawio.png?raw=true)

#### ksqlDB Topology (Processing)
![ksqlDB Topology](https://github.com/p-bear/charts.draw.io/blob/main/subway/subwayKsqlDBTopology.drawio.png?raw=true)


## Technical Key Idea

### Kafka를 활용한 Data Processing

#### 목적
- 용이한 Scale-out, 고가용성 확보
- Full Async / Non-Blocking 아키텍처 구성

#### Data Source (Collect Data)
- Seoul OpenAPI에서 지하철 관련 정보 확보
- Kafka Topic으로 데이터 Publish

#### Data Processing with ksqlDB
- 고수준 Interface를 통한 Stream 처리
- 확장성 있는 구조 확보
- 구현 및 유지보수 생산성 극대화


## API Spec

- Postman을 통한 관리

## CI / CD

- 참고: [Main README](https://github.com/p-bear/pbear-root/blob/master/README.md)


## Links
### for more information

- Blog: 