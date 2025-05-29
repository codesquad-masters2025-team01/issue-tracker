export const lightTheme = {
  mode: "light",
  colors: {
    grayscale: {
      50: "#FEFEFE",
      100: "#F7F7FC",
      200: "#EFF0F6",
      300: "#D9DBE9",
      400: "#BEC1D5",
      500: "#A0A3BD",
      600: "#6E7191",
      700: "#4E4B66",
      800: "#2A2A44",
      900: "#14142B",
    },
    accent: {
      blue: "#007AFF",
      navy: "#0025E6",
      red: "#FF3B30",
    },
    neutralText: {
      weak: "#6E7191", // grayscale.600
      default: "#4E4B66", // grayscale.700
      strong: "#14142B", // grayscale.900
    },
    surface: {
      default: "#F7F7FC", // grayscale.100
      bold: "#EFF0F6", // grayscale.200
      strong: "#FEFEFE", // grayscale.50
    },
    border: {
      default: "#D9DBE9", // grayscale.300
      defaultActive: "#14142B", // grayscale.900
    },
    brandText: {
      weak: "#007AFF", // accent.blue
      default: "#FEFEFE", // grayscale.50
    },
    brandSurface: {
      weak: "#FEFEFE", // grayscale.50
      default: "#007AFF",
    },
    brandBorder: {
      default: "#007AFF",
    },
    danger: {
      text: "#FF3B30",
      surface: "#FF3B30",
      border: "#FF3B30",
    },
  },
  radius: {
    half: "50%",
    medium: "12px",
    large: "16px",
  },
  border: {
    default: "1px solid",
    icon: "1.6px solid",
    dash: "1px dashed",
  },
  opacity: {
    hover: 0.8,
    default: 1,
    press: 0.64,
    disabled: 0.32,
  },
  dropShadow: {
    modal: "0px 4px 8px rgba(20, 20, 43, 0.04)", // lightmode
  },
};

export const darkTheme = {
  mode: "dark",
  colors: {
    grayscale: {
      50: "#FEFEFE",
      100: "#F7F7FC",
      200: "#EFF0F6",
      300: "#D9DBE9",
      400: "#BEC1D5",
      500: "#A0A3BD",
      600: "#6E7191",
      700: "#4E4B66",
      800: "#2A2A44",
      900: "#14142B",
    },
    accent: {
      blue: "#007AFF",
      navy: "#0025E6",
      red: "#FF3B30",
    },
    neutralText: {
      weak: "#A0A3BD", // grayscale.500
      default: "#BEC1D5", // grayscale.400
      strong: "#FEFEFE", // grayscale.50
    },
    surface: {
      default: "#14142B", // grayscale.900
      bold: "#4E4B66", // grayscale.700
      strong: "#2A2A44", // grayscale.800
    },
    border: {
      default: "#6E7191", // grayscale.600
      defaultActive: "#D9DBE9", // grayscale.300
    },
    brandText: {
      weak: "#007AFF",
      default: "#FEFEFE",
    },
    brandSurface: {
      weak: "#14142B",
      default: "#007AFF",
    },
    brandBorder: {
      default: "#007AFF",
    },
    danger: {
      text: "#FF3B30",
      surface: "#FF3B30",
      border: "#FF3B30",
    },
  },
  radius: {
    half: "50%",
    medium: "12px",
    large: "16px",
  },
  border: {
    default: "1px solid",
    icon: "1.6px solid",
    dash: "1px dashed",
  },
  opacity: {
    hover: 0.8,
    default: 1,
    press: 0.64,
    disabled: 0.32,
  },
  dropShadow: {
    modal: "0px 4px 16px rgba(20, 20, 43, 0.8)", // darkmode
  },
};

export type ThemeType = typeof lightTheme;

export default lightTheme;
