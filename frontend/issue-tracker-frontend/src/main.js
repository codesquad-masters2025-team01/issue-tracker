import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Highlight.js CSS 추가 (GitHub 스타일)
import 'highlight.js/styles/github.css'

const app = createApp(App)
app.use(router)
app.mount('#app')