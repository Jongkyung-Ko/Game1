# 중세마을 이야기 (Medieval Village)

Kotlin + Jetpack Compose 로 만든 안드로이드 모바일 RPG 초안입니다.
마을은 **오크헤이븐 일러스트 맵**, 던전은 걸어 다니는 **만화풍 탑다운 층 지도**로 플레이합니다.

## 화면 구성

```
┌──────────────────────────────────────┐
│ Status │ Inventory │ Equipment │ Sys │  ← 상단 메뉴
│ Lv.1  [HP ▮▮▮▯] [MP ▮▮▯▯]  300G  1일 │  ← 상태 표시줄
├──────────────────────────────────────┤
│                                      │
│         중세 마을 (한 화면에 전부)      │
│                                      │
└──────────────────────────────────────┘
```

## 마을에 배치된 장소 (12곳)

| 장소 | 할 수 있는 일 |
|------|---------------|
| 주인공 집 | 잠자기(무료·완전 회복), 회복 아이템 사용 |
| 상점 | 소모품·장신구 구매, 아이템 판매 |
| 무기점 | 무기·방패·갑옷·투구 구매 |
| 병원 | HP 치료, 영양제로 최대 HP 증가 |
| 교회 | 기도(MP 회복·행운), 헌금(3일간 축복) |
| INN (여관) | 숙박(HP·MP 회복, 하루 경과), 소문 듣기 |
| PUB (선술집) | 선술집 안을 걸어 다니며 주인장·여행객·길드원·취객과 말풍선 대화 |
| 대련소 | 대련으로 경험치·상금 획득, 전적 기록 |
| 던전입구 | 층별 탐험, 전투·전리품·최고 기록 |
| 대장간 | 착용 장비 강화 (+1 ~ +9, 실패 확률 있음) |
| 마법학교 | 마법 습득(최대 MP 증가), 고서 연구(지능) |
| 용병고용소 | 용병 최대 2명 고용 / 해고 |

## 조작

- **건물을 탭** → 길을 따라 그 앞까지 걸어간 뒤 자동 입장
- **빈 땅을 탭** → 마을 대로를 경유해 그 지점으로 이동
- **PUB의 NPC를 탭** → NPC 앞으로 걸어간 뒤 게임 정보와 소문을 대화
- 문 앞에 서면 하단에 `들어가기` 버튼 표시
- 뒤로 가기 → 메뉴 닫기 / 마을로 나가기

주인공은 게임 시작 시 **자기 집 안에서** 출발하며, `마을로 나가기`를 눌러 마을로 나옵니다.

## 음악과 동료

- 정착지마다 배경음악이 다르고, 저주받은 마을·성과 해방된 곳은 다른 곡이 재생됩니다. 던전·대련소는 긴장 테마입니다.
- 걸을 때는 배경음 위에 발소리가 함께 재생됩니다.
- 음원은 앱에서 PCM으로 합성하므로 별도 오디오 파일이나 네트워크 연결이 필요 없습니다.
- 용병은 여러 명 고용할 수 있지만 Status에서 최대 2명만 원정대로 선택합니다.
- 선택한 용병만 마을과 각 장소 상단 그림에 등장하고 던전 전투력에 반영됩니다.

## APK 다운로드 (최신: v0.4.44)

바로 설치할 파일: [MedievalVillage-v0.4.44-debug.apk](https://github.com/Jongkyung-Ko/Game1/releases/download/v0.4.44/MedievalVillage-v0.4.44-debug.apk)

이전 버전은 [Releases](https://github.com/Jongkyung-Ko/Game1/releases) 에서 받을 수 있습니다.

1. 위 APK 를 폰으로 다운로드
2. 폰 **설정 > 보안** 에서 알 수 없는 앱 설치 허용
3. APK 를 열어 설치 → 앱 이름 **중세마을 v0.4.44**
4. 예전 빌드와 패키지명이 다르므로 기존 앱을 지울 필요는 없습니다

`v*` 태그를 푸시하면 GitHub Actions 가 자동으로 APK 를 빌드해 Release Assets 에 올립니다.

## 실행 방법

1. [Android Studio](https://developer.android.com/studio) (Ladybug 이상 권장) 설치
2. `File > Open` 으로 이 폴더를 엽니다
3. Gradle Sync가 끝나면 (SDK·Gradle 자동 다운로드) 상단 `Run ▶` 실행

명령줄에서 빌드하려면 JDK 17과 Android SDK 34가 필요합니다. Gradle Wrapper(`./gradlew`)가 포함되어 있습니다.

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 에뮬레이터에서 APK 실행

Android SDK 34와 `medieval` AVD가 있으면 명령줄만으로 디버그 APK를 띄울 수 있습니다.

```bash
scripts/emulator-install.sh          # SDK 패키지 + AVD (한 번만)
scripts/emulator-run-apk.sh          # 부팅 → 설치 → 실행
# 화면 미러 (VNC DISPLAY=:1). scrcpy가 안 되면 클릭 전달 스크린샷 미러로 넘어갑니다.
scripts/emulator-show.sh
```

일부 Cloud Agent 호스트는 `/dev/kvm`이 있어도 중첩 KVM에서 게스트 vCPU 생성이 실패합니다. 그 경우 스크립트가 소프트웨어 가속(`-accel off`)으로 넘어갑니다. TCG 부팅은 수 분이 걸릴 수 있습니다. 로컬 PC에서 KVM이 정상이면 `EMULATOR_ACCEL=on scripts/emulator-start.sh` 로 강제할 수 있습니다.

## 프로젝트 구조

```
app/src/main/java/com/medieval/village/
├── MainActivity.kt              앱 진입점
├── model/
│   ├── Place.kt                 마을 좌표계 · 11개 장소 배치 · 소품 위치
│   ├── Item.kt                  아이템 · 장비 · 상점 목록
│   └── Player.kt                플레이어 스탯 · 마법 · 용병 데이터
├── game/
│   └── GameViewModel.kt         상태, 이동 경로 탐색, 모든 게임 행동
└── ui/
    ├── GameRoot.kt              프레임 루프 · 화면 전환
    ├── TopBar.kt                상단 4개 메뉴 + HP/MP/골드/일차
    ├── Widgets.kt               공통 UI 조각
    ├── theme/Theme.kt           중세풍 색 팔레트
    ├── village/
    │   ├── VillageScene.kt      마을 렌더링 · 탭 입력 · 이름표
    │   ├── Buildings.kt         건물 11종 외형
    │   └── Hero.kt              주인공 스프라이트 (4방향 · 걷기)
    ├── menu/MenuOverlay.kt      Status/Inventory/Equipment/System
    └── place/
        ├── PlaceScreen.kt       장소별 행동 UI
        ├── PubScreen.kt         PUB 실내 탐험
        ├── DungeonScreen.kt     만화풍 탑다운 던전 맵
        └── Interior.kt          장소별 실내 배경
```

## 마을 좌표계

`Village.W x Village.H = 1000 x 1650` 고정 월드를 정의하고, 화면 크기에 맞춰 균등 스케일합니다.
따라서 어떤 해상도의 폰에서도 **마을 전체가 한 화면에** 들어옵니다.

주인공은 중앙 대로(x=500)와 가로길로 이루어진 길망을 따라 ㄱ자 경로로 이동하므로
건물을 뚫고 지나가지 않습니다.

## 앞으로 추가하면 좋을 것

- 저장/불러오기 (DataStore)
- 턴제 전투 화면 (지금은 결과 요약 방식)
- 던전 층별 보스전 (맵 UI는 만화풍 탑다운으로 구현됨)
- 효과음 / BGM
- 퀘스트와 NPC 대화
