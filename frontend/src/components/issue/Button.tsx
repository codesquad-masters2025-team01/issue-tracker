/** @jsxImportSource @emotion/react */
import styled from "@emotion/styled";

const StyledButton = styled.button`
  width: 8rem;
  height: 2.5rem;
  padding: 0.5rem 1rem; /* 8px 16px */
  font-size: 1rem; /* 16px */
  line-height: 1.5rem; /* 24px */
  font-weight: 500;
  border: ${({ theme }) =>
    `${theme.border.default} ${theme.colors.brandBorder.default}`};
  border-radius: ${({ theme }) => theme.radius.medium};
  background-color: ${({ theme }) => theme.colors.brandSurface.default};
  color: ${({ theme }) => theme.colors.brandText.default};
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover {
    opacity: ${({ theme }) => theme.opacity.hover};
  }
`;

export const Button: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>> = (
  props
) => <StyledButton {...props} />;

export default Button;
