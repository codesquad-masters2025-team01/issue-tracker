/** @jsxImportSource @emotion/react */
"use client";

import { useState, useRef, useEffect } from "react";
import styled from "@emotion/styled";
import FilterPopup from "./FilterPopup";
import { FilterDropdown } from "./FilterGroup";

type FilterKey = "assignee" | "label" | "milestone" | "writer";

interface Option {
  id: string;
  label: string;
  color?: string;
}

interface FilterDropdownButtonProps {
  filterKey: FilterKey;
  title: string;
  options: Option[];
  selectedId?: string;
  onSelect: (id: string) => void;
}

const RelativeContainer = styled.div`
  position: relative; /* 팝업의 기준점 */
  display: inline-block;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 0.875rem;
  background: transparent;
  border: none;
  cursor: pointer;
  color: ${({ theme }) => theme.colors.neutralText.default};
`;

const IndicatorDropdown = styled(FilterDropdown)`
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  min-width: 80px;
  background: transparent;
  border: none;
  cursor: pointer;
`;

export default function FilterDropdownButton({
  filterKey,
  title,
  options,
  selectedId,
  onSelect,
}: FilterDropdownButtonProps) {
  const [open, setOpen] = useState(false);
  const btnRef = useRef<HTMLDivElement>(null);

  /* ① 바깥 클릭 시 닫기 ------------------------------------------- */
  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (!btnRef.current?.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  /* ② 선택 처리 ---------------------------------------------------- */
  const handleOptionSelect = (id: string) => {
    onSelect(id);
    setOpen(false);
  };

  return (
    <RelativeContainer ref={btnRef}>
      <IndicatorDropdown
        label={title}
        hasDownIcon
        onClick={() => setOpen((prev) => !prev)}
      />

      {open && (
        <FilterPopup
          title={title}
          options={options}
          selectedId={selectedId}
          onSelect={handleOptionSelect}
        />
      )}
    </RelativeContainer>
  );
}
