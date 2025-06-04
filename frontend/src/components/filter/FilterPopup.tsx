/** @jsxImportSource @emotion/react */
import styled from "@emotion/styled";
interface Option {
  id: string;
  label: string;
  color?: string; // optional (예: 레이블의 경우 색상)
}
interface FilterPopupProps {
  title: string;
  options: Option[];
  selectedId?: string;
  onSelect: (id: string) => void;
}

const PopupWrapper = styled.div`
  position: absolute; // 필터 팝업을 표시하기 위한 레이어 분리 코드
  top: 2.5rem; /* Wrapper 내부 padding 포함 좌표 */
  right: 0.5rem;
  width: 200px;
  background-color: ${({ theme }) => theme.colors.surface.default};
  border: ${({ theme }) =>
    `${theme.border.default} ${theme.colors.border.default}`};
  border-radius: ${({ theme }) => theme.radius.medium};
  box-shadow: ${({ theme }) => theme.dropShadow.modal};
  z-index: 10;
`;

const PopupTitle = styled.div`
  padding: 0.75rem 1rem;
  font-weight: 600;
  font-size: 0.875rem;
  color: ${({ theme }) => theme.colors.neutralText.default};
  border-bottom: ${({ theme }) =>
    `${theme.border.default} ${theme.colors.border.default}`};
`;

const OptionItem = styled.button<{ selected: boolean; dotColor?: string }>`
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0.75rem 1rem;
  background-color: transparent;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  color: ${({ theme }) => theme.colors.neutralText.default};
  &:hover {
    background-color: ${({ theme }) => theme.colors.surface.bold};
  }

  &::before {
    content: "";
    display: ${({ dotColor }) => (dotColor ? "inline-block" : "none")};
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: ${({ dotColor }) => dotColor};
    margin-right: 8px;
  }

  &::after {
    content: ${({ selected }) => (selected ? "'●'" : "''")};
    margin-left: auto;
    color: ${({ theme }) => theme.colors.accent.blue};
    font-size: 0.75rem;
  }
`;

const FilterPopup = ({
  title,
  options,
  selectedId,
  onSelect,
}: FilterPopupProps) => {
  return (
    <PopupWrapper>
      <PopupTitle>{title}</PopupTitle>
      {options.map((option) => (
        <OptionItem
          key={option.id}
          selected={option.id === selectedId}
          dotColor={option.color}
          onClick={() => onSelect(option.id)}
        >
          {option.label}
        </OptionItem>
      ))}
    </PopupWrapper>
  );
};

export default FilterPopup;
