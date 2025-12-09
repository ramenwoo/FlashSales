import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://10.150.7.5:8090/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const getStartTime = () => {
    // 백엔드의 GET /api/flash-sale/start-time 엔드포인트 호출
    return axios.get('/api/flash-sale/start-time')
}

const unlock = (userId, productId) => {
  return axios.post('/api/flash-sale/unlock', { userId, productId })
}

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    console.log('API 요청:', config.url)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API 에러:', error)
    if (error.response) {
      console.error('에러 상태:', error.response.status)
      console.error('에러 데이터:', error.response.data)
    }
    return Promise.reject(error)
  }
)

export default {
  // 플래시 세일 참여
  participate(userId, productId) {
    return api.post('/flash-sale/participate', {
      userId,
      productId
    })
  },

  // 재고 조회
  getStock(productId) {
    return api.get(`/flash-sale/stock/${productId}`)
  },

  // 참여 여부 확인
  checkParticipation(userId, productId) {
    return api.get(`/flash-sale/check/${userId}/${productId}`)
  },

  // 관리자: 재고 초기화
  initStock(productId, quantity) {
    return api.post('/admin/flash-sale/init-stock', null, {
      params: { productId, quantity }
    })
  },

  // 관리자: 플래시 세일 초기화
  resetFlashSale(productId) {
    return api.post('/admin/flash-sale/reset', null, {
      params: { productId }
    })
  },

  // 헬스 체크
  healthCheck() {
    return api.get('/test/health')
  },

  unlock,
  getStartTime
}