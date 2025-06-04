// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import IssueListView from '@/views/IssueListView.vue'
// import IssueDetailView from '@/views/IssueDetailView.vue'
// import CreateIssueView from '@/views/CreateIssueView.vue'
// import LabelManageView from '@/views/LabelManageView.vue'
// import MilestoneManageView from '@/views/MilestoneManageView.vue'

const routes = [
  {
    path: '/',
    redirect: '/issues'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/issues',
    name: 'IssueList',
    component: IssueListView,
    meta: { requiresAuth: true }
  },
  {
    path: '/issues/new',
    name: 'CreateIssue',
    component: () => import('@/views/CreateIssueView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/issues/:id',
    name: 'IssueDetail',
    component: () => import('@/views/IssueDetailView.vue'),  // 동적 import로 변경
    meta: { requiresAuth: true }
  },
  {
    path: '/labels',
    name: 'LabelManage',
    component: () => import('@/views/LabelManageView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/milestones',
    name: 'MilestoneManage',
    component: () => import('@/views/MilestoneManageView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 인증 가드
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/issues')
  } else {
    next()
  }
})

export default router