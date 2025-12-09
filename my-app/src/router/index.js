import { createRouter, createWebHistory } from 'vue-router'
import FlashSale from '../views/FlashSale.vue'
import Confirmation from '../views/ConfirmWindow.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'flashsale',
      component: FlashSale
    },
    {
      path: '/confirm/:userId/:productId',
      name: 'confirmation',
      component: Confirmation,
      props: true
    }
  ]
})

export default router