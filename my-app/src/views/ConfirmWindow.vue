<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'


const router = useRouter()
const route = useRoute()

const userId = ref(route.params.userId || '')
const productId = ref(route.params.productId || '')
const currentTime = ref('')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const goBack = () => {
  // 락 해제 성공/실패 여부와 관계없이 페이지 이동
  router.push({ name: 'flashsale' })
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  updateTime()
})
</script>

<template>
  <div class="confirmation-container">
    <div class="confirmation-card">
      <div class="success-animation">
        <div class="checkmark-circle">
          <div class="checkmark"></div>
        </div>
      </div>

      <h1 class="title">🎉 신청 완료!</h1>
      <p class="subtitle">플래시 세일 신청이 성공적으로 완료되었습니다.</p>

      <div class="info-box">
        <div class="info-item">
          <span class="info-label">신청자 ID</span>
          <span class="info-value">{{ userId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">상품 ID</span>
          <span class="info-value">{{ productId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">신청 시간</span>
          <span class="info-value">{{ currentTime }}</span>
        </div>
      </div>

      <div class="notice">
        <p>📧 등록하신 이메일로 상세 안내가 발송될 예정입니다.</p>
        <p>💳 결제는 24시간 내에 진행해주세요.</p>
      </div>

      <div class="button-group">
        <button @click="goBack" class="btn btn-secondary">다시 참여하기</button>
        <button @click="goHome" class="btn btn-primary">홈으로</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.confirmation-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.confirmation-card {
  background: white;
  border-radius: 20px;
  padding: 50px 40px;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.success-animation {
  margin-bottom: 30px;
}

.checkmark-circle {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border-radius: 50%;
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  animation: scaleIn 0.5s ease-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.checkmark {
  width: 50px;
  height: 30px;
  border-left: 6px solid white;
  border-bottom: 6px solid white;
  transform: rotate(-45deg);
  animation: drawCheck 0.5s ease-out 0.3s forwards;
  opacity: 0;
}

@keyframes drawCheck {
  to {
    opacity: 1;
  }
}

.title {
  font-size: 2.5em;
  margin-bottom: 15px;
  color: #333;
}

.subtitle {
  font-size: 1.1em;
  color: #666;
  margin-bottom: 40px;
}

.info-box {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 30px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e0e0e0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: #666;
}

.info-value {
  font-weight: 500;
  color: #333;
  font-family: 'Courier New', monospace;
}

.notice {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
  text-align: left;
}

.notice p {
  margin: 10px 0;
  color: #856404;
  line-height: 1.6;
}

.button-group {
  display: flex;
  gap: 15px;
}

.btn {
  flex: 1;
  padding: 15px;
  font-size: 1.1em;
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

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

@media (max-width: 600px) {
  .confirmation-card {
    padding: 40px 30px;
  }

  .button-group {
    flex-direction: column;
  }

  .title {
    font-size: 2em;
  }
}
</style>