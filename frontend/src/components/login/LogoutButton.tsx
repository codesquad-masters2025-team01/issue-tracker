import { useAuthStore } from "@/stores/useAuthStore";
import { apiFetch } from "@/hooks/useApiLoginFetch";
import styled from "@emotion/styled";

const ItemButton = styled.button`
  width: 100%;
  padding: 8px 16px;
  background: none;
  border: none;
  text-align: left;
  font: var(--display-medium16); /* Foundation typographic token */
  color: var(--neutral-text-default, #4e4b66);
  cursor: pointer;

  &:hover {
    background: var(--surface-bold, #f7f7fc);
  }
`;

export const LogoutButton = () => {
  const logout = useAuthStore((s) => s.logout);
  return (
    <ItemButton
      type="button"
      onClick={async () => {
        await apiFetch("/auth/logout", { method: "POST" }); // 서버에서 refresh 쿠키 삭제
        logout(); // 전역 상태 초기화
        location.href = "/login"; // 이동
      }}
    >
      로그아웃
    </ItemButton>
  );
};
