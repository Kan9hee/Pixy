# Pixy

LSTM 기반 영어 회화 피드백 서비스

## 개발 환경
- Springboot, Spring JPA, Spring Security, Spring Websocket
- Flask
- Kotlin, Python
- Mysql, MongoDB
- Docker
- Jenkins
- Prometheus, Grafana
- Nginx
- AWS EC2, RDS, Route 53
- GCP STT, GCP Cloud Storage

## 시스템 아키텍쳐
<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_architecture.png" width="800"/>

## 주요 기능

### 음성 녹음 및 분석
|Screen #1|Screen #2|
|:---:|:---:|
|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_websocketSequence.png" width="600"/>|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_loading.png" width="600"/>|
- 사용자와 서버간 UUID 기반 웹소켓 통신
- 녹음된 음성 데이터를 분석하여 텍스트로 변환
- 음성 데이터의 정보를 추출하고 JSON 형식으로 파싱
    - 인코딩 형식, 샘플링 레이트
- 자동 구두점 및 타임스탬프Google STT API를 사용하여 음성을 텍스트로 변환

### 텍스트화된 답변 피드백
|Screen #3|
|:---:|
|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_result.png" width="600"/>|
- 문장의 흐름이 끊기는 음성 공백이 발생한 문장 위치에 [needfiller] 태그 삽입
- 문장 텍스트를 분석하여 [needfiller] 태그 위치에 필러 삽입
- 초기 인식 텍스트와 피드백이 완료된 텍스트를 사용자에게 전송

### 서버 모니터링
|Screen #4|
|:---:|
|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_adminPage.png" width="800"/>|

|Screen #5|
|:---:|
|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_apiServerMonitering.png" width="800"/>|
- Prometheus 및 Grafana를 활용하여 실시간 모니터링 환경 구성
- 주요 외부 서버의 CPU, 메모리 성능 시각화
- Node Exporter로 모니터링 서버에 Jenkins CI/CD 서버의 상태 정보 전송
- CAdvisor로 모니터링 서버에 API 서버의 상태 정보 전송

### 무중단 배포 환경
|Screen #6|
|:---:|
|<img src="https://github.com/Kan9hee/Pixy/blob/main/Pixy_deployment.png"/>|
- Jenkins기반 지속적 통합 및 지속적 배포 환경 구축
- Github에 코드 변경 사항을 push할 시, Jenkins가 자동으로 애플리케이션 빌드(CI)
- Docker 이미지를 생성하여 Docker Hub에 업로드(CD)
- Nginx와 Docker Compose를 사용하여 배포 환경 관리, YAML 파일을 이용해 컨테이너 설정 자동화
- Blue-Green 배포 방식을 적용하여 무중단 배포 구현
- 기존(Blue) 환경이 실행 중일 때, 새로운(Green) 환경에 최신 버전 배포
- Green 환경 정상 작동시, 로드 밸런서를 통해 트래픽을 전환한 후 Blue 환경 중지

## 참고자료
- https://cloud.google.com/speech-to-text/docs/async-recognize
- https://seongwon.dev/DevOps/20220717-CICD%EA%B5%AC%EC%B6%95%EA%B8%B02
