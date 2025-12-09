<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import flashSaleService from '@/api/flashSaleService'

const router = useRouter()

// Data
const userId = ref('user_' + Math.random().toString(36).substr(2, 9))
const productId = ref('product_001')
const currentStock = ref(1)
const totalStock = ref(1)
const participated = ref(false)
const soldOut = ref(false)
const isProcessing = ref(false)
const message = ref('')
const messageType = ref('')
const saleStartTime = ref(null)
const saleStarted = ref(false)
const remainingSeconds = ref(0)
const isTotalStockInitialized = ref(false)
let stockInterval = null
let timerInterval = null
const isCoolingDown = ref(false)

// Computed
const stockPercentage = computed(() => {
  return totalStock.value > 0 ? (currentStock.value / totalStock.value) * 100 : 0
})

const formattedTime = computed(() => {
  const hours = Math.floor(remainingSeconds.value / 3600)
  const minutes = Math.floor(remainingSeconds.value % 3600 / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// Methods
const updateTimer = () => {
  const now = Date.now()
  const diff = saleStartTime.value - now

  if (diff <= 0) {
    saleStarted.value = true
    remainingSeconds.value = 0
  } else {
    remainingSeconds.value = Math.floor(diff / 1000)
  }
}

const fetchStock = async () => {
  try {
    const response = await flashSaleService.getStock(productId.value)
    const fetchedStock = response.data

    if (!isTotalStockInitialized.value) {
        totalStock.value = fetchedStock
        isTotalStockInitialized.value = true
    }

    currentStock.value = fetchedStock

    if (currentStock.value <= 0) {
      soldOut.value = true
      if (stockInterval) {
        clearInterval(stockInterval)
      }
    }
  } catch (error) {
    console.error('재고 조회 실패:', error)
  }
}

const checkParticipation = async () => {
  try {
    const response = await flashSaleService.checkParticipation(userId.value, productId.value)
    participated.value = response.data
  } catch (error) {
    console.error('참여 확인 실패:', error)
  }
}

const participate = async () => {
  if (!saleStarted.value) {
    showMessage('아직 시작 전입니다!', 'error')
    return
  }

  if (isProcessing.value || isCoolingDown.value) {
    return
  }

  isProcessing.value = true
  message.value = ''

  try {
    const response = await flashSaleService.participate(userId.value, productId.value)
    const result = response.data

    if (result.success) {
      participated.value = true
      showMessage(result.message, 'success')
      fetchStock()
    } else {
      showMessage(result.message, 'error')

      if (result.message.includes('마감')) {
        soldOut.value = true
      } else if (result.message.includes('다른 사용자')) {
        isCoolingDown.value = true
        setTimeout(() => {
            isCoolingDown.value = false
        }, 3000)
      }
    }
  } catch (error) {
    showMessage('오류가 발생했습니다. 다시 시도해주세요.', 'error')
    console.error('신청 실패:', error)
  } finally {
    isProcessing.value = false
  }
}

const showMessage = (text, type) => {
  message.value = text
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const goToConfirmPage = async () => {
  try {
    await flashSaleService.unlock(userId.value, productId.value)
    console.log(`Lock successfully released for user: ${userId.value}`)
  } catch (error) {
    console.error('락 해제 API 호출 실패:', error)
  }

  router.push({
    name: 'confirmation',
    params: {
      userId: userId.value,
      productId: productId.value
    }
  })
}

// Lifecycle
onMounted(async() => {
  // 세일 시작 시간 설정 (현재로부터 5초 후)
  try {
    const response = await flashSaleService.getStartTime()
    // ISO 문자열을 받아 Date 객체로 변환
    saleStartTime.value = new Date(response.data)
  } catch (error) {
    console.error("세일 시작 시간 로드 실패. 기본값 사용:", error)
    // 실패 시 임시 기본값 사용 (예: 현재 시간 + 1분)
    saleStartTime.value = new Date(Date.now() + 60000)
  }
  updateTimer()

  // 타이머 시작
  timerInterval = setInterval(updateTimer, 10)

  // 초기 재고 조회
  fetchStock()

  // 재고 polling 시작
  stockInterval = setInterval(fetchStock, 1000)

  // 참여 여부 확인
  checkParticipation()
})

onBeforeUnmount(() => {
  if (stockInterval) clearInterval(stockInterval)
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<template>
  <div class="flash-sale-container">
    <div class="flash-sale-card">
      <h1 class="title">⚡ Flash Sale</h1>

      <!-- 타이머 -->
      <div class="timer-section">
        <p class="timer-label">남은 시간</p>
        <div class="timer">{{ formattedTime }}</div>
      </div>

      <!-- 재고 정보 -->
      <div class="stock-section">
        <p class="stock-label">남은 수량</p>
        <div class="stock-bar">
          <div class="stock-fill" :style="{ width: stockPercentage + '%' }"></div>
        </div>
        <p class="stock-text">{{ currentStock }} / {{ totalStock }}</p>
      </div>

      <!-- 상태별 UI -->
      <div v-if="!saleStarted" class="status-section">
        <p class="status-message">곧 시작됩니다!</p>
        <button class="btn btn-disabled" disabled>대기중</button>
      </div>

      <div v-else-if="participated" class="status-section success">
        <div class="success-icon">✓</div>
        <p class="status-message">신청 완료!</p>
        <button @click="goToConfirmPage" class="btn btn-success">확인 페이지로 이동</button>
      </div>

      <div v-else-if="soldOut" class="status-section soldout">
        <p class="status-message">마감되었습니다</p>
        <button class="btn btn-disabled" disabled>마감</button>
      </div>

      <div v-else class="status-section">
        <button @click="participate" :disabled="isProcessing || isCoolingDown" class="btn btn-primary">
          {{ isProcessing ? '처리중...' : isCoolingDown ? '잠시 대기 중...' : '지금 신청하기' }}
        </button>
      </div>

      <!-- 메시지 -->
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.flash-sale-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.flash-sale-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.title {
  text-align: center;
  font-size: 2.5em;
  margin-bottom: 30px;
  color: #333;
}

.timer-section {
  text-align: center;
  margin-bottom: 30px;
}

.timer-label {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 10px;
}

.timer {
  font-size: 3em;
  font-weight: bold;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

.stock-section {
  margin-bottom: 30px;
}

.stock-label {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 10px;
  text-align: center;
}

.stock-bar {
  height: 30px;
  background: #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  margin-bottom: 10px;
}

.stock-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.5s ease;
}

.stock-text {
  text-align: center;
  font-weight: bold;
  color: #333;
  font-size: 1.1em;
}

.status-section {
  text-align: center;
}

.status-section.success {
  color: #27ae60;
}

.status-section.soldout {
  color: #e74c3c;
}

.success-icon {
  font-size: 4em;
  margin-bottom: 10px;
}

.status-message {
  font-size: 1.3em;
  font-weight: bold;
  margin-bottom: 20px;
}

.btn {
  width: 100%;
  padding: 18px;
  font-size: 1.2em;
  font-weight: bold;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover {
  background: #229954;
  transform: translateY(-2px);
}

.btn-disabled {
  background: #bdc3c7;
  color: white;
  cursor: not-allowed;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 10px;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

@media (max-width: 600px) {
  .flash-sale-card {
    padding: 30px 20px;
  }

  .title {
    font-size: 2em;
  }

  .timer {
    font-size: 2.5em;
  }
}
</style>