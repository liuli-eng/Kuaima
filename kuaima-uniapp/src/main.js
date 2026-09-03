import { createSSRApp } from "vue";
import App from "./App.vue";

export function createApp() {
  const app = createSSRApp(App);
  app.config.errorHandler = (error, instance, info) => {
    const message =
      error instanceof Error
        ? error.message
        : (() => {
            try {
              return JSON.stringify(error);
            } catch (_) {
              return String(error);
            }
          })();
    console.error(`[Vue error] ${message}`, info);
  };
  if (typeof uni !== "undefined") {
    uni.onError?.((error) => {
      const message =
        error instanceof Error
          ? error.message
          : (() => {
              try {
                return JSON.stringify(error);
              } catch (_) {
                return String(error);
              }
            })();
      console.error(`[uni error] ${message}`);
    });
    uni.onUnhandledRejection?.((event) => {
      const reason = event?.reason ?? event;
      const message =
        reason instanceof Error
          ? reason.message
          : (() => {
              try {
                return JSON.stringify(reason);
              } catch (_) {
                return String(reason);
              }
            })();
      console.error(`[unhandled rejection] ${message}`);
    });
  }
  return { app };
}
