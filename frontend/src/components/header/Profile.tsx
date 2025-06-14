import Image from "next/image";
import ProfileMenu from "./ProfileMenu";

export default function Profile() {
  return (
    <Image
      src="/icons/profileIcon.svg"
      alt="이슈 트래커 로고"
      width={32}
      height={32}
    />
  );
}
