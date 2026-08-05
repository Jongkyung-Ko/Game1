(() => {
  const STORAGE_KEY = "sv-course-progress-v1";
  const pageOrder = ["home", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"];
  const app = document.getElementById("app");
  const sideNav = document.getElementById("sideNav");
  const progressFill = document.getElementById("progressFill");
  const progressLabel = document.getElementById("progressLabel");
  const progressPct = document.getElementById("progressPct");
  const navToggle = document.getElementById("navToggle");
  const sidebar = document.getElementById("sidebar");
  const backdrop = document.getElementById("sidebarBackdrop");

  const state = {
    visited: new Set(loadProgress()),
    current: "home",
  };

  function loadProgress() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    } catch {
      return [];
    }
  }

  function saveProgress() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify([...state.visited]));
  }

  function imgFigure(img) {
    return `
      <figure class="media-frame">
        <img src="${img.src}" alt="${img.alt}" loading="lazy" referrerpolicy="no-referrer" />
        <figcaption>${img.caption}</figcaption>
      </figure>
    `;
  }

  function pageShell(page, body) {
    const idx = pageOrder.indexOf(page.id);
    const prev = pageOrder[idx - 1];
    const next = pageOrder[idx + 1];
    return `
      <section class="page" data-page="${page.id}">
        <div class="page-head">
          <div class="meta">${page.module || "강좌 홈"} · ${page.id === "home" ? "Overview" : page.id.toUpperCase()}</div>
          <h1>${page.title}</h1>
          ${page.summary ? `<p class="lead muted">${page.summary}</p>` : ""}
        </div>
        ${body}
        <div class="pager">
          ${prev ? `<button class="btn btn-outline" data-go="${prev}">← 이전</button>` : `<span></span>`}
          ${next ? `<button class="btn btn-teal" data-go="${next}">다음 →</button>` : `<button class="btn btn-teal" data-go="home">홈으로</button>`}
        </div>
      </section>
    `;
  }

  function renderHome() {
    const cards = COURSE.modules
      .map((m, i) => {
        const first = m.pages[0];
        return `
          <a class="module-card" href="#${first}" data-nav="${first}">
            <div>
              <div class="num">0${i + 1}</div>
              <h3>${m.title}</h3>
              <p class="muted">${m.pages.length}페이지 · 인터랙티브 실습 포함</p>
            </div>
            <span class="chip">시작하기</span>
          </a>
        `;
      })
      .join("");

    return `
      <section class="page" data-page="home">
        <div class="hero-panel home">
          <span class="kicker">Interactive Software Course</span>
          <h1>SW × Vibe Coding</h1>
          <p class="lead">컴퓨터의 시초부터 국제 표준, 그리고 AI와 함께 요구사항을 다루는 바이브 코딩까지 — 9페이지로 체험하는 실무형 입문 강좌입니다.</p>
          <div class="cta-row">
            <button class="btn btn-primary" data-go="p1">1페이지부터 시작</button>
            <button class="btn btn-ghost" data-go="p7">바이브 코딩부터</button>
          </div>
        </div>
        <div class="module-cards">${cards}</div>
      </section>
    `;
  }

  function renderP1(page) {
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>타임라인으로 보는 시작</h2>
            <div class="timeline">
              <div class="t-item"><div class="t-dot"></div><div class="t-body"><strong>1945–46 · ENIAC</strong>플러그보드와 스위치로 ‘프로그램’하던 초기 전자식 컴퓨터. 소프트웨어라는 말이 정착되기 전의 하드웨어 중심 세계입니다.</div></div>
              <div class="t-item"><div class="t-dot"></div><div class="t-body"><strong>1947 · 실제 버그</strong>Harvard Mark II 릴레이에 끼인 나방을 로그에 붙인 사건. debugging이라는 문화적 상징이 됐습니다.</div></div>
              <div class="t-item"><div class="t-dot"></div><div class="t-body"><strong>1950s · 언어의 탄생</strong>기계어·어셈블리를 넘어 FORTRAN, COBOL 등 고급 언어가 등장하며 ‘사람 친화적 코딩’이 시작됩니다.</div></div>
            </div>
          </div>
          <div class="panel">
            <h2>퀵 퀴즈</h2>
            <p>‘First actual case of bug being found’ 로그는 어느 맥락의 사건일까요?</p>
            <div class="quiz" data-quiz="p1">
              <button data-answer="wrong">ENIAC 진공관이 타서 생긴 오류 기록</button>
              <button data-answer="correct">Mark II 릴레이에 끼인 나방을 로그에 붙인 기록</button>
              <button data-answer="wrong">최초의 C 컴파일러 버그 리포트</button>
            </div>
            <div class="feedback" data-feedback="p1"></div>
          </div>
        </div>
        <div class="stack">
          ${page.images.map(imgFigure).join("")}
        </div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP2(page) {
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>클릭하며 배우는 실행 구조</h2>
            <p class="muted">노드를 누르면 역할이 열리고, 파이프라인이 한 바퀴 돕니다.</p>
            <div class="arch" id="archNodes">
              <button class="arch-node" data-arch="cpu"><strong>CPU</strong><span>연산·제어</span></button>
              <button class="arch-node" data-arch="ram"><strong>RAM</strong><span>실행 중 데이터</span></button>
              <button class="arch-node" data-arch="rom"><strong>ROM/저장소</strong><span>펌웨어·영구 저장</span></button>
            </div>
            <div class="pipeline" id="pipeline">
              <span data-step="0">Fetch</span>
              <span data-step="1">Decode</span>
              <span data-step="2">Execute</span>
              <span data-step="3">Store</span>
            </div>
            <p id="archExplain" class="muted" style="margin-top:1rem">노드를 선택해 보세요.</p>
            <button class="btn btn-teal" id="runPipeline" style="margin-top:0.75rem">코드 실행 시뮬레이션</button>
          </div>
          <div class="panel">
            <h3>코딩이 수행되는 실제 경로</h3>
            <ol>
              <li>소스코드 작성 (사람이 읽는 텍스트)</li>
              <li>컴파일/인터프리트 → 기계어·바이트코드</li>
              <li>로더가 프로그램을 메모리(RAM)에 올림</li>
              <li>CPU가 명령어를 가져와 해석·실행</li>
              <li>필요 시 ROM/디스크의 데이터·펌웨어와 연동</li>
            </ol>
          </div>
        </div>
        <div class="stack">${page.images.map(imgFigure).join("")}</div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP3(page) {
    const body = `
      <div class="grid-2">
        <div class="panel">
          <h2>언어 레벨 비교기</h2>
          <div class="level-tabs" id="langTabs">
            <button class="active" data-lang="low">저급 언어</button>
            <button data-lang="high">고급 언어</button>
            <button data-lang="oop">객체지향</button>
          </div>
          <div class="code-box" id="langCode">; 어셈블리 느낌의 저급 예시
LDA #$01
STA $1000
JMP START</div>
          <p id="langNote" class="muted" style="margin-top:0.85rem">하드웨어에 가깝고 제어는 세밀하지만, 생산성과 이식성은 낮습니다.</p>
        </div>
        <div class="stack">
          ${page.images.map(imgFigure).join("")}
          <div class="panel">
            <h3>한 줄 정리</h3>
            <p><strong>저급</strong>은 기계를, <strong>고급</strong>은 문제를, <strong>객체지향</strong>은 세상의 구조(객체·관계)를 중심에 둡니다.</p>
          </div>
        </div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP4(page) {
    const body = `
      <div class="grid-2">
        <div class="panel">
          <h2>인터랙티브 V-Model</h2>
          <p class="muted">왼쪽 단계를 누르면 오른쪽 검증 단계가 함께 강조됩니다.</p>
          <div class="vmodel" id="vmodel">
            <div class="v-col">
              <button class="v-node left" data-v="req">요구사항 분석<small>Needs / Spec</small></button>
              <button class="v-node left" data-v="arch">시스템/SW 설계<small>Architecture</small></button>
              <button class="v-node left" data-v="mod">모듈 설계<small>Detailed Design</small></button>
            </div>
            <div class="v-mid" aria-hidden="true"></div>
            <div class="v-col">
              <button class="v-node right" data-v="req">인수/시스템 테스트<small>Acceptance</small></button>
              <button class="v-node right" data-v="arch">통합 테스트<small>Integration</small></button>
              <button class="v-node right" data-v="mod">단위 테스트<small>Unit Test</small></button>
            </div>
          </div>
          <div class="v-node center" style="margin-top:0.75rem">구현 (Coding)</div>
          <p id="vExplain" class="muted" style="margin-top:1rem">원리: 왼쪽에서 정의한 것을 오른쪽에서 증명합니다. “설계 없는 테스트”나 “테스트 없는 설계”를 줄이는 모델입니다.</p>
        </div>
        <div class="stack">${page.images.map(imgFigure).join("")}</div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP5(page) {
    const body = `
      <div class="panel">
        <h2>국제 표준 탐색기</h2>
        <div class="tabs" id="stdTabs">
          <button class="active" data-tab="cmmi">CMMI</button>
          <button data-tab="iso">ISO 26262</button>
          <button data-tab="ul">UL 1998</button>
        </div>
        <div class="tab-panel active" data-panel="cmmi">
          <h3>CMMI — 프로세스 성숙도</h3>
          <p>조직이 소프트웨어를 <strong>얼마나 일관되고 개선 가능하게</strong> 만드는지 평가·개선하는 프레임워크입니다.</p>
          <ul>
            <li>Level 1 Initial → 5 Optimizing 형태의 성숙도 사고</li>
            <li>요구관리, 프로젝트 계획, 형상관리, 측정·분석 등 프로세스 영역</li>
            <li>SW 관리 방안: 표준 프로세스 정의 → 프로젝트 적용 → 측정으로 개선</li>
          </ul>
        </div>
        <div class="tab-panel" data-panel="iso">
          <h3>ISO 26262 — 자동차 기능안전</h3>
          <p>전기·전자 시스템의 오동작으로 인한 위험을 다루며, <strong>ASIL</strong>로 안전 목표와 활동을 차등 적용합니다.</p>
          <ul>
            <li>개념·시스템·HW/SW·생산·운영 전 생애주기</li>
            <li>SW: 요구 분해, 설계, 구현, 단위/통합/검증의 추적성</li>
            <li>관리 방안: 위험분석 ↔ 안전요구 ↔ 테스트 증거 연결</li>
          </ul>
        </div>
        <div class="tab-panel" data-panel="ul">
          <h3>UL 1998 — 소프트웨어 안전 표준</h3>
          <p>안전 관련 제품에 들어가는 소프트웨어의 <strong>결함 회피·통제</strong>를 다루는 UL 규격 계열입니다.</p>
          <ul>
            <li>요구사항·설계·구현·검증의 문서화와 리뷰</li>
            <li>위험 분석, 코딩 표준, 테스트, 형상/변경 관리</li>
            <li>관리 방안: “무엇을 안전하게 막아야 하는가”를 요구로 고정하고 증거를 남김</li>
          </ul>
        </div>
      </div>
      <div class="grid-2" style="margin-top:1.25rem">
        ${page.images.map(imgFigure).join("")}
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP6(page) {
    const checks = [
      "목표가 한 문장으로 명확한가?",
      "사용자/이해관계자가 식별됐는가?",
      "수락 기준(Acceptance Criteria)이 있는가?",
      "비기능(성능·보안·호환)이 빠지지 않았는가?",
      "범위 밖(Non-goals)이 명시됐는가?",
      "변경 시 추적(버전/이슈 링크)이 가능한가?",
    ];
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>요구사항 품질 체크리스트</h2>
            <p class="muted">항목을 체크하면 품질 점수가 올라갑니다. 좋은 요구는 “검증 가능”해야 합니다.</p>
            <div class="check-list" id="reqChecks">
              ${checks
                .map(
                  (c, i) => `
                <label>
                  <input type="checkbox" data-check="${i}" />
                  <span>${c}</span>
                </label>`
                )
                .join("")}
            </div>
          </div>
          <div class="panel" style="text-align:center">
            <div class="score-ring" id="reqScore" style="--p:0%">0%</div>
            <p class="muted" id="reqScoreText">아직 시작 전 — 모호한 요구는 가장 비싼 버그입니다.</p>
          </div>
        </div>
        <div class="stack">
          ${page.images.map(imgFigure).join("")}
          <div class="panel">
            <h3>왜 중요한가?</h3>
            <p>요구가 흔들리면 설계·코드·테스트가 동시에 흔들립니다. 표준 프로세스(V모델, CMMI, 기능안전)의 공통 전제는 <strong>추적 가능한 요구</strong>입니다.</p>
          </div>
        </div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP7(page) {
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>바이브 코딩이란?</h2>
            <p>의도·감각(“이런 느낌의 앱”)을 AI에게 맡기고, 대화하며 빠르게 코드를 만들어가는 방식입니다. 진입장벽은 낮아지고, <strong>요구의 모호함</strong>은 더 비싸집니다.</p>
            <div class="impact-bars" id="impactBars">
              <div class="bar-row"><label><span>아이디어 → 프로토타입 속도</span><span>+큰 폭</span></label><div class="bar"><i data-w="92"></i></div></div>
              <div class="bar-row"><label><span>학습/탐험 속도</span><span>+높음</span></label><div class="bar"><i data-w="84"></i></div></div>
              <div class="bar-row"><label><span>기술부채·회귀 위험</span><span>관리 없으면 ↑</span></label><div class="bar"><i data-w="70"></i></div></div>
              <div class="bar-row"><label><span>요구사항 품질의 영향력</span><span>결정적</span></label><div class="bar"><i data-w="96"></i></div></div>
            </div>
            <button class="btn btn-teal" id="animateImpact" style="margin-top:1rem">파급효과 시각화</button>
          </div>
          <div class="panel">
            <h3>파급효과 한 줄</h3>
            <p>AI는 “빠른 손”이 되어주지만, <strong>무엇을 만들지</strong>는 여전히 사람의 요구관리 영역입니다.</p>
          </div>
        </div>
        <div class="stack">${page.images.map(imgFigure).join("")}</div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP8(page) {
    const guides = [
      {
        t: "컨텍스트를 계약처럼 고정하라",
        d: "기술스택, 폴더 구조, 금지사항, 기존 패턴을 먼저 제시합니다. (roadmap.sh / Designveloper 등에서 공통으로 강조)",
      },
      {
        t: "코드 전에 계획을 받아라",
        d: "구현 전 변경 파일·가정·접근을 먼저 쓰게 하고 검토합니다. 계획 없는 바이브는 범위 팽창으로 이어지기 쉽습니다.",
      },
      {
        t: "작은 수직 슬라이스로 나누라",
        d: "한 번에 ‘앱 전체’가 아니라, 동작하는 작은 단위(UI+로직+테스트)로 요구를 쪼갭니다.",
      },
      {
        t: "제약(하지 말 것)과 완료 정의를 명시하라",
        d: "Negative constraints + Definition of Done. “멋지게” 대신 “입력 검증 실패 시 400 + 테스트 통과”처럼 계약형으로 씁니다.",
      },
      {
        t: "검증 루프를 요구에 포함하라",
        d: "테스트·린트·수동 확인 기준을 요구사항의 일부로 둡니다. ‘생성’이 아니라 ‘수락’이 끝입니다.",
      },
    ];
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>대표 지침 Top 5</h2>
            <p class="muted">Levelop, roadmap.sh, Designveloper, Drainpipe, arXiv 바이브코딩 가이드 등에서 반복되는 핵심만 골랐습니다.</p>
            <div class="guide-list">
              ${guides
                .map(
                  (g) => `
                <details class="guide-item">
                  <summary>${g.t}</summary>
                  <div class="guide-body">${g.d}</div>
                </details>`
                )
                .join("")}
            </div>
          </div>
          <div class="panel prompt-builder">
            <h3>컨텍스트 프롬프트 빌더</h3>
            <label>목표</label>
            <input id="pbGoal" value="할 일 목록에 마감일 필터 추가" />
            <label>스택/컨텍스트</label>
            <input id="pbStack" value="HTML/JS, 기존 localStorage 사용, CSS 변수 유지" />
            <label>제약</label>
            <input id="pbConstraint" value="새 프레임워크 금지, index.html 구조 유지" />
            <label>완료 정의</label>
            <input id="pbDone" value="필터 버튼 3개, 새로고침 후에도 유지, 수동 확인 시나리오 포함" />
            <button class="btn btn-teal" id="pbBuild" style="margin-top:0.9rem">프롬프트 생성</button>
            <div class="prompt-preview" id="pbOut">버튼을 누르면 복붙용 프롬프트가 만들어집니다.</div>
          </div>
        </div>
        <div class="stack">${page.images.map(imgFigure).join("")}</div>
      </div>
    `;
    return pageShell(page, body);
  }

  function renderP9(page) {
    const body = `
      <div class="grid-2">
        <div class="stack">
          <div class="panel">
            <h2>AI Agent 페어 세션 시뮬레이터</h2>
            <p class="muted">아래 프리셋으로 “상담 → 상의 → 구현 계획” 흐름을 체험하세요. (로컬 규칙 기반 데모)</p>
            <div class="presets" id="chatPresets">
              <button data-preset="consult">요구사항 상담</button>
              <button data-preset="plan">구현 계획 요청</button>
              <button data-preset="impl">작은 단위 구현</button>
              <button data-preset="review">검증 체크 요청</button>
            </div>
            <div class="chat">
              <div class="chat-log" id="chatLog"></div>
              <div class="chat-input">
                <textarea id="chatInput" placeholder="Agent에게 상담/상의할 내용을 적어보세요"></textarea>
                <button class="btn btn-teal" id="chatSend">보내기</button>
              </div>
            </div>
          </div>
        </div>
        <div class="stack">
          ${page.images.map(imgFigure).join("")}
          <div class="panel">
            <h3>같이 개발하는 루프</h3>
            <ol>
              <li>목표와 비목표를 먼저 말한다</li>
              <li>Agent에게 계획을 받는다</li>
              <li>한 슬라이스만 구현한다</li>
              <li>실행·테스트로 수락한다</li>
              <li>배운 컨텍스트를 다음 요청에 남긴다</li>
            </ol>
          </div>
        </div>
      </div>
    `;
    return pageShell(page, body);
  }

  const renderers = {
    home: renderHome,
    p1: renderP1,
    p2: renderP2,
    p3: renderP3,
    p4: renderP4,
    p5: renderP5,
    p6: renderP6,
    p7: renderP7,
    p8: renderP8,
    p9: renderP9,
  };

  function buildSidebar() {
    let html = `<a href="#home" data-nav="home"><strong>홈</strong><small>강좌 개요</small></a>`;
    COURSE.modules.forEach((m) => {
      html += `<div class="sidebar-section">${m.title}</div>`;
      m.pages.forEach((pid) => {
        const p = COURSE.pages[pid];
        html += `<a href="#${pid}" data-nav="${pid}"><strong>${pid.toUpperCase()} · ${p.title}</strong><small>${m.title}</small></a>`;
      });
    });
    sideNav.innerHTML = html;
  }

  function updateProgressUI() {
    const learnable = pageOrder.filter((p) => p !== "home");
    const done = learnable.filter((p) => state.visited.has(p)).length;
    const pct = Math.round((done / learnable.length) * 100);
    progressFill.style.width = `${pct}%`;
    progressPct.textContent = `${pct}%`;
    const cur = COURSE.pages[state.current];
    progressLabel.textContent = state.current === "home" ? "시작 전" : cur?.title || state.current;
    sideNav.querySelectorAll("a").forEach((a) => {
      const id = a.dataset.nav;
      a.classList.toggle("active", id === state.current);
      a.classList.toggle("done", id !== "home" && state.visited.has(id));
    });
  }

  function bindQuiz(root) {
    root.querySelectorAll("[data-quiz]").forEach((quiz) => {
      const key = quiz.dataset.quiz;
      const feedback = root.querySelector(`[data-feedback="${key}"]`);
      quiz.querySelectorAll("button").forEach((btn) => {
        btn.addEventListener("click", () => {
          quiz.querySelectorAll("button").forEach((b) => b.classList.remove("correct", "wrong"));
          const ok = btn.dataset.answer === "correct";
          btn.classList.add(ok ? "correct" : "wrong");
          if (ok) {
            feedback.textContent = "정확합니다! 디버깅의 문화적 상징을 잘 짚으셨어요.";
            feedback.className = "feedback ok";
          } else {
            feedback.textContent = "다시 생각해볼까요? 나방이 붙은 그 로그입니다.";
            feedback.className = "feedback bad";
          }
        });
      });
    });
  }

  function bindP2(root) {
    const explain = {
      cpu: "CPU는 명령어를 해석하고 연산합니다. 프로그램 카운터로 다음 명령을 가리키며 ALU·제어장치가 핵심입니다.",
      ram: "RAM은 실행 중인 코드와 데이터가 머무르는 작업 공간입니다. 전원이 꺼지면 사라지는 휘발성 메모리입니다.",
      rom: "ROM/플래시 등 저장소는 펌웨어·부트로더·영구 데이터를 보관하고, 필요 시 RAM으로 불러와 CPU가 실행합니다.",
    };
    const nodes = root.querySelectorAll("[data-arch]");
    const note = root.querySelector("#archExplain");
    nodes.forEach((n) =>
      n.addEventListener("click", () => {
        nodes.forEach((x) => x.classList.remove("active"));
        n.classList.add("active");
        note.textContent = explain[n.dataset.arch];
      })
    );
    const steps = [...root.querySelectorAll("#pipeline span")];
    root.querySelector("#runPipeline")?.addEventListener("click", async () => {
      for (let i = 0; i < steps.length; i++) {
        steps.forEach((s) => s.classList.remove("on"));
        steps[i].classList.add("on");
        note.textContent = ["메모리에서 명령 가져오기", "명령 해석", "연산 수행", "결과 저장/다음 명령"][i];
        await new Promise((r) => setTimeout(r, 550));
      }
      note.textContent = "한 사이클 완료! 실제 프로그램은 이 루프를 초당 수억 번 반복합니다.";
    });
  }

  function bindP3(root) {
    const data = {
      low: {
        code: `; 저급(어셈블리) 예시\nLDA #$01\nSTA $1000\nJMP START`,
        note: "하드웨어에 가깝고 제어는 세밀하지만, 생산성과 이식성은 낮습니다.",
      },
      high: {
        code: `# 고급 언어 예시 (Python 느낌)\ntotal = sum(values)\nprint("합계:", total)`,
        note: "문제 해결에 집중합니다. 컴파일러/런타임이 기계 세부사항을 대신 처리합니다.",
      },
      oop: {
        code: `// 객체지향 예시\nclass Cart {\n  add(item) { this.items.push(item); }\n  total() { return this.items.reduce((a,b)=>a+b.price,0); }\n}`,
        note: "데이터와 행동을 객체로 묶고, 상속·캡슐화·다형성으로 구조를 확장합니다.",
      },
    };
    const tabs = root.querySelectorAll("#langTabs button");
    const code = root.querySelector("#langCode");
    const note = root.querySelector("#langNote");
    tabs.forEach((tab) =>
      tab.addEventListener("click", () => {
        tabs.forEach((t) => t.classList.remove("active"));
        tab.classList.add("active");
        const d = data[tab.dataset.lang];
        code.textContent = d.code;
        note.textContent = d.note;
      })
    );
  }

  function bindP4(root) {
    const explain = {
      req: "요구사항 ↔ 인수/시스템 테스트: ‘무엇을 만들어야 하는가’를 사용자 관점에서 검증합니다.",
      arch: "설계 ↔ 통합 테스트: 컴포넌트 간 인터페이스와 협력이 설계대로인지 확인합니다.",
      mod: "모듈 설계 ↔ 단위 테스트: 가장 작은 단위의 정확성을 먼저 증명합니다.",
    };
    root.querySelectorAll("[data-v]").forEach((node) => {
      node.addEventListener("click", () => {
        const key = node.dataset.v;
        root.querySelectorAll("[data-v]").forEach((n) => {
          n.classList.toggle("active", n === node);
          n.classList.toggle("pair", n.dataset.v === key);
        });
        root.querySelector("#vExplain").textContent = explain[key];
      });
    });
  }

  function bindP5(root) {
    const tabs = root.querySelectorAll("#stdTabs button");
    const panels = root.querySelectorAll(".tab-panel");
    tabs.forEach((tab) =>
      tab.addEventListener("click", () => {
        tabs.forEach((t) => t.classList.remove("active"));
        tab.classList.add("active");
        panels.forEach((p) => p.classList.toggle("active", p.dataset.panel === tab.dataset.tab));
      })
    );
  }

  function bindP6(root) {
    const boxes = [...root.querySelectorAll("#reqChecks input")];
    const ring = root.querySelector("#reqScore");
    const text = root.querySelector("#reqScoreText");
    const update = () => {
      const on = boxes.filter((b) => b.checked).length;
      const pct = Math.round((on / boxes.length) * 100);
      ring.style.setProperty("--p", `${pct}%`);
      ring.textContent = `${pct}%`;
      text.textContent =
        pct < 40
          ? "위험 — 모호한 요구는 재작업을 부릅니다."
          : pct < 80
            ? "양호 — 수락기준과 비기능을 더 다듬어보세요."
            : "훌륭합니다 — 이 정도면 AI에게도, 팀에도 전달 가능한 요구입니다.";
    };
    boxes.forEach((b) => b.addEventListener("change", update));
  }

  function bindP7(root) {
    const run = () => {
      root.querySelectorAll("#impactBars i").forEach((bar) => {
        bar.style.width = "0%";
        requestAnimationFrame(() => {
          bar.style.width = `${bar.dataset.w}%`;
        });
      });
    };
    root.querySelector("#animateImpact")?.addEventListener("click", run);
    run();
  }

  function bindP8(root) {
    const build = () => {
      const goal = root.querySelector("#pbGoal").value.trim();
      const stack = root.querySelector("#pbStack").value.trim();
      const constraint = root.querySelector("#pbConstraint").value.trim();
      const done = root.querySelector("#pbDone").value.trim();
      root.querySelector("#pbOut").textContent = `역할: 신중한 시니어 엔지니어\n\n[컨텍스트]\n${stack}\n\n[과제]\n${goal}\n\n[제약]\n${constraint}\n\n[완료 정의]\n${done}\n\n먼저 변경 계획(파일/가정/리스크)을 제시하고, 내가 승인하면 작은 단위로 구현하세요.`;
    };
    root.querySelector("#pbBuild")?.addEventListener("click", build);
  }

  function bindP9(root) {
    const log = root.querySelector("#chatLog");
    const input = root.querySelector("#chatInput");
    const presets = {
      consult:
        "간단한 습관 트래커 웹페이지를 만들고 싶은데, 첫 버전 요구사항을 같이 정리해줄래?",
      plan: "방금 정리한 범위로 구현 계획을 파일 단위로 제안해줘. 코드는 아직 쓰지 마.",
      impl: "계획의 1단계만 구현해줘. HTML/JS만 사용하고 외부 라이브러리는 금지야.",
      review: "방금 구현한 결과에 대한 수동 검증 체크리스트와 회귀 포인트를 알려줘.",
    };

    function addBubble(role, text) {
      const div = document.createElement("div");
      div.className = `bubble ${role}`;
      div.textContent = text;
      log.appendChild(div);
      log.scrollTop = log.scrollHeight;
    }

    function agentReply(userText) {
      const t = userText.toLowerCase();
      let reply;
      if (t.includes("계획") || t.includes("plan")) {
        reply =
          "좋아요. 코드 전에 계획부터 제안할게요.\n1) index.html 골격\n2) localStorage 상태 모델\n3) UI 이벤트 연결\n4) 수동 검증 시나리오 3개\n어떤 단계부터 승인할까요?";
      } else if (t.includes("구현") || t.includes("만들어") || t.includes("코드")) {
        reply =
          "1슬라이스만 구현하는 방향으로 가죠.\n- 완료 정의: 항목 추가/체크/새로고침 유지\n- 제약: 라이브러리 금지\n구현 후 브라우저에서 추가→체크→새로고침 순으로 확인해 주세요. 결과를 알려주시면 다음 슬라이스로 넘어갈게요.";
      } else if (t.includes("검증") || t.includes("테스트") || t.includes("체크")) {
        reply =
          "검증 체크리스트:\n1) 빈 입력 방지\n2) 체크 상태 유지\n3) 삭제 후 저장 동기화\n4) 모바일 폭에서 버튼 클릭 영역\n실패 항목을 알려주시면 그 부분만 수정하겠습니다.";
      } else {
        reply =
          "요구를 같이 좁혀볼게요.\n- 사용자: 개인\n- 핵심 행동: 습관 추가 / 오늘 완료 체크\n- 비목표: 로그인, 클라우드 동기화\n- 수락기준: 새로고침 후에도 데이터 유지\n이 전제에서 시작할까요, 아니면 빼고 싶은 게 있나요?";
      }
      setTimeout(() => addBubble("agent", reply), 450);
    }

    addBubble(
      "agent",
      "안녕하세요! 저는 이 강좌의 연습용 AI Agent입니다.\n만들고 싶은 것, 쓰지 않을 것, 완료 기준을 편하게 말해보세요."
    );

    root.querySelector("#chatSend")?.addEventListener("click", () => {
      const text = input.value.trim();
      if (!text) return;
      addBubble("user", text);
      input.value = "";
      agentReply(text);
    });

    root.querySelectorAll("#chatPresets button").forEach((btn) => {
      btn.addEventListener("click", () => {
        input.value = presets[btn.dataset.preset];
        input.focus();
      });
    });
  }

  function bindPageInteractions(root, id) {
    bindQuiz(root);
    if (id === "p2") bindP2(root);
    if (id === "p3") bindP3(root);
    if (id === "p4") bindP4(root);
    if (id === "p5") bindP5(root);
    if (id === "p6") bindP6(root);
    if (id === "p7") bindP7(root);
    if (id === "p8") bindP8(root);
    if (id === "p9") bindP9(root);

    root.querySelectorAll("[data-go]").forEach((btn) => {
      btn.addEventListener("click", () => navigate(btn.dataset.go));
    });
  }

  function navigate(id) {
    if (!pageOrder.includes(id)) id = "home";
    state.current = id;
    if (id !== "home") {
      state.visited.add(id);
      saveProgress();
    }
    const page = COURSE.pages[id] || { id: "home", title: "홈" };
    const html = renderers[id](page);
    app.innerHTML = html;
    const section = app.querySelector(".page");
    section?.classList.add("active");
    bindPageInteractions(app, id);
    updateProgressUI();
    closeNav();
    window.scrollTo({ top: 0, behavior: "smooth" });
    if (location.hash.replace("#", "") !== id) {
      history.replaceState(null, "", `#${id}`);
    }
  }

  function openNav() {
    sidebar.classList.add("open");
    backdrop.classList.add("open");
    navToggle.setAttribute("aria-expanded", "true");
  }
  function closeNav() {
    sidebar.classList.remove("open");
    backdrop.classList.remove("open");
    navToggle.setAttribute("aria-expanded", "false");
  }

  navToggle.addEventListener("click", () => {
    if (sidebar.classList.contains("open")) closeNav();
    else openNav();
  });
  backdrop.addEventListener("click", closeNav);

  document.addEventListener("click", (e) => {
    const a = e.target.closest("[data-nav]");
    if (!a) return;
    e.preventDefault();
    navigate(a.dataset.nav);
  });

  window.addEventListener("hashchange", () => {
    const id = location.hash.replace("#", "") || "home";
    if (id !== state.current) navigate(id);
  });

  buildSidebar();
  navigate(location.hash.replace("#", "") || "home");
})();
