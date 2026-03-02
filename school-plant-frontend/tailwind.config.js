/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#52c41a', // Plant Green
        'primary-hover': '#73d13d',
        secondary: '#13c2c2',
      }
    },
  },
  plugins: [],
}
