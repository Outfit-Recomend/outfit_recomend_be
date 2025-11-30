# 프론트엔드 연동 가이드

## API 엔드포인트

### Base URL
- 로컬 개발: `http://localhost:8080`
- 프로덕션: 배포된 서버 URL

### 주요 API

1. **코디 추천** (이미지 생성 포함)
   - `POST /api/outfit/recommend`
   - 이미지를 업로드하면 코디 추천 + 생성된 이미지 반환

2. **제품 추천** (이미지 생성 + 상품 목록)
   - `POST /api/outfit/products`
   - 이미지를 업로드하면 코디 추천 + 생성된 이미지 + 관련 상품 목록 반환

3. **헬스 체크**
   - `GET /api/outfit/health`

## 응답 구조

```typescript
interface OutfitSuggestion {
  description: string;           // 코디 텍스트 설명
  outfitImageUrl: string;        // 생성된 이미지 (Base64 Data URL)
  prompt: string;                // 이미지 생성 프롬프트
  searchQuery: string;           // 검색 쿼리
  products: ProductCandidate[];  // 추천 상품 목록 (products API만)
}

interface ProductCandidate {
  title: string;      // 상품 제목
  imageUrl: string;   // 상품 이미지 URL
  link: string;       // 상품 링크 (무신사)
  snippet: string;    // 상품 설명
  searchQuery: string; // 검색 쿼리
}
```

## 이미지 업로드 및 처리 흐름

### 1. 사용자가 이미지 선택
### 2. FormData로 이미지 파일 전송
### 3. 백엔드에서 이미지 분석 및 처리
### 4. JSON 응답 받기
### 5. Base64 이미지 URL을 img 태그에 직접 사용

## 프론트엔드 예제 코드

### React 예제

```tsx
import React, { useState } from 'react';

interface OutfitSuggestion {
  description: string;
  outfitImageUrl: string;
  prompt: string;
  searchQuery: string;
  products?: ProductCandidate[];
}

interface ProductCandidate {
  title: string;
  imageUrl: string;
  link: string;
  snippet: string;
  searchQuery: string;
}

function OutfitRecommendation() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<OutfitSuggestion | null>(null);
  const [error, setError] = useState<string | null>(null);

  const API_BASE_URL = 'http://localhost:8080'; // 프로덕션에서는 실제 URL로 변경

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setResult(null);
      setError(null);
    }
  };

  const handleRecommend = async () => {
    if (!selectedFile) {
      setError('이미지를 선택해주세요.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('image', selectedFile);

      const response = await fetch(`${API_BASE_URL}/api/outfit/recommend`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const data: OutfitSuggestion = await response.json();
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleProducts = async () => {
    if (!selectedFile) {
      setError('이미지를 선택해주세요.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('image', selectedFile);

      const response = await fetch(`${API_BASE_URL}/api/outfit/products`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const data: OutfitSuggestion = await response.json();
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="outfit-recommendation">
      <h1>코디 추천</h1>
      
      <div className="upload-section">
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={loading}
        />
        {selectedFile && (
          <div>
            <p>선택된 파일: {selectedFile.name}</p>
            <img
              src={URL.createObjectURL(selectedFile)}
              alt="미리보기"
              style={{ maxWidth: '300px', maxHeight: '300px' }}
            />
          </div>
        )}
      </div>

      <div className="button-section">
        <button onClick={handleRecommend} disabled={!selectedFile || loading}>
          {loading ? '처리 중...' : '코디 추천'}
        </button>
        <button onClick={handleProducts} disabled={!selectedFile || loading}>
          {loading ? '처리 중...' : '제품 추천'}
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="result-section">
          <h2>추천 결과</h2>
          
          <div className="description">
            <h3>설명</h3>
            <p>{result.description}</p>
          </div>

          {result.outfitImageUrl && (
            <div className="generated-image">
              <h3>생성된 코디 이미지</h3>
              {/* Base64 Data URL을 직접 img src에 사용 */}
              <img
                src={result.outfitImageUrl}
                alt="생성된 코디"
                style={{ maxWidth: '100%', height: 'auto' }}
              />
            </div>
          )}

          {result.products && result.products.length > 0 && (
            <div className="products">
              <h3>추천 상품 ({result.products.length}개)</h3>
              <div className="product-grid">
                {result.products.map((product, index) => (
                  <div key={index} className="product-item">
                    <img
                      src={product.imageUrl}
                      alt={product.title}
                      onError={(e) => {
                        (e.target as HTMLImageElement).src = '/placeholder.png';
                      }}
                    />
                    <h4>{product.title}</h4>
                    <p>{product.snippet}</p>
                    <a href={product.link} target="_blank" rel="noopener noreferrer">
                      상품 보기
                    </a>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default OutfitRecommendation;
```

