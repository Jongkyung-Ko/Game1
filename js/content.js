window.COURSE = {
  brand: "SW × Vibe",
  modules: [
    {
      id: "m1",
      title: "SW와 컴퓨터 기초",
      pages: ["p1", "p2", "p3"],
    },
    {
      id: "m2",
      title: "SW 개발 표준 프로세스",
      pages: ["p4", "p5", "p6"],
    },
    {
      id: "m3",
      title: "좋은 Vibe 코딩을 위한 요구사항 관리",
      pages: ["p7", "p8", "p9"],
    },
  ],
  pages: {
    home: {
      id: "home",
      title: "홈",
      type: "home",
    },
    p1: {
      id: "p1",
      module: "SW와 컴퓨터 기초",
      title: "에니악, 디버깅, 컴퓨터의 시초와 언어의 시작",
      summary: "전자식 컴퓨터의 탄생과 ‘버그’의 기원, 프로그래밍 언어의 출발점을 살펴봅니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/1/16/Classic_shot_of_the_ENIAC.jpg",
          alt: "ENIAC 컴퓨터",
          caption: "ENIAC (U.S. Army Photo) — 초기 전자식 디지털 컴퓨터",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/First_Computer_Bug%2C_1947.jpg/960px-First_Computer_Bug%2C_1947.jpg",
          alt: "최초의 컴퓨터 버그(나방)",
          caption: "1947년 Mark II에서 발견된 ‘실제 버그’ 로그 — debugging의 상징적 사건",
        },
      ],
    },
    p2: {
      id: "p2",
      module: "SW와 컴퓨터 기초",
      title: "CPU·메모리·ROM과 실제 코딩의 수행",
      summary: "코드가 하드웨어에서 어떻게 실행되는지 Fetch–Decode–Execute 흐름으로 이해합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/ComputerMemoryHierarchy.svg/960px-ComputerMemoryHierarchy.svg.png",
          alt: "컴퓨터 메모리 계층",
          caption: "메모리 계층 구조 — CPU 캐시부터 저장장치까지",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Von_Neumann_model.png/960px-Von_Neumann_model.png",
          alt: "폰 노이만 구조",
          caption: "폰 노이만 모델 — CPU, 메모리, 입출력의 기본 연결",
        },
      ],
    },
    p3: {
      id: "p3",
      module: "SW와 컴퓨터 기초",
      title: "저급 언어 · 고급 언어 · 객체지향 언어",
      summary: "기계에 가까운 언어부터 사람을 위한 추상화, 그리고 객체지향까지 단계적으로 비교합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Motorola_6800_Assembly_Language.png/960px-Motorola_6800_Assembly_Language.png",
          alt: "어셈블리 언어 예시",
          caption: "Motorola 6800 어셈블리 — 저급 언어의 대표적 모습",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/BookClassUML.svg/960px-BookClassUML.svg.png",
          alt: "UML 클래스 다이어그램",
          caption: "UML 클래스 — 객체지향 설계의 시각적 표현",
        },
      ],
    },
    p4: {
      id: "p4",
      module: "SW 개발 표준 프로세스",
      title: "V 개발모델의 구성과 원리",
      summary: "왼쪽의 분해·설계와 오른쪽의 통합·검증이 어떻게 대응되는지 인터랙티브로 확인합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/V-model.svg/960px-V-model.svg.png",
          alt: "V-Model 다이어그램",
          caption: "V-Model — 개발 단계와 검증 단계의 대칭 구조",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Systems_Engineering_Process_II.svg/960px-Systems_Engineering_Process_II.svg.png",
          alt: "시스템 엔지니어링 프로세스",
          caption: "시스템 엔지니어링 프로세스 — 요구부터 검증까지의 흐름",
        },
      ],
    },
    p5: {
      id: "p5",
      module: "SW 개발 표준 프로세스",
      title: "CMMI, ISO 26262, UL 1998과 SW 관리",
      summary: "프로세스 성숙도·기능안전·소프트웨어 안전 표준의 핵심 구성과 관리 포인트를 정리합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/6/64/Characteristics_of_the_Maturity_levels.jpg",
          alt: "성숙도 레벨 특성",
          caption: "성숙도 레벨 특성 — CMMI류 프로세스 개선의 기본 사고",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/7/78/ISO_26262_ASIL_berechnen.svg/960px-ISO_26262_ASIL_berechnen.svg.png",
          alt: "ISO 26262 ASIL",
          caption: "ISO 26262 ASIL 결정 개념 — 자동차 기능안전의 핵심",
        },
      ],
    },
    p6: {
      id: "p6",
      module: "SW 개발 표준 프로세스",
      title: "SW 요구사항 관리 방안과 중요성",
      summary: "모호한 요구가 어떻게 품질·일정·비용을 흔드는지, 관리의 핵심 루프를 실습합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Requirements_engineering_cycle.png/960px-Requirements_engineering_cycle.png",
          alt: "요구공학 사이클",
          caption: "요구공학 사이클 — 도출·분석·명세·검증의 반복",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Dimensions_of_requirements_engineering-01.png/960px-Dimensions_of_requirements_engineering-01.png",
          alt: "요구공학의 차원",
          caption: "요구공학의 여러 차원 — 무엇을·왜·어떻게 관리할 것인가",
        },
      ],
    },
    p7: {
      id: "p7",
      module: "좋은 Vibe 코딩을 위한 요구사항 관리",
      title: "AI 바이브 코딩의 시작과 파급효과",
      summary: "흐름 중심의 AI 코딩이 생산성과 기술부채에 미치는 영향을 균형 있게 봅니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/0/0c/Artificial_Intelligence_System.png",
          alt: "인공지능 시스템 개념도",
          caption: "AI 시스템 개념 — 입력·모델·출력의 기본 구조",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Pair_Programming_3.jpg/960px-Pair_Programming_3.jpg",
          alt: "페어 프로그래밍",
          caption: "협업 코딩의 장면 — AI agent와의 ‘페어’로 확장되는 개발 방식",
        },
      ],
    },
    p8: {
      id: "p8",
      module: "좋은 Vibe 코딩을 위한 요구사항 관리",
      title: "콘텍스트 개념과 요구사항 작성 지침 Top 5",
      summary: "인터넷에서 반복적으로 강조되는 대표 지침 5가지를 정리하고, 프롬프트로 바로 적용해 봅니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Prompt_injection.svg/960px-Prompt_injection.svg.png",
          alt: "프롬프트/컨텍스트 개념",
          caption: "프롬프트·컨텍스트 관련 개념 도식 — 입력 구조의 중요성",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/0/09/Pros_and_cons_of_Search_Engines%2C_Knowledge_Graphs_and_Large_Language_Modells%2C_2025.png",
          alt: "LLM과 지식 소스 비교",
          caption: "검색·지식그래프·LLM 비교 — 컨텍스트 품질이 결과에 미치는 영향",
        },
      ],
    },
    p9: {
      id: "p9",
      module: "좋은 Vibe 코딩을 위한 요구사항 관리",
      title: "친해지기 — AI Agent와 상의하며 같이 개발하기",
      summary: "상담 → 계획 → 구현 → 검증 루프로 Agent와 페어 개발하는 감각을 체험합니다.",
      images: [
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/Pair_programming_1.jpg/960px-Pair_programming_1.jpg",
          alt: "함께 개발하는 모습",
          caption: "함께 보는 개발 — AI agent와의 협업도 같은 리듬이 중요합니다",
        },
        {
          src: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Agile_software_development_release_early_and_often.jpg/960px-Agile_software_development_release_early_and_often.jpg",
          alt: "빠른 반복 배포",
          caption: "작은 단위로 자주 확인 — 바이브 코딩의 안전한 운영 방식",
        },
      ],
    },
  },
};
