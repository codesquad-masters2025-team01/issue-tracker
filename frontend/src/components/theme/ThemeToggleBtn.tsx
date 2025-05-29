/** @jsxImportSource @emotion/react */
import styled from "@emotion/styled";
import { useThemeStore } from "@/stores/useThemeStore";

const ThemeToggleButton = styled.button`
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
