import { createApp } from 'vue'; 
import App from './App.vue'; 
import router from './router'; 
import { createPinia } from 'pinia'; 
import Antd from 'ant-design-vue'; 
import 'ant-design-vue/dist/reset.css'; 
import * as Icons from '@ant-design/icons-vue';

const app = createApp(App); 
app.use(createPinia()); 
app.use(router); 
app.use(Antd); 

// Register icons
const icons: any = Icons;
for (const i in icons) {
  app.component(i, icons[i]);
}

app.mount('#app');
