# Kelbeth
- JWT 인증 및 noSQL In-memory DB를 사용한 토큰 유출 시 강제 로그아웃 및 사용불가 조치 시키는 게이트웨이 실증용으로 만들어봄

## 구현 리스트
- Gateway Filter에서 Bearer Access Token으로 인증 후, 헤더에 필요 정보를 담아 라우트
  - 라우트된 엔드포인트에서는 별도의 JWT 인증 절차 없이 헤더를 통해 사용자 정보를 받아 비즈니스 로직에 사용 가능
- In-memory DB를 사용한 만료 전 토큰의 로그아웃 처리
- Refresh/Access Token의 발급과 갱신
- Refresh Token Rotation
  - Refresh Grace Period : 5 seconds
- Refresh Token 탈취 감지 후 토큰 폐기


### JWT Token 관리 매커니즘 및 각 케이스별 애플리케이션 플로우
- 모든 JWT Token 은 jti (JsonTokenId) 가짐
- 모든 로그인은 최초 발생 시 Session Id 를 가지며, 이는 토큰 또한 가짐.
- 모든 Refresh Token은 Access 토큰이 발급될 때마다 갱신됨
- 모든 Refresh Token의 세션 ID는 Redis에서 관리되며, 해당 토큰의 수명만큼의 TTL이 부여됨
- Refresh Token과 Access Token은 Payload의 `type`으로 구분됨
- 로그아웃 시 세션 ID는 레디스에서 삭제됨
<br><br>
- Access Token 을 재발급할 떄마다, Refresh Token 도 갱신하며, Redis 에서의 value(jti)와 TTL을 갱신함
- Access Token 재발급 시, 기존 Refresh Token의 jti를 Key로 하고, 새로 재발급한 Access Token과 Refresh Token 의 정보를 Value로 하고, TTL은 5초로 하여 정보 저장

#### Redis 에 저장할 데이터 구조
- 활성 세션 정보를 관리
  - Key : Sid
  - value: jti
  - ttl : Refresh Token 의 수명

- Grace Period를 위한 정보
  - Key : jti
  - value : Json String of new Access and Refresh Token
  - ttl : 5 seconds

### Access Token 인증
- Token의 기본 Validation만을 진행
  - 기존에는 Redis Session Check를 진행하였으나, 이 경우에는 JWT의 Stateless라는 장점을 다 내다 버리기 때문에, JWT를 쓰는 이유가 없어짐
- 모든 탈취 감지 및 세션 종료는 Refresh Token과 Refresh 시 처리함


### RTR
- Redis 조회 시에 원자적 작업임을 이용
- refresh token 저장할 때 session id를 key로 이용하므로, jti를 키로 이욯

- key-session id로 검색했을 때 value-jti가 같으면 refresh가 가능한 상태
  - key-jti, value-임의 값 아무거나 넣음
  - `SET key value NX EX 10` 쿼리를 날린다 (key에 데이터가 없으면 set하고 ttl 10ms를 건다, 데이터가 있으면 nil을 return한다)
    - 삽입에 성공하면 제일 처음 요청한 스레드이므로, 새로운 토큰 페어를 발급한다
      - key-jti, value-token pair, ttl-5000 으로 5초 동안 토큰 페어 정보를 남긴다
      - key-session id 데이터를 갱신한다
    - 삽입에 실패하면 제일 처음 요청한 스레드가 아니다
      - 50~100ms정도 대기한 후, key-jtl로 데이터를 읽어 (새로운 token pair 저장되어 있음) 이를 반환한다

- key-session id로 검색했을 때 value-jti가 다르면
  - key-jti로 검색해 발급한 token pair 정보가 남아있으면, 이를 리턴한다
  - key-jti로 검색해 아무것도 없으면 key-session id를 삭제한다 (토큰 탈취)

- key-session id로 검색했을 때 존재하지 않는다면
  - 세션 만료로 refresh 불가

#### Redis 원자적 처리
- `SET key value NX EX 10` 날리고 성공하면 key-value 업데이트하고 ttl 늘리는 간단한 방식으로 함
  - 왜? 토이프로젝트라 간단한 방식으로 함
- 최적화 더 필요하면 Lua 스크립트로 여러 작업 원자적으로 처리하면 된다
  - spring 단에서 sid, jti, 새 token pair 만들어서 집어넣고
    - sid로 조회안되면 유효하지 않은 세션 (로그인 불가)
    - sid로 조회하고 jti 조회안되면 토큰 발급 후 jti에 새 토큰 정보 저장 (5초 ttl)
    - sid로 조회시 jti 일치안하고 jti로 조회되면 jti에 저장된 새로운 토큰 정보 읽어서 리턴
    - sid로 조회시 jti 일치안하고 jti로 조회안되면 sid 삭제 (세션 파기)

#### Refresh Token 탈취 케이스
- jti A 인 Fresh한 Refresh Token 을 사용자가 들고 있음
  - Redis에는 A 가 저장되어 있음
- 부정 사용자가 A 토큰을 탈취하여, jti B를 가진 Refresh/Access Token pair를 발급함
  - Redis에서는 Key가 Session ID인 Value의 A가 B로 갱신됨
- 기존 사용자가 A 토큰을 사용해 Access Token 발급을 요청함
  - Redis에 A 정보가 아니므로, jti가 A인 토큰이 부정 사용되었음을 알 수 있음
  - 해당 Session ID가 Key인 Redis의 Key-Value 데이터를 삭제함

### 로그아웃 시
- Redis 에 저장되어 있는 Session ID KEY 데이터를 삭제함
  - Refresh Token 확인 시 Session ID에 맞는 Key-Value pair가 없으므로 로그아웃되었음을 확인할 수 있다
- Access Token 은 폐기 __안함__
  - 토큰 폐기가 필요한 경우에는 만료 시간까지를 ttl로 지정해서 redis 등에 해당 access token의 jti를 블랙리스트로 저장하면 된다
  - 보안 빡세게 필요하면 그냥 2차 인증 넣어야 하는 게 맞음