import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import Antd from "ant-design-vue";
import "ant-design-vue/dist/reset.css";

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(Antd);

app.config.errorHandler = (err, vm, info) => {
  console.error("Global Error Handler:", err, info);
};

app.mount("#app");
