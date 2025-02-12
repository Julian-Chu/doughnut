module.exports = {
  verbose: true,
  collectCoverage: false,
  globals: {
    "@vue/vue3-jest": { babelConfig: true },
    "ts-jest": { babelConfig: true },
  },
  moduleFileExtensions: ["js", "ts", "json", "vue"],
  transform: {
    ".*\\.(vue)$": "@vue/vue3-jest",
    ".*\\.(js)$": "babel-jest",
    "^.+\\.(ts|tsx)$": "ts-jest",
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  setupFilesAfterEnv: ["./tests/setupJest.js"],
};