### Vue 3 예제

```vue
<template>
  <div class="outfit-recommendation">
    <h1>코디 추천</h1>
    
    <div class="upload-section">
      <input
        type="file"
        accept="image/*"
        @change="handleFileChange"
        :disabled="loading"
      />
      <div v-if="selectedFile">
        <p>선택된 파일: {{ selectedFile.name }}</p>
        <img
          :src="previewUrl"
          alt="미리보기"
          style="max-width: 300px; max-height: 300px;"
        />
      </div>
    </div>

    <div class="button-section">
      <button @click="handleRecommend" :disabled="!selectedFile || loading">
        {{ loading ? '처리 중...' : '코디 추천' }}
      </button>
      <button @click="handleProducts" :disabled="!selectedFile || loading">
        {{ loading ? '처리 중...' : '제품 추천' }}
      </button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="result" class="result-section">
      <h2>추천 결과</h2>
      
      <div class="description">
        <h3>설명</h3>
        <p>{{ result.description }}</p>
      </div>

      <div v-if="result.outfitImageUrl" class="generated-image">
        <h3>생성된 코디 이미지</h3>
        <img
          :src="result.outfitImageUrl"
          alt="생성된 코디"
          style="max-width: 100%; height: auto;"
        />
      </div>

      <div v-if="result.products && result.products.length > 0" class="products">
        <h3>추천 상품 ({{ result.products.length }}개)</h3>
        <div class="product-grid">
          <div
            v-for="(product, index) in result.products"
            :key="index"
            class="product-item"
          >
            <img
              :src="product.imageUrl"
              :alt="product.title"
              @error="handleImageError"
            />
            <h4>{{ product.title }}</h4>
            <p>{{ product.snippet }}</p>
            <a :href="product.link" target="_blank" rel="noopener noreferrer">
              상품 보기
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface OutfitSuggestion {
  description: string;
  outfitImageUrl: string;
  prompt: string;
  searchQuery: string;
  products?: ProductCandidate[];
}

interface ProductCandidate {
  title: string;
  imageUrl: string;
  link: string;
  snippet: string;
  searchQuery: string;
}

const API_BASE_URL = 'http://localhost:8080'; // 프로덕션에서는 실제 URL로 변경

const selectedFile = ref<File | null>(null);
const loading = ref(false);
const result = ref<OutfitSuggestion | null>(null);
const error = ref<string | null>(null);

const previewUrl = computed(() => {
  if (selectedFile.value) {
    return URL.createObjectURL(selectedFile.value);
  }
  return '';
});

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0];
    result.value = null;
    error.value = null;
  }
};

const handleRecommend = async () => {
  if (!selectedFile.value) {
    error.value = '이미지를 선택해주세요.';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const formData = new FormData();
    formData.append('image', selectedFile.value);

    const response = await fetch(`${API_BASE_URL}/api/outfit/recommend`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`서버 오류: ${response.status}`);
    }

    const data: OutfitSuggestion = await response.json();
    result.value = data;
  } catch (err) {
    error.value = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
};

const handleProducts = async () => {
  if (!selectedFile.value) {
    error.value = '이미지를 선택해주세요.';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const formData = new FormData();
    formData.append('image', selectedFile.value);

    const response = await fetch(`${API_BASE_URL}/api/outfit/products`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`서버 오류: ${response.status}`);
    }

    const data: OutfitSuggestion = await response.json();
    result.value = data;
  } catch (err) {
    error.value = err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
};

const handleImageError = (e: Event) => {
  (e.target as HTMLImageElement).src = '/placeholder.png';
};
</script>
```

### Vanilla JavaScript 예제

