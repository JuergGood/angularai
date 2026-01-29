import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  {
    ignores: [
      'dist/**',
      'node_modules/**',
      'coverage/**',
      '*.config.js',
      'webpack.config.cjs',
      'cypress/**',
      '**/*.spec.ts',
      '**/*.cy.js',
    ],
  },
  {
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn',
      'max-lines': ['error', { max: 500, skipBlankLines: true, skipComments: true }],
      '@typescript-eslint/no-unused-vars': 'warn',
      'no-undef': 'off',
    }
  },
  {
    files: ['src/**/*.ts'],
    rules: {
      // Add custom rules for component size or other project-specific requirements
    }
  }
);
