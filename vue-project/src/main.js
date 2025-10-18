import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/bootstrap.min.css'
import axios from 'axios'
import { createStore } from 'vuex'

const app = createApp(App)

// 创建一个新的 store 实例
const store = createStore({
    state () {
        return {
            count: 0
        }
    },
    mutations: {
        increment (state) {
            state.count++
        }
    }
})

app.use(router)

app.mount('#app')


app.use(ElementPlus, { size: 'small', zIndex: 3000 })
app.use(store)

app.config.globalProperties.$axios = axios
axios.defaults.baseURL = 'http://localhost:3306'