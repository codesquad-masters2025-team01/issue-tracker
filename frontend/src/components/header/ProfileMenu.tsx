"use client";

import { useState, useRef, useEffect } from "react";
import styled from "@emotion/styled";
import Profile from "../header/Profile"; // 아이콘
import { LogoutButton } from "../login/LogoutButton";

/* ─── Emotion Style ──────────────────────────────────────────────── */
const Wrapper = styled.div`
  position: relative; /* 기준 위치 */
`;

const IconButton = styled.button`
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  display: flex;
`;

const Dropdown = styled.div`
  position: absolute;
  right: 0;
  top: calc(100% + 8px); /* 아이콘 아래로 8px */
  min-width: 160px;
  padding: 12px 0;
  border: 1px solid var(--border-default, #d9dbe9);
  border-radius: 12px;
  background: var(--surface-default, #fff);
  box-shadow: 0 4px 8px rgba(20, 20, 43, 0.04); /* Foundation dropshadow.lightmode */
  z-index: 1000;
`;

export default function ProfileMenu() {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // ⬇︎ 바깥 클릭 시 닫기
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <Wrapper ref={menuRef}>
      <IconButton onClick={() => setOpen((v) => !v)}>
        <Profile />
      </IconButton>

      {open && (
        <Dropdown>
          {/* 원하는 항목을 자유롭게 추가 */}
          <LogoutButton />
        </Dropdown>
      )}
    </Wrapper>
  );
}
