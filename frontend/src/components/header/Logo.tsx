import Image from "next/image";
import { useThemeStore } from "@/stores/useThemeStore";
import styled from "@emotion/styled";

const Container = styled.div`
  cursor: pointer;
`;

export default function Logo() {
  const isDarkMode = useThemeStore((state) => state.isDark);
  const handleClickRefresh = () => {
    window.location.reload();
  };

  return (
    <Container onClick={handleClickRefresh}>
      {isDarkMode ? (
        <Image
          src="/icons/logoIconDark.svg"
          alt="이슈 트래커 로고 - 다크모드"
          width={199}
          height={40}
        />
      ) : (
        <Image
          src="/icons/logoIcon.svg"
          alt="이슈 트래커 로고"
          width={199}
          height={40}
        />
      )}
    </Container>
  );
}
