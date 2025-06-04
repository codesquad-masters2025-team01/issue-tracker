import styled from "@emotion/styled";
// import OpenIcon from "@/assets/icons/archive.svg?react";
// import ClosedIcon from "@/assets/icons/alertCircle.svg?react";

const IssueOpenTabWrapper = styled.div`
  display: flex;
  gap: 24px;
`;

const TabButton = styled.button<{ active?: boolean }>`
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
  background-color: transparent;
  color: ${({ active, theme }) =>
    active
      ? theme.colors.neutralText.strong
      : theme.colors.neutralText.default};
  font-size: 1rem;
  font-weight: 700;
  border: none;

  cursor: pointer;

  &:hover {
    opacity: 0.8;
  }
`;

const IconWrapper = styled.span`
  display: flex;
  align-items: center;
  color: inherit;

  svg {
    width: 16px;
    height: 16px;
  }
`;

const Label = styled.span`
  color: inherit;
`;

interface TabItemProps {
  icon?: React.ReactNode;
  label: string;
  active: boolean;
  onClick: () => void;
}

interface IssueOpenTabFilterProps {
  openCount: number;
  closeCount: number;
  selected: "open" | "closed";
  onChangeTab: (status: "open" | "closed") => void;
}

function TabItem({ icon, label, active, onClick }: TabItemProps) {
  return (
    <TabButton active={active} onClick={onClick}>
      <IconWrapper>{icon}</IconWrapper>
      <Label>{label}</Label>
    </TabButton>
  );
}

export default function IssueOpenTabFilter({
  openCount,
  closeCount,
  selected,
  onChangeTab,
}: IssueOpenTabFilterProps) {
  return (
    <IssueOpenTabWrapper>
      <TabItem
        // icon={<OpenIcon />}
        label={`열린 이슈 (${openCount})`}
        active={selected === "open"}
        onClick={() => onChangeTab("open")}
      />
      <TabItem
        // icon={<ClosedIcon />}
        label={`닫힌 이슈 (${closeCount})`}
        active={selected === "closed"}
        onClick={() => onChangeTab("closed")}
      />
    </IssueOpenTabWrapper>
  );
}
