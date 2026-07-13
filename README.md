### Kelbeth
- JWT 인증 및 noSQL Inmemory DB를 사용한 토큰 유출 시 강제 로그아웃 및 사용불가 조치 시키는 게이트웨이 실증용으로 만들어봄


#### JWT Token 관리 매커니즘 및 각 케이스별 애플리케이션 플로우
- 모든 Token은 jti (JsonTokenId) 가짐
- 모든 로그인은 최초 발생 시 Session Id 를 가지며, 이는 토큰 또한 가짐.
- 모든 Refresh Token은 Access 토큰이 발급될 때마다 갱신됨
- 모든 Refresh Token의 세션 ID는 Redis에서 관리되며, 해당 토큰의 수명만큼의 TTL이 부여됨
- 로그아웃 시 세션 ID는 레디스에서 삭제됨

- Redis 에는 Key 로 Session Id, Valu e로 jti, TTL 세 가지 정보를 저장함
- Access Token 을 재발급할 떄마다, Refresh Token 도 갱신하며, Redis 에서의 value(jti)와 TTL을 갱신함

#### Access Token 인증 시
- Token의 기본 Validation을 진행
- Valid하다면, Access Token이 가지고 있는 Session ID 와 jit 를 확인함
- Session ID를 통해 Key-Value pair를 조회함
- 존재하고, Value(jit)가 동일한지 확인함
- 동일하면 유효한 Access 토큰임
- 동일하지 않다면, Refresh Token 이 탈취되어 다른 Access Token 이 발급된 것임
  - 이 경우, 해당 세션을 파기하고 Redis 에서의 정보를 삭제함

#### RTR시 
- Session ID를 통해 Key-Value pair를 조회함
- 존재하고, Value 가 동일한지 확인함
- 새로운 jti 를 발급함
- Access Token 와 Refresh Token 를 발급함
- Redis 의 TTL 과 Value(jti)를 갱신함

#### RTR시 탈취되엇다면
- Session ID를 통해 Key-Value pair를 조회함
- 존재하고, Value가 동일한지 확인함
- 동일하지 않으면 다른 사용자가 임의로 Refresh Token을 사용한것
- Redis의 Session ID KEY 데이터를 삭제함

##### Refresh Token이 탈취되었을 때
- jti A 인 Fresh한 Refresh Token 을 사용자가 들고 있음
  - Redis에는 A 가 저장되어 있음
- 부정 사용자가 A 토큰을 탈취하여, jti B를 가진 Refresh/Access Token pair를 발급함
  - Redis에서는 Key가 Session ID인 Value의 A가 B로 갱신됨
- 기존 사용자가 A 토큰을 사용해 Access Token 발급을 요청함
  - Redis에 A 정보가 아니므로, jti가 A인 토큰이 부정 사용되었음을 알 수 있음
  - 해당 Session ID가 Key인 Redis의 Key-Value 데이터를 삭제함

#### 로그아웃 시
- Redis 에 저장되어 있는 Session ID KEY 데이터를 삭제함
  - Access/Refresh Token 확인 시 Session ID에 맞는 Key-Value pair가 없으므로 로그아웃되었음을 확인할 수 있다