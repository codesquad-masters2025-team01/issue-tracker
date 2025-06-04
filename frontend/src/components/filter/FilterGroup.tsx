/** @jsxImportSource @emotion/react */
// 레이블+마일스톤 필터 컴포넌트
import type { HTMLAttributes } from "react";
import Image from "next/image";
import styled from "@emotion/styled";

const Group = styled.div`
  display: flex;
  align-items: center;
`;

const Dropdown = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.5rem; /* 8px */
  font-size: 1rem;
  border: ${({ theme }) =>
    `${theme.border.default} ${theme.colors.border.default}`};
  border-radius: ${({ theme }) => theme.radius.medium};
  background-color: ${({ theme }) => theme.colors.surface.strong};
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover {
    opacity: ${({ theme }) => theme.opacity.hover};
  }
`;

const LabelText = styled.span`
  font-size: 1rem;
  /* 여기에서 레이블 텍스트에 적용할 CSS를 추가하세요 */
  color: ${({ theme }) => theme.colors.neutralText.default};
  margin: 0 0.25rem;
`;

interface FilterDropdownProps extends HTMLAttributes<HTMLDivElement> {
  icon?: React.ReactNode;
  label: string;
  hasDownIcon: boolean;
  onClick: () => void;
}

export const FilterGroup: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => <Group>{children}</Group>;

export const FilterDropdown: React.FC<FilterDropdownProps> = ({
  icon,
  label,
  hasDownIcon = true,
  onClick,
  className,
  ...rest
}: FilterDropdownProps) => (
  <Dropdown onClick={onClick} className={className} {...rest}>
    {icon}
    <LabelText>{label}</LabelText>
    {hasDownIcon && (
      <Image
        src="/icons/downIcon.svg"
        alt="필터용 아래 화살표 아이콘"
        width={16}
        height={16}
      />
    )}
  </Dropdown>
);