```html
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>코디 추천</title>
  <style>
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
    }
    .upload-section {
      margin: 20px 0;
    }
    .button-section {
      margin: 20px 0;
    }
    button {
      padding: 10px 20px;
      margin-right: 10px;
      cursor: pointer;
    }
    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .error {
      color: red;
      margin: 10px 0;
    }
    .result-section {
      margin-top: 30px;
    }
    .product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }
    .product-item {
      border: 1px solid #ddd;
      padding: 10px;
      border-radius: 4px;
    }
    .product-item img {
      width: 100%;
      height: 200px;
      object-fit: contain;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>코디 추천</h1>
    
    <div class="upload-section">
      <input type="file" id="imageInput" accept="image/*">
      <div id="preview"></div>
    </div>

    <div class="button-section">
      <button id="recommendBtn">코디 추천</button>
      <button id="productsBtn">제품 추천</button>
    </div>

    <div id="error" class="error"></div>
    <div id="result" class="result-section"></div>
  </div>

  <script>
    const API_BASE_URL = 'http://localhost:8080'; // 프로덕션에서는 실제 URL로 변경

    const imageInput = document.getElementById('imageInput');
    const recommendBtn = document.getElementById('recommendBtn');
    const productsBtn = document.getElementById('productsBtn');
    const preview = document.getElementById('preview');
    const error = document.getElementById('error');
    const result = document.getElementById('result');

    let selectedFile = null;

    imageInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        selectedFile = e.target.files[0];
        preview.innerHTML = `
          <p>선택된 파일: ${selectedFile.name}</p>
          <img src="${URL.createObjectURL(selectedFile)}" alt="미리보기" style="max-width: 300px; max-height: 300px;">
        `;
        result.innerHTML = '';
        error.textContent = '';
      }
    });

    recommendBtn.addEventListener('click', async () => {
      await callAPI('/api/outfit/recommend');
    });

    productsBtn.addEventListener('click', async () => {
      await callAPI('/api/outfit/products');
    });

    async function callAPI(endpoint) {
      if (!selectedFile) {
        error.textContent = '이미지를 선택해주세요.';
        return;
      }

      recommendBtn.disabled = true;
      productsBtn.disabled = true;
      error.textContent = '';
      result.innerHTML = '<p>처리 중...</p>';

      try {
        const formData = new FormData();
        formData.append('image', selectedFile);

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
          method: 'POST',
          body: formData,
        });

        if (!response.ok) {
          throw new Error(`서버 오류: ${response.status}`);
        }

        const data = await response.json();
        displayResult(data);
      } catch (err) {
        error.textContent = err.message || '알 수 없는 오류가 발생했습니다.';
        result.innerHTML = '';
      } finally {
        recommendBtn.disabled = false;
        productsBtn.disabled = false;
      }
    }

    function displayResult(data) {
      let html = '<h2>추천 결과</h2>';
      
      html += `<div class="description"><h3>설명</h3><p>${data.description}</p></div>`;

      if (data.outfitImageUrl) {
        html += `
          <div class="generated-image">
            <h3>생성된 코디 이미지</h3>
            <img src="${data.outfitImageUrl}" alt="생성된 코디" style="max-width: 100%; height: auto;">
          </div>
        `;
      }

      if (data.products && data.products.length > 0) {
        html += `<div class="products"><h3>추천 상품 (${data.products.length}개)</h3><div class="product-grid">`;
        data.products.forEach(product => {
          html += `
            <div class="product-item">
              <img src="${product.imageUrl}" alt="${product.title}" onerror="this.src='/placeholder.png'">
              <h4>${product.title}</h4>
              <p>${product.snippet}</p>
              <a href="${product.link}" target="_blank" rel="noopener noreferrer">상품 보기</a>
            </div>
          `;
        });
        html += '</div></div>';
      }

      result.innerHTML = html;
    }
  </script>
</body>
</html>
```

## CORS 설정

백엔드에서 CORS가 이미 설정되어 있습니다. 기본적으로 다음 프론트엔드 포트가 허용됩니다:
- `http://localhost:3000` (React)
- `http://localhost:5173` (Vite)
- `http://localhost:8080` (Vue CLI)
- `http://localhost:4200` (Angular)

프로덕션 배포 시 `CorsConfig.java`에서 실제 프론트엔드 도메인을 추가해야 합니다.

## 중요 사항

### 1. Base64 이미지 처리
- `outfitImageUrl`은 `data:image/png;base64,...` 형식의 Data URL입니다
- `<img src={result.outfitImageUrl} />` 처럼 직접 사용 가능합니다
- 별도의 이미지 서버나 변환 과정이 필요 없습니다

### 2. 파일 크기 제한
- 최대 20MB까지 업로드 가능
- 프론트엔드에서도 파일 크기 체크 권장:
```javascript
if (file.size > 20 * 1024 * 1024) {
  alert('파일 크기는 20MB 이하여야 합니다.');
  return;
}
```

### 3. CORS 설정
- 백엔드에서 CORS를 허용해야 합니다 (아래 CORS 설정 참고)

### 4. 에러 처리
- 400: 잘못된 요청 (빈 파일 등)
- 500: 서버 오류
- 네트워크 오류 처리 필요

### 5. 로딩 상태
- 이미지 처리에 시간이 걸릴 수 있으므로 로딩 상태 표시 권장

## Axios 사용 예제

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

async function recommendOutfit(imageFile) {
  const formData = new FormData();
  formData.append('image', imageFile);

  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/outfit/recommend`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 60000, // 60초 타임아웃 (이미지 처리 시간 고려)
      }
    );
    return response.data;
  } catch (error) {
    if (error.response) {
      // 서버 응답 오류
      throw new Error(`서버 오류: ${error.response.status}`);
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못함
      throw new Error('서버에 연결할 수 없습니다.');
    } else {
      // 요청 설정 중 오류
      throw new Error(error.message);
    }
  }
}
```

