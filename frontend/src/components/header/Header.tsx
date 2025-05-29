import Logo from "./Logo";
import ThemeToggleBtn from "../theme/ThemeToggleBtn";
import Profile from "./Profile";
import styled from "@emotion/styled";

const HeaderWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* position: relative; */
  height: 5.875rem;
  padding: 3rem 5rem 3rem 5rem;
  background-color: ${({ theme }) => theme.colors.surface.default};
`;

export default function Header() {
  return (
    <HeaderWrapper>
      <Logo />
      <ThemeToggleBtn />
      <Profile />
    </HeaderWrapper>
  );
}
