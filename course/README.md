# SW × Vibe Coding 인터랙티브 강좌

HTML/JS 기반 9페이지 인터랙티브 교육 사이트입니다.  
SW·컴퓨터 기초 → 개발 표준 프로세스 → AI 바이브 코딩 요구사항 관리까지 이어집니다.

## 구성

| 페이지 | 주제 |
|------|------|
| 1 | 에니악, 디버깅, 컴퓨터·언어의 시작 |
| 2 | CPU·메모리·ROM과 코드 실행 |
| 3 | 저급 / 고급 / 객체지향 언어 |
| 4 | V 개발모델 |
| 5 | CMMI, ISO 26262, UL 1998 |
| 6 | SW 요구사항 관리 |
| 7 | AI 바이브 코딩의 시작과 파급효과 |
| 8 | 컨텍스트·요구사항 지침 Top 5 |
| 9 | AI Agent와 상담하며 같이 개발하기 |

## 로컬 실행

정적 사이트이므로 아무 정적 서버나 브라우저로 열면 됩니다.

```bash
# Python
python3 -m http.server 8080

# 또는 Node
npx serve .
```

브라우저에서 `http://localhost:8080` 접속.

## 배포 (GitHub Pages)

1. 이 저장소 Settings → Pages
2. Source: **GitHub Actions** 또는 **Deploy from branch** (`main` / `/ (root)`)
3. 배포 후 URL: `https://<username>.github.io/sw-vibe-coding-course/`

포함되어 있는 `.github/workflows/deploy-pages.yml`이 `main` 푸시 시 Pages에 배포합니다.

## 이미지

각 페이지에 Wikimedia Commons의 실제 연관 이미지 1–2장을 사용합니다.  
교육·비상업적 목적의 인용이며, 원본 라이선스는 각 파일의 Commons 페이지를 따릅니다.

## 라이선스

강좌 텍스트·코드: MIT  
이미지: 각 Wikimedia 파일의 개별 라이선스
