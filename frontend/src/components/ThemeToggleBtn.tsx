/** @jsxImportSource @emotion/react */
import styled from "@emotion/styled";
import { useThemeStore } from "@/lib/useThemeStore";

const ThemeToggleButton = styled.button`
  // 중앙 정렬을 위한 작업
  position: absolute;
  left: 50%;
  transform: translateX(-50%);

  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.5rem;
`;

const ThemeToggleBtn: React.FC = () => {
  const { isDark, toggle } = useThemeStore();
  return (
    <ThemeToggleButton onClick={toggle}>
      {isDark ? (
        <span role="img" aria-label="Toggle Theme">
          🌙
        </span>
      ) : (
        <span role="img" aria-label="Toggle Theme">
          ☀️
        </span>
      )}
    </ThemeToggleButton>
  );
};

export default ThemeToggleBtn;
