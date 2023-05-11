# 4Week_김연준.md

## Title: [4Week] 김연준

### 필수 미션 요구사항 분석 & 체크리스트
- [x] 네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용
- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현
### 선택 미션 요구사항 분석 & 체크리스트
- [x] 젠킨스를 통해서 리포지터리의 main 브랜치에 커밋 이벤트가 발생하면 자동으로 배포가 진행되도록
- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현
- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능
---

-  젠킨스를 통해서 리포지터리의 main 브랜치에 커밋 이벤트가 발생하면 자동으로 배포가 진행되도록 (미구현)
-  내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현 (구현)
-  내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능 (구현)
-  네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용 (구현)
-  내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현 (구현)
### 4주차 미션 요약

---

**[접근 방법]**



- 성별, 호감사유 필터링 기능
  - 컨트롤러에서 gender, attractiveTypeCode라는 이름의 파라미터를 받는다.
  - 컨트롤러에서 받은 파라미터를 querydsl레파지토리까지 전달
  - 처음 toList페이지를 들어갈 때 조건 파라미터들이 null값으로 전달되는 것을 고려하여 service단에서 처리하지 않고 querydsl로 처리함
  - BooleanExpression은 null일 경우 null을 반환하고 where절에서 자동으로 사라지는 특성이 있으므로 유연하게 동적쿼리에 대응
  - null이 아니라면 BooleanExpression는 원하는 필터링 조건을 반환하고 where절에 주입
- 정렬 기능
  - orderby절에는 표현식으로 OrderSpecifier클래스를 받으므로 이를 반환하는 sortByField 메소드를 구성
  - 클라이언트로부터 sortCode라는 1~6의 정렬 옵션 중 하나를 파라미터로 받아 switch문으로 각각의 옵션 처리 
  - 5, 6번 옵션은 다중 옵션이므로 sortByField메소드에서 OrderSpecifier[]와 같이 옵션을 배열에 담아 반환 
  - 정렬옵션을 선택하지 않을 때 기본으로 실행되는 옵션은 최신날짜순이므로 옵션파라미터가 null이거나 공백이면 기본옵션을 실행하게 처리
  - OrderSpecifier의 Expression 매개변수로 실제 엔티티에 존재하는 필드(데이터베이스에 등록된 필드)만 인식한다.
  - 인기 순으로 정렬하려면 호감을 표시한 인스타 멤버의 총 like 갯수로 판단해야하므로 InstaMemberBase에 likes필드를 추가
- 도메인, HTTPS적용
  - iwantmyname에서 도메인을 구입하고 dnszi에서 관리를 하여 도메인이 네이버 클라우드서버에서 받은 공인ip를 가르키도록 A레코드 설정
  - 접속용 포트인 443, 80 관리자 포트인 81번을 npm을 도입하여 전부 몰아준다.
  - npm에서 SSL인증서 발급받고 https적용

**[특이사항]**

아쉬웠던 점
- 인기 순 정렬에 likes를 기준으로 판단 할 다른 방법이 생각나지 않아 OrderSpecifier규격에 맞추려고 likes필드를 추가시켜 테이블 구조를 바꾼 것
- 배포 과정에 대해 전반적인 이해가 부족한 상태여서 다시 공부할 필요성을 느낌
- 젠킨스 자동 배포를 하지 못한 점

궁금했던 점
- 서비스단과 DB단의 정렬 처리 중 상황에 따라서 어느 방법이 좋은지 구체적으로 알고싶습니다.


**[Refactoring]**