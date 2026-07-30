# 중세마을 이야기 (Medieval Village)

Kotlin + Jetpack Compose 로 만든 안드로이드 모바일 RPG 초안입니다.
마을 지도와 캐릭터를 이미지 리소스 없이 **Canvas 로 직접 그려서**, 별도 에셋 없이 바로 실행됩니다.

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

## 마을에 배치된 장소 (11곳)

| 장소 | 할 수 있는 일 |
|------|---------------|
| 주인공 집 | 잠자기(무료·완전 회복), 회복 아이템 사용 |
| 상점 | 소모품·장신구 구매, 아이템 판매 |
| 무기점 | 무기·방패·갑옷·투구 구매 |
| 병원 | HP 치료, 영양제로 최대 HP 증가 |
| 교회 | 기도(MP 회복·행운), 헌금(3일간 축복) |
| INN (여관) | 숙박(HP·MP 회복, 하루 경과), 소문 듣기 |
| 대련소 | 대련으로 경험치·상금 획득, 전적 기록 |
| 던전입구 | 층별 탐험, 전투·전리품·최고 기록 |
| 대장간 | 착용 장비 강화 (+1 ~ +9, 실패 확률 있음) |
| 마법학교 | 마법 습득(최대 MP 증가), 고서 연구(지능) |
| 용병고용소 | 용병 최대 2명 고용 / 해고 |

## 조작

- **건물을 탭** → 길을 따라 그 앞까지 걸어간 뒤 자동 입장
- **빈 땅을 탭** → 마을 대로를 경유해 그 지점으로 이동
- 문 앞에 서면 하단에 `들어가기` 버튼 표시
- 뒤로 가기 → 메뉴 닫기 / 마을로 나가기

주인공은 게임 시작 시 **자기 집 안에서** 출발하며, `마을로 나가기`를 눌러 마을로 나옵니다.

## 모바일에서 바로 실행 (APK)

Android 폰에 디버그 APK를 설치하면 바로 플레이할 수 있습니다.

1. 아래 APK를 폰으로 다운로드합니다  
   - 이 Cloud Agent 실행 결과물: `MedievalVillage-debug.apk`  
   - 또는 GitHub Actions → 최신 워크플로 → Artifacts → `MedievalVillage-debug`
2. 폰에서 파일 앱으로 APK를 엽니다
3. **알 수 없는 앱 설치** / **이 소스 허용** 을 켠 뒤 설치합니다
4. 홈 화면의 **중세마을 이야기** 아이콘으로 실행합니다

> 디버그 APK라 Play 스토어 서명이 아닙니다. 설치 경고가 뜨는 것이 정상입니다.

### 명령줄로 APK 만들기

JDK 17과 Android SDK 34가 있으면:

```bash
./gradlew assembleDebug
```

결과물: `app/build/outputs/apk/debug/app-debug.apk`

### Android Studio로 실행

1. [Android Studio](https://developer.android.com/studio) (Ladybug 이상 권장) 설치
2. `File > Open` 으로 이 폴더를 엽니다
3. Gradle Sync가 끝나면 상단 `Run ▶` 로 에뮬레이터/실기기 실행

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
- 던전 층별 맵과 보스
- 효과음 / BGM
- 퀘스트와 NPC 대화
