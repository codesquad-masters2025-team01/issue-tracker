import createCache from "@emotion/cache";

export const emotionCache = createCache({
  key: "css", // data-emotion="css"로 삽입
  prepend: true, // <head> 맨 앞에 삽입해 우선순위 확보
});
